package com.joshcough.trollabot.twitch.commands

import cats.Monad
import cats.implicits.toFunctorOps
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.ChannelName
import com.joshcough.trollabot.api.Api
import fs2.Stream

object Scores {

  lazy val scoreCommands: List[BotCommand] = List(scoreCommand, playerCommand, opponentCommand)

  sealed trait ScoreArg {
    def run[F[_]: Monad](api: Api[F])(channelName: ChannelName): Stream[F, Response]
  }

  case class GetScore() extends ScoreArg {
    override def run[F[_] : Monad](api: Api[F])(channelName: ChannelName): Stream[F, Response] =
      getScore(api)(channelName)
  }
  case class SetScore(player1Score: Int, player2Score: Int) extends ScoreArg{
    def run[F[_]: Monad](api: Api[F])(channelName: ChannelName): Stream[F, Response] =
      setScore(api)(channelName, player1Score, player2Score)
  }
  case class SetAll(player1: String, player2: String, player1Score: Int, player2Score: Int)
      extends ScoreArg {
    def run[F[_]: Monad](api: Api[F])(channelName: ChannelName): Stream[F, Response] =
      setScoreAndPlayers(api)(channelName, player1, player2, player1Score, player2Score)
  }

  trait ScoreActionI extends Action

  case class ScoreAction(channelName: ChannelName, scoreArg: ScoreArg) extends ScoreActionI {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] = scoreArg.run(api)(channelName)
  }
  case class SetPlayerAction(channelName: ChannelName, playerName: String) extends ScoreActionI {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      setPlayer(api)(channelName, playerName)
  }
  case class SetOpponentAction(channelName: ChannelName, playerName: String) extends ScoreActionI {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      setOpponent(api)(channelName, playerName)
  }

  val scoreCommand: BotCommand = {
    val scoreParser: Parser[ScoreArg] =
      eof.named("no arguments").^^^(GetScore()) |
        (int.named("p1_score") ~ int.named("p2_score")).map { case p1 ~ p2 => SetScore(p1, p2) } |
        (anyString.named("p1_name") ~ int.named("p1_score") ~
          anyString.named("p2_name") ~ int.named("p2_score")).map {
          case p1 ~ p1Score ~ p2 ~ p2Score => SetAll(p1, p2, p1Score, p2Score)
        }
    // this is dumb, it should really be a ScoreAction.
    def perms(a: ScoreAction): Permission =
      a.scoreArg match {
        case GetScore()  => Anyone
        case _           => Mod
      }
    BotCommand[ScoreArg, ScoreAction]("!score", scoreParser, perms)((channelName, _, scoreArg) =>
      ScoreAction(channelName, scoreArg)
    )
  }

  val playerCommand: BotCommand =
    BotCommand[String, SetPlayerAction]("!player", anyStringAs("player_name"), _ => Mod)(
      (channelName, _, name) => SetPlayerAction(channelName, name)
    )

  val opponentCommand: BotCommand =
    BotCommand[String, SetOpponentAction]("!opponent", anyStringAs("player_name"), _ => Mod)(
      (channelName, _, name) => SetOpponentAction(channelName, name)
    )

  def getScore[F[_]: Monad](api: Api[F])(channel: ChannelName): Stream[F, Response] =
    Stream.eval(api.scores.getScore(channel).map(s => RespondWith(s.display)))

  def setScore[F[_]: Monad](
      api: Api[F]
  )(channel: ChannelName, p1s: Int, p2s: Int): Stream[F, Response] =
    Stream.eval(api.scores.setScore(channel, p1s, p2s).map(s => RespondWith(s.display)))

  def setPlayer[F[_]: Monad](api: Api[F])(channel: ChannelName, p1: String): Stream[F, Response] =
    Stream.eval(api.scores.setPlayer1(channel, p1).map(s => RespondWith(s.display)))

  def setOpponent[F[_]: Monad](api: Api[F])(channel: ChannelName, p2: String): Stream[F, Response] =
    Stream.eval(api.scores.setPlayer2(channel, p2).map(s => RespondWith(s.display)))

  def setScoreAndPlayers[F[_]: Monad](api: Api[F])(
      channel: ChannelName,
      p1: String,
      p2: String,
      p1s: Int,
      p2s: Int
  ): Stream[F, Response] =
    Stream.eval(api.scores.setAll(channel, p1, p1s, p2, p2s).map(s => RespondWith(s.display)))
}
