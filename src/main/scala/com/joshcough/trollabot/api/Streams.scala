package com.joshcough.trollabot.api

import cats.Monad
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.{ChannelName, Queries, Stream}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

abstract class Streams[F[_]: Monad] {
  def getStreams: fs2.Stream[F, Stream]
  def markParted(channelName: ChannelName): F[Boolean]
  def markJoined(channelName: ChannelName): F[Boolean]
  def insertStream(channelName: ChannelName): F[Boolean]
  def doesStreamExist(channelName: ChannelName): F[Boolean]
  def getAllStreams: fs2.Stream[F, Stream]
  def getJoinedStreams: fs2.Stream[F, Stream]

  def join(channelName: ChannelName): F[Boolean] =
    for {
      b <- doesStreamExist(channelName)
      z <- if (b) markJoined(channelName) else insertStream(channelName)
    } yield z
}

object Streams {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Streams[F] =
    new Streams[F] {
      def getStreams: fs2.Stream[F, Stream] = StreamsDb.getStreams.transact(xa)
      def markParted(channelName: ChannelName): F[Boolean] =
        StreamsDb.markParted(channelName).transact(xa)
      def markJoined(channelName: ChannelName): F[Boolean] =
        StreamsDb.markJoined(channelName).transact(xa)
      def insertStream(channelName: ChannelName): F[Boolean] =
        StreamsDb.insertStream(channelName).transact(xa)
      def doesStreamExist(channelName: ChannelName): F[Boolean] =
        StreamsDb.doesStreamExist(channelName).transact(xa)
      def getAllStreams: fs2.Stream[F, Stream] = StreamsDb.getAllStreams.transact(xa)
      def getJoinedStreams: fs2.Stream[F, Stream] = StreamsDb.getJoinedStreams.transact(xa)
    }
}

object StreamsDb extends Streams[ConnectionIO] {
  def getStreams: fs2.Stream[ConnectionIO, Stream] = Queries.getAllStreams.stream
  def markParted(channelName: ChannelName): ConnectionIO[Boolean] =
    Queries.partStream(channelName).run.map(_ > 0)
  def markJoined(channelName: ChannelName): ConnectionIO[Boolean] =
    Queries.joinStream(channelName).run.map(_ > 0)
  def insertStream(channelName: ChannelName): ConnectionIO[Boolean] =
    Queries.insertStream(Stream(None, channelName, joined = false)).run.map(_ > 0)
  def doesStreamExist(channelName: ChannelName): ConnectionIO[Boolean] =
    Queries.doesStreamExist(channelName).unique
  def getAllStreams: fs2.Stream[ConnectionIO, Stream] = Queries.getAllStreams.stream
  def getJoinedStreams: fs2.Stream[ConnectionIO, Stream] = Queries.getJoinedStreams.stream
}
