package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{ChannelName, ChatUser, ChatUserName, Counter, CounterName}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Counters[F[_]] {
  def getCounters(channelName: ChannelName): fs2.Stream[F, Counter]
  def insertCounter(
      channelName: ChannelName,
      chatUser: ChatUser,
      counterName: CounterName
  ): F[Counter]
  def incrementCounter(channelName: ChannelName, counterName: CounterName): F[Counter]
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

      def incrementCounter(channelName: ChannelName, counterName: CounterName): F[Counter] =
        CountersDb.incrementCounter(channelName, counterName).transact(xa)
    }
}

object CounterQueries {

  import doobie.implicits.javasql._

  def countersJoinStreams(counterName: CounterName, channelName: ChannelName): Fragment =
    fr"""
      from counters c
      join streams s on s.id = c.channel
      where s.name = ${channelName.name} and c.name = ${counterName.name}
      """

  def insertCounter(
      counterName: CounterName,
      username: ChatUserName,
      channelName: ChannelName
  ): Query0[Counter] =
    sql"""insert into counters (name, current_count, channel, added_by)
          select ${counterName.name}, 0, s.id, ${username.name}
          from streams s where s.name = ${channelName.name}
          returning *""".query[Counter]

  def counterValue(counterName: CounterName, channelName: ChannelName): Query0[Int] =
    (sql"select c.current_count" ++ countersJoinStreams(counterName, channelName)).query[Int]

  def selectAllCountersForStream(channelName: ChannelName): Query0[Counter] =
    sql"""select c.* from counters c join streams s
          on s.id = c.channel
          where s.name = ${channelName.name}""".query[Counter]

  def incrementCounter(counterName: CounterName, channelName: ChannelName): Query0[Counter] =
    sql"""update counters c
          set current_count = current_count + 1
          from streams s
          where c.channel = s.id and c.name = ${counterName.name} and s.name = ${channelName.name}
          returning *""".query[Counter]

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

  def incrementCounter(channelName: ChannelName, counterName: CounterName): ConnectionIO[Counter] =
    CounterQueries.incrementCounter(counterName, channelName).unique
}
