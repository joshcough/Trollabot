package com.joshcough.trollabot

import io.circe.Decoder.Result
import io.circe.{Codec, Decoder, Encoder, HCursor, Json, derivation}
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
