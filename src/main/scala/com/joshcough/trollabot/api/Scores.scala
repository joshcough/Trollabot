package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.twitch._
import com.joshcough.trollabot.{ChannelName, Score}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

trait Scores[F[_]] {
  def setPlayer1(channelName: ChannelName, player1: String): F[Score]
  def setPlayer2(channelName: ChannelName, player2: String): F[Score]
  def setPlayer1Score(channelName: ChannelName, player1Score: Int): F[Score]
  def setPlayer2Score(channelName: ChannelName, player2Score: Int): F[Score]
  def setScore(channelName: ChannelName, player1Score: Int, player2Score: Int): F[Score]
  def setAll(
      channelName: ChannelName,
      player1: String,
      player1Score: Int,
      player2: String,
      player2Score: Int
  ): F[Score]
  def getScore(channelName: ChannelName): F[Score]
}

object Scores {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Scores[F] =
    new Scores[F] {
      def setPlayer1(channelName: ChannelName, player1: String): F[Score] =
        ScoresDb.setPlayer1(channelName, player1).transact(xa)
      def setPlayer2(channelName: ChannelName, player2: String): F[Score] =
        ScoresDb.setPlayer2(channelName, player2).transact(xa)
      def setPlayer1Score(channelName: ChannelName, player1Score: Int): F[Score] =
        ScoresDb.setPlayer1Score(channelName, player1Score).transact(xa)
      def setPlayer2Score(channelName: ChannelName, player2Score: Int): F[Score] =
        ScoresDb.setPlayer2Score(channelName, player2Score).transact(xa)
      def setScore(channelName: ChannelName, player1Score: Int, player2Score: Int): F[Score] =
        ScoresDb.setScore(channelName, player1Score, player2Score).transact(xa)
      def setAll(
          channelName: ChannelName,
          player1: String,
          player1Score: Int,
          player2: String,
          player2Score: Int
      ): F[Score] =
        ScoresDb.setAll(channelName, player1, player1Score, player2, player2Score).transact(xa)
      def getScore(channelName: ChannelName): F[Score] =
        ScoresDb.getScore(channelName).transact(xa)
    }
}

object ScoreQueries {

  def insertChannel(channelName: ChannelName): Update0 =
    sql"""insert into scores (channel, player1_score, player2_score)
          select s.id, 0, 0
          from streams s where s.name = ${channelName.name}
       """.update

  def setPlayer1(channelName: ChannelName, player1: String): Query0[Score] =
    updateScores(channelName, fr"set player1 = $player1")

  def setPlayer2(channelName: ChannelName, player2: String): Query0[Score] =
    updateScores(channelName, fr"set player2 = $player2")

  def setPlayer1Score(channelName: ChannelName, player1Score: Int): Query0[Score] =
    updateScores(channelName, fr"set player1_score = $player1Score")

  def setPlayer2Score(channelName: ChannelName, player2Score: Int): Query0[Score] =
    updateScores(channelName, fr"set player2_score = $player2Score")

  def setScore(channelName: ChannelName, player1Score: Int, player2Score: Int): Query0[Score] =
    updateScores(channelName, fr"set player1_score = $player1Score, player2_score = $player2Score")

  def setAll(
      channelName: ChannelName,
      player1: String,
      player1Score: Int,
      player2: String,
      player2Score: Int
  ): Query0[Score] =
    updateScores(channelName, fr"""set player1 = $player1, player2 = $player2,
               player1_score = $player1Score, player2_score = $player2Score""")

  def getScore(channelName: ChannelName): Query0[Score] =
    sql"""select * from scores
          join streams on scores.channel = streams.id
          where streams.name = ${channelName.name}""".query[Score]

  def updateScores(channelName: ChannelName, fragment: Fragment): Query0[Score] =
    (fr"update scores" ++ fragment ++
      fr"""from streams
           where scores.channel = streams.id and streams.name = ${channelName.name}
           returning *""").query[Score]
}

object ScoresDb extends Scores[ConnectionIO] {
  def setPlayer1(channelName: ChannelName, player1: String): ConnectionIO[Score] =
    withScore(channelName, ScoreQueries.setPlayer1(channelName, player1).unique)
  def setPlayer2(channelName: ChannelName, player2: String): ConnectionIO[Score] =
    withScore(channelName, ScoreQueries.setPlayer2(channelName, player2).unique)
  def setPlayer1Score(channelName: ChannelName, player1Score: Int): ConnectionIO[Score] =
    withScore(channelName, ScoreQueries.setPlayer1Score(channelName, player1Score).unique)
  def setPlayer2Score(channelName: ChannelName, player2Score: Int): ConnectionIO[Score] =
    withScore(channelName, ScoreQueries.setPlayer2Score(channelName, player2Score).unique)
  def setScore(
      channelName: ChannelName,
      player1Score: Int,
      player2Score: Int
  ): ConnectionIO[Score] =
    withScore(channelName, ScoreQueries.setScore(channelName, player1Score, player2Score).unique)
  def setAll(
      channelName: ChannelName,
      player1: String,
      player1Score: Int,
      player2: String,
      player2Score: Int
  ): ConnectionIO[Score] =
    withScore(
      channelName,
      ScoreQueries.setAll(channelName, player1, player1Score, player2, player2Score).unique
    )
  def getScore(channelName: ChannelName): ConnectionIO[Score] =
    withScore(channelName, ScoreQueries.getScore(channelName).option.map(_.getOrElse(Score.empty)))

