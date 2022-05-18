package com.joshcough.trollabot

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}

case class IrcConfig(token: String, username: String, server: String, port: Int)
case class Configuration(irc: IrcConfig, dbUrl: String, debug: Boolean)

object Configuration {
  def read(): IO[Configuration] = IO {
    val conf: Config = ConfigFactory.load
    val ircToken: String = conf.getString("irc.token")
    val ircUsername: String = conf.getString("irc.username")
    val ircServer: String = conf.getString("irc.server")
    val ircPort: Int = conf.getInt("irc.port")
    val dbUrl: String = conf.getString("db_url")
    val debug: Boolean = conf.getBoolean("debug")
    new Configuration(IrcConfig(ircToken, ircUsername, ircServer, ircPort), dbUrl, debug)
  }
}
