package com.joshcough.trollabot.api

import cats.Monad
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.ChannelName
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

case class Stream(name: ChannelName, joined: Boolean)

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

object StreamQueries {

  val getJoinedStreams: Query0[Stream] =
    sql"select * from streams where joined = true".query[Stream]

  val getAllStreams: Query0[Stream] =
    sql"select * from streams".query[Stream]

  def doesStreamExist(channelName: ChannelName): Query0[Boolean] =
    sql"select exists(select true from streams where name = ${channelName.name})".query[Boolean]

  // TODO: what if stream already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertStream(s: Stream): Update0 =
    sql"insert into streams (name, joined) values (${s.name}, ${s.joined})".update

  // TODO: instead of deleting - mark as deleted, by whom and when
  def deleteStream(channelName: ChannelName): Update0 =
    sql"delete from streams where name=${channelName.name}".update

  // TODO: record who did this action
  def partStream(channelName: ChannelName): Update0 =
    sql"update streams set joined=false where name=${channelName.name}".update

  // TODO: record who did this action
  def joinStream(channelName: ChannelName): Update0 =
    sql"update streams set joined=true where name=${channelName.name}".update
}

object StreamsDb extends Streams[ConnectionIO] {
  def getStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getAllStreams.stream
  def markParted(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.partStream(channelName).run.map(_ > 0)
  def markJoined(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.joinStream(channelName).run.map(_ > 0)
  def insertStream(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.insertStream(Stream(channelName, joined = false)).run.map(_ > 0)
  def doesStreamExist(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.doesStreamExist(channelName).unique
  def getAllStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getAllStreams.stream
  def getJoinedStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getJoinedStreams.stream
}