  def withScore(channelName: ChannelName, action: ConnectionIO[Score]): ConnectionIO[Score] =
    for {
      o <- ScoreQueries.getScore(channelName).option
      _ <- if (o.isEmpty) ScoreQueries.insertChannel(channelName).run else 0.pure[ConnectionIO]
      s <- action
    } yield s
}

object ScoreCommands {

  lazy val scoreCommands: List[BotCommand] = List(scoreCommand, playerCommand, opponentCommand)

  sealed trait ScoreArg
  case object GetScore extends ScoreArg
  case class SetScore(player1Score: Int, player2Score: Int) extends ScoreArg
  case class SetAll(player1: String, player2: String, player1Score: Int, player2Score: Int)
      extends ScoreArg

  object ScoreArg {
    implicit val encodeResponse: Encoder[ScoreArg] = Encoder.instance {
      case _ @GetScore            => "GetScore".asJson
      case r @ SetScore(_, _)     => r.asJson
      case r @ SetAll(_, _, _, _) => r.asJson
    }

    implicit val decodeResponse: Decoder[ScoreArg] =
      List[Decoder[ScoreArg]](
        Decoder[GetScore.type].widen,
        Decoder[SetScore].widen,
        Decoder[SetAll].widen
      ).reduceLeft(_ or _)
  }

  case class ScoreAction(channelName: ChannelName, scoreArg: ScoreArg) extends Action {
    override def run: Stream[ConnectionIO, Response] =
      scoreArg match {
        case GetScore           => ScoreCommands.getScore(channelName)
        case SetScore(p1s, p2s) => ScoreCommands.setScore(channelName, p1s, p2s)
        case SetAll(p1, p2, p1s, p2s) =>
          ScoreCommands.setScoreAndPlayers(channelName, p1, p2, p1s, p2s)
      }
  }
  case class SetPlayerAction(channelName: ChannelName, playerName: String) extends Action {
    override def run: Stream[ConnectionIO, Response] = setPlayer(channelName, playerName)
  }
  case class SetOpponentAction(channelName: ChannelName, playerName: String) extends Action {
    override def run: Stream[ConnectionIO, Response] = setOpponent(channelName, playerName)
  }

  val scoreCommand: BotCommand = {
    val scoreParser: Parser[ScoreArg] =
      eof.named("no arguments").^^^(GetScore) |
        (int.named("p1_score") ~ int.named("p2_score")).map { case p1 ~ p2 => SetScore(p1, p2) } |
        (anyString.named("p1_name") ~ int.named("p1_score") ~
          anyString.named("p2_name") ~ int.named("p2_score")).map {
          case p1 ~ p1Score ~ p2 ~ p2Score => SetAll(p1, p2, p1Score, p2Score)
        }
    // this is dumb, it should really be a ScoreAction.
    def perms(a: ScoreAction): Permission =
      a.scoreArg match {
        case GetScore => Anyone
        case _        => ModOnly
      }
    BotCommand[ScoreArg, ScoreAction]("!score", scoreParser, perms)((channelName, _, scoreArg) =>
      ScoreAction(channelName, scoreArg)
    )
  }

  val playerCommand: BotCommand =
    BotCommand[String, SetPlayerAction]("!player", anyStringAs("player_name"), _ => ModOnly)(
      (channelName, _, name) => SetPlayerAction(channelName, name)
    )

  val opponentCommand: BotCommand =
    BotCommand[String, SetOpponentAction]("!opponent", anyStringAs("player_name"), _ => ModOnly)(
      (channelName, _, name) => SetOpponentAction(channelName, name)
    )

  def getScore(channel: ChannelName): Stream[ConnectionIO, Response] =
    Stream.eval(ScoresDb.getScore(channel).map(s => RespondWith(s.display)))

  def setScore(channel: ChannelName, p1s: Int, p2s: Int): Stream[ConnectionIO, Response] =
    Stream.eval(ScoresDb.setScore(channel, p1s, p2s).map(s => RespondWith(s.display)))

  def setPlayer(channel: ChannelName, p1: String): Stream[ConnectionIO, Response] =
    Stream.eval(ScoresDb.setPlayer1(channel, p1).map(s => RespondWith(s.display)))

  def setOpponent(channel: ChannelName, p2: String): Stream[ConnectionIO, Response] =
    Stream.eval(ScoresDb.setPlayer2(channel, p2).map(s => RespondWith(s.display)))

  def setScoreAndPlayers(
      channel: ChannelName,
      p1: String,
      p2: String,
      p1s: Int,
      p2s: Int
  ): Stream[ConnectionIO, Response] =
    Stream.eval(ScoresDb.setAll(channel, p1, p1s, p2, p2s).map(s => RespondWith(s.display)))
}
