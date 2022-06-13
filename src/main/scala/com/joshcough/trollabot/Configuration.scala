package com.joshcough.trollabot

import cats.effect.{Async, IO}
import com.joshcough.trollabot.twitch.IrcConfig
import com.comcast.ip4s
import doobie.Transactor
import io.circe.Encoder
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.auto._

case class Configuration(irc: IrcConfig, dbUrl: String, debug: Boolean) {
  def xa[M[_]: Async]: Transactor[M] = Transactor.fromDriverManager[M]("org.postgresql.Driver", dbUrl)
}

object Configuration extends Ip4sCodecs {
  def read(): IO[ConfigReader.Result[Configuration]] = IO(ConfigSource.default.load[Configuration])
}

sealed trait Ip4sCodecs {
  implicit val ip4sPortReader: ConfigReader[ip4s.Port] =
    ConfigReader.fromStringOpt(ip4s.Port.fromString)
  implicit val ip4sPortEncoder: Encoder[ip4s.Port] =
    Encoder.encodeInt.contramap(_.value)
}
