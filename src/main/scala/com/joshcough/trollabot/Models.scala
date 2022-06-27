package com.joshcough.trollabot

import cats.Show
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.sql.Timestamp

object TimestampInstances {
  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] =
    new Encoder[Timestamp] with Decoder[Timestamp] {
      override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)
      override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
    }
}

case class Stream(id: Option[Int], name: String, joined: Boolean)

case class Quote(
    id: Option[Int],
    qid: Int,
    text: String,
    channel: Int,
    addedBy: String,
    addedAt: Timestamp,
    deleted: Boolean,
    deletedBy: Option[String],
    deletedAt: Option[Timestamp]
) {
  def display: String = s"Quote #$qid: $text"
}

case class Counter(id: Option[Int], name: String, count: Int, channel: Int, addedBy: String, addedAt: Timestamp)

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
