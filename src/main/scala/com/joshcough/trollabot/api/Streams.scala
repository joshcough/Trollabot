package com.joshcough.trollabot.api

import cats.Monad
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.{ChannelName, ChatUserName}
import com.joshcough.trollabot.db.queries.{Streams => StreamQueries}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import java.sql.Timestamp

case class Stream(name: ChannelName, joined: Boolean, addedBy: ChatUserName, addedAt: Timestamp)

case class StreamException(msg: String) extends RuntimeException

abstract class Streams[F[_]: Monad] {
  def getStreams: fs2.Stream[F, Stream]
  def getStreamByName(channelName: ChannelName): F[Option[Stream]]
  def markParted(channelName: ChannelName): F[Boolean]
  def markJoined(channelName: ChannelName): F[Boolean]
  def insertStream(
      channelName: ChannelName,
      joined: Boolean,
      username: ChatUserName
  ): F[Either[Stream, Stream]]
  def getAllStreams: fs2.Stream[F, Stream]
  def getJoinedStreams: fs2.Stream[F, Stream]

  def join(channelName: ChannelName, username: ChatUserName): F[Either[Stream, Stream]] = {
    for {
      os <- getStreamByName(channelName)
      z <- os match {
        case Some(s) => markJoined(channelName) *> Left(s.copy(joined = true)).pure[F]
        case None    => insertStream(channelName, joined = true, username)
      }
    } yield z
  }
}

object Streams {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Streams[F] =
    new Streams[F] {
      def getStreams: fs2.Stream[F, Stream] = StreamsDb.getStreams.transact(xa)
      def getStreamByName(channelName: ChannelName): F[Option[Stream]] =
        StreamsDb.getStreamByName(channelName).transact(xa)
      def markParted(channelName: ChannelName): F[Boolean] =
        StreamsDb.markParted(channelName).transact(xa)
      def markJoined(channelName: ChannelName): F[Boolean] =
        StreamsDb.markJoined(channelName).transact(xa)
      def insertStream(
          channelName: ChannelName,
          joined: Boolean,
          username: ChatUserName
      ): F[Either[Stream, Stream]] =
        StreamsDb.insertStream(channelName, joined, username).transact(xa)
      def getAllStreams: fs2.Stream[F, Stream] = StreamsDb.getAllStreams.transact(xa)
      def getJoinedStreams: fs2.Stream[F, Stream] = StreamsDb.getJoinedStreams.transact(xa)
    }
}

object StreamsDb extends Streams[ConnectionIO] {
  def getStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getAllStreams.stream
  def getStreamByName(channelName: ChannelName): ConnectionIO[Option[Stream]] =
    StreamQueries.getStreamByName(channelName).option
  def markParted(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.partStream(channelName).run.map(_ > 0)
  def markJoined(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.joinStream(channelName).run.map(_ > 0)

  def insertStream(
      channelName: ChannelName,
      joined: Boolean,
      username: ChatUserName
  ): ConnectionIO[Either[Stream, Stream]] =
    for {
      o <- StreamQueries.getStreamByName(channelName).option
      r <- o match {
        case None    => StreamQueries.insertStream(channelName, joined, username).unique.map(Right(_))
        case Some(q) => Left(q).pure[ConnectionIO]
      }
    } yield r

  def getAllStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getAllStreams.stream
  def getJoinedStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getJoinedStreams.stream
}
