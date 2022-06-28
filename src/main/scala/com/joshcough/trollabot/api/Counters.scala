package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{ChannelName, ChatUser, Counter, CounterName, Queries}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Counters[F[_]] {
  def getCounters(channelName: String): fs2.Stream[F, Counter]
  def insertCounter(channelName: ChannelName, chatUser: ChatUser, counterName: CounterName): F[Counter]
  def incrementCounter(channelName: ChannelName, counterName: CounterName): F[Counter]
}

object Counters {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Counters[F] =
    new Counters[F] {
      def getCounters(channelName: String): fs2.Stream[F, Counter] =
        CountersDb.getCounters(channelName).transact(xa)

      // TODO: this one might want to return an either or something, in case a counter with that name
      // already exists.
      def insertCounter(channelName: ChannelName, chatUser: ChatUser, counterName: CounterName): F[Counter] =
        CountersDb.insertCounter(channelName, chatUser, counterName).transact(xa)

      def incrementCounter(channelName: ChannelName, counterName: CounterName): F[Counter] =
        CountersDb.incrementCounter(channelName, counterName).transact(xa)
    }
}

object CountersDb extends Counters[ConnectionIO] {
  def getCounters(channelName: String): fs2.Stream[ConnectionIO, Counter] =
    Queries.selectAllCountersForStream(channelName).stream

  def insertCounter(channelName: ChannelName, chatUser: ChatUser, counterName: CounterName): ConnectionIO[Counter] =
    Queries.insertCounter(counterName, chatUser.username.name, channelName.name).unique

  def incrementCounter(channelName: ChannelName, counterName: CounterName): ConnectionIO[Counter] =
    Queries.incrementCounter(counterName, channelName.name).unique
}
