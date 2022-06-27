package com.joshcough.trollabot.api

import cats.Monad
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.{Stream, Queries}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

abstract class Streams[F[_]: Monad] {
  def getStreams: fs2.Stream[F, Stream]
  def markParted(streamName: String): F[Boolean]
  def markJoined(streamName: String): F[Boolean]
  def insertStream(streamName: String): F[Boolean]
  def doesStreamExist(streamName: String): F[Boolean]
  def getAllStreams: fs2.Stream[F, Stream]
  def getJoinedStreams: fs2.Stream[F, Stream]

  def join(streamName: String): F[Boolean] =
    for {
      b <- doesStreamExist(streamName)
      z <- if (b) markJoined(streamName) else insertStream(streamName)
    } yield z
}

object Streams {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Streams[F] =
    new Streams[F] {
      def getStreams: fs2.Stream[F, Stream] = StreamsDb.getStreams.transact(xa)
      def markParted(streamName: String): F[Boolean] = StreamsDb.markParted(streamName).transact(xa)
      def markJoined(streamName: String): F[Boolean] = StreamsDb.markJoined(streamName).transact(xa)
      def insertStream(streamName: String): F[Boolean] = StreamsDb.insertStream(streamName).transact(xa)
      def doesStreamExist(streamName: String): F[Boolean] = StreamsDb.doesStreamExist(streamName).transact(xa)
      def getAllStreams: fs2.Stream[F, Stream] = StreamsDb.getAllStreams.transact(xa)
      def getJoinedStreams: fs2.Stream[F, Stream] = StreamsDb.getJoinedStreams.transact(xa)
    }
}

object StreamsDb extends Streams[ConnectionIO] {
  def getStreams: fs2.Stream[ConnectionIO, Stream] = Queries.getAllStreams.stream
  def markParted(streamName: String): ConnectionIO[Boolean] = Queries.partStream(streamName).run.map(_ > 0)
  def markJoined(streamName: String): ConnectionIO[Boolean] = Queries.joinStream(streamName).run.map(_ > 0)
  def insertStream(streamName: String): ConnectionIO[Boolean] =
    Queries.insertStream(Stream(None, streamName, joined = false)).run.map(_ > 0)
  def doesStreamExist(streamName: String): ConnectionIO[Boolean] = Queries.doesStreamExist(streamName).unique
  def getAllStreams: fs2.Stream[ConnectionIO, Stream] = Queries.getAllStreams.stream
  def getJoinedStreams: fs2.Stream[ConnectionIO, Stream] = Queries.getJoinedStreams.stream
}
