package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import cats.implicits.catsSyntaxApplicativeId
import com.joshcough.trollabot.ChannelName
import com.joshcough.trollabot.db.queries.{Scores => ScoreQueries}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

case class Score(
    channel: ChannelName,
    player1: Option[String],
    player2: Option[String],
    player1Score: Int,
    player2Score: Int
) {
  def display: String =
    s"${player1.getOrElse("player")} $player1Score - $player2Score ${player2.getOrElse("opponent")}"
}

object Score {
  def empty(channelName: ChannelName) = Score(channelName, None, None, 0, 0)
}

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
    withScore(
      channelName,
      ScoreQueries.getScore(channelName).option.map(_.getOrElse(Score.empty(channelName)))
    )

  def withScore(channelName: ChannelName, action: ConnectionIO[Score]): ConnectionIO[Score] =
    for {
      o <- ScoreQueries.getScore(channelName).option
      _ <- if (o.isEmpty) ScoreQueries.insertChannel(channelName).run else 0.pure[ConnectionIO]
      s <- action
    } yield s
}
