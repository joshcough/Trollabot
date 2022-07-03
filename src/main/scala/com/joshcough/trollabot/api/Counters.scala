package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{ChannelName, ChatUser, ChatUserName, TimestampInstances}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Codec, Decoder, Encoder, derivation}
import logstage.LogstageCodec
import logstage.circe.LogstageCirceCodec

import java.sql.Timestamp
import TimestampInstances._

case class CounterName(name: String) extends AnyVal

object CounterName {
  implicit val circeCodec: Codec[CounterName] = derivation.deriveCodec[CounterName]
  implicit val logstageCodec: LogstageCodec[CounterName] = LogstageCirceCodec.derived[CounterName]
}

case class Counter(
    id: Option[Int],
    name: CounterName,
    count: Int,
    channel: ChannelName,
    addedBy: ChatUserName,
    addedAt: Timestamp
)

object Counter {
  implicit val counterDecoder: Decoder[Counter] = deriveDecoder[Counter]
  implicit val counterEncoder: Encoder[Counter] = deriveEncoder[Counter]
}

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

object CounterQueries {

  import doobie.implicits.javasql._

  def insertCounter(
      counterName: CounterName,
      username: ChatUserName,
      channelName: ChannelName
  ): Query0[Counter] =
    sql"""insert into counters (name, current_count, channel, added_by)
          values(${counterName.name}, 0, ${channelName.name}, ${username.name})
          returning *""".query[Counter]

  def counterValue(counterName: CounterName, channelName: ChannelName): Query0[Int] =
    sql"""select current_count from counters
          where channel = ${channelName.name} and name = ${counterName.name}""".query[Int]

  def selectAllCountersForStream(channelName: ChannelName): Query0[Counter] =
    sql"""select * from counters where channel = ${channelName.name}""".query[Counter]

  def incrementCounter(counterName: CounterName, channelName: ChannelName): Query0[Counter] =
    sql"""update counters
          set current_count = current_count + 1
          where channel = ${channelName.name} and name = ${counterName.name}
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

  def incrementCounter(
      channelName: ChannelName,
      counterName: CounterName
  ): ConnectionIO[Option[Counter]] =
    CounterQueries.incrementCounter(counterName, channelName).option
}
