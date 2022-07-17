package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{ChannelName, ChatUser, ChatUserName}
import com.joshcough.trollabot.db.queries.{Counters => CounterQueries}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import java.sql.Timestamp

case class CounterName(name: String) extends AnyVal

case class Counter(
    name: CounterName,
    count: Int,
    channel: ChannelName,
    addedBy: ChatUserName,
    addedAt: Timestamp
)

trait Counters[F[_]] {
  def getCounters(channelName: ChannelName): fs2.Stream[F, Counter]
  def insertCounter(
      channelName: ChannelName,
      chatUser: ChatUser,
      counterName: CounterName
  ): F[Counter]
  def incrementCounter(channelName: ChannelName, counterName: CounterName): F[Option[Counter]]
}

object Counters {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Counters[F] =
    new Counters[F] {
      def getCounters(channelName: ChannelName): fs2.Stream[F, Counter] =
        CountersDb.getCounters(channelName).transact(xa)

      // TODO: this one might want to return an either or something, in case a counter with that name
      // already exists.
      def insertCounter(
          channelName: ChannelName,
          chatUser: ChatUser,
          counterName: CounterName
      ): F[Counter] =
        CountersDb.insertCounter(channelName, chatUser, counterName).transact(xa)

      def incrementCounter(channelName: ChannelName, counterName: CounterName): F[Option[Counter]] =
        CountersDb.incrementCounter(channelName, counterName).transact(xa)
    }
}

object CountersDb extends Counters[ConnectionIO] {
  def getCounters(channelName: ChannelName): fs2.Stream[ConnectionIO, Counter] =
    CounterQueries.selectAllCountersForStream(channelName).stream

  def insertCounter(
      channelName: ChannelName,
      chatUser: ChatUser,
      counterName: CounterName
  ): ConnectionIO[Counter] =
    CounterQueries.insertCounter(counterName, chatUser.username, channelName).unique

  def incrementCounter(
      channelName: ChannelName,
      counterName: CounterName
  ): ConnectionIO[Option[Counter]] =
    CounterQueries.incrementCounter(counterName, channelName).option
}
