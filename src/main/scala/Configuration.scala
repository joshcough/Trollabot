import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

object Configuration {
  val conf: Config = ConfigFactory.load
  val debug: Boolean = conf.getBoolean("debug")
  val ircToken: String = conf.getString("twitch.irc.token")
  val ircUsername: String = conf.getString("twitch.irc.username")
  val ircChannel: String = conf.getString("twitch.irc.channel")
  val ircServer: String = conf.getString("twitch.irc.server")
  val ircPort: Int = conf.getInt("twitch.irc.port")
  val ircCapabilities: Option[String] = Try(conf.getString("twitch.irc.capabilities")).toOption
}
