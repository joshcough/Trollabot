package com.joshcough.trollabot

import cats.Show
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.time.Instant

case class BuildInfo(
    name: String,
    version: String,
    scalaVersion: String,
    sbtVersion: String,
    buildTime: Instant,
    commit: Option[String]
)

object BuildInfo {
  implicit val buildInfoDecoder: Decoder[BuildInfo] = deriveDecoder[BuildInfo]
  implicit val buildInfoEncoder: Encoder[BuildInfo] = deriveEncoder[BuildInfo]
  implicit val buildInfoShow: Show[BuildInfo] = Show.fromToString

  private val bi: BuildInfo = {
    val b = com.joshcough.trollabot.raw.BuildInfo
    BuildInfo(b.name, b.version, b.scalaVersion, b.sbtVersion, b.buildTime, b.commit)
  }

  def apply(): BuildInfo = bi
}
