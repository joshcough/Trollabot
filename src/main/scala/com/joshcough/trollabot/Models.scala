package com.joshcough.trollabot

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

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
