package com.joshcough.trollabot

import cats.Show
import io.circe.Decoder.Result
import io.circe.{Codec, Decoder, Encoder, HCursor, Json, derivation}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import logstage.LogstageCodec
import logstage.circe.LogstageCirceCodec

import java.sql.Timestamp

object TimestampInstances {
  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] =
    new Encoder[Timestamp] with Decoder[Timestamp] {
      override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)
      override def apply(c: HCursor): Result[Timestamp] =
        Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
    }
}

case class ChatUserName(name: String) extends AnyVal
case class ChatUser(
    username: ChatUserName,
    isMod: Boolean,
    subscriber: Boolean,
    badges: Map[String, String]
)
case class ChannelName(name: String) extends AnyVal

object ChatUserName {
  implicit val circeCodec: Codec[ChatUserName] = derivation.deriveCodec[ChatUserName]
  implicit val logstageCodec: LogstageCodec[ChatUserName] = LogstageCirceCodec.derived[ChatUserName]
}

object ChatUser {
  implicit val circeCodec: Codec[ChatUser] = derivation.deriveCodec[ChatUser]
  implicit val logstageCodec: LogstageCodec[ChatUser] = LogstageCirceCodec.derived[ChatUser]
}

object ChannelName {
  implicit val circeCodec: Codec[ChannelName] = derivation.deriveCodec[ChannelName]
  implicit val logstageCodec: LogstageCodec[ChannelName] = LogstageCirceCodec.derived[ChannelName]
}

case class Stream(id: Option[Int], name: ChannelName, joined: Boolean)

case class Quote(
    id: Option[Int],
    qid: Int,
    text: String,
    channel: Int,
    addedBy: ChatUserName,
    addedAt: Timestamp,
    deleted: Boolean,
    deletedBy: Option[ChatUserName],
    deletedAt: Option[Timestamp]
) {
  def display: String = s"Quote #$qid: $text"
}

case class CounterName(name: String) extends AnyVal

object CounterName {
  implicit val circeCodec: Codec[CounterName] = derivation.deriveCodec[CounterName]
  implicit val logstageCodec: LogstageCodec[CounterName] = LogstageCirceCodec.derived[CounterName]
}

case class Counter(
    id: Option[Int],
    name: CounterName,
    count: Int,
    channel: Int,
    addedBy: ChatUserName,
    addedAt: Timestamp
)

case class Score(
    id: Option[Int],
    channel: Int,
    player1: Option[String],
    player2: Option[String],
    player1Score: Int,
    player2Score: Int
) {
  def display: String =
    s"${player1.getOrElse("player")} $player1Score - $player2Score ${player2.getOrElse("opponent")}"
}

import TimestampInstances._

object Quote {
  implicit val quoteDecoder: Decoder[Quote] = deriveDecoder[Quote]
  implicit val quoteEncoder: Encoder[Quote] = deriveEncoder[Quote]
  implicit val quoteShow: Show[Quote] = Show.fromToString
}

object Stream {
  implicit val streamDecoder: Decoder[Stream] = deriveDecoder[Stream]
  implicit val streamEncoder: Encoder[Stream] = deriveEncoder[Stream]
}

object Counter {
  implicit val counterDecoder: Decoder[Counter] = deriveDecoder[Counter]
  implicit val counterEncoder: Encoder[Counter] = deriveEncoder[Counter]
}

object Score {
  val empty = Score(None, 0, None, None, 0, 0)
  implicit val scoreDecoder: Decoder[Score] = deriveDecoder[Score]
  implicit val scoreEncoder: Encoder[Score] = deriveEncoder[Score]
}
