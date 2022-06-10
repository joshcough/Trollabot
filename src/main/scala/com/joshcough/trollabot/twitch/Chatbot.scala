package com.joshcough.trollabot.twitch

import cats.effect.{Async, IO}
import com.joshcough.trollabot.{Configuration, Logging, LoggingInstances, TrollabotDb}
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Pure
import fs2.io.net.Network
import logstage.strict.LogIOStrict

object Chatbot {
  implicit val logger: LogIOStrict[IO] = Logging.impl[IO](LoggingInstances.productionLogger)

  def streamFromConfig(config: Configuration): fs2.Stream[IO, Message] =
    Chatbot[IO](config.irc, Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl)).stream

  def streamFromDefaultConfig: fs2.Stream[IO, Message] =
    fs2.Stream.eval(Configuration.read()).flatMap {
      case Left(_) => fs2.Stream.empty
      case Right(config) => streamFromConfig(config)
    }
}

case class Chatbot[F[_]: Async: Network](ircConfig: IrcConfig, xa: Transactor[F]) {

  def join(streamName: String): fs2.Stream[Pure, OutgoingMessage] =
    fs2.Stream(
      Irc.join(streamName),
      Irc.privMsg(streamName, s"Hola mujeres!")
    )

  def stream(implicit L: LogIOStrict[F]): fs2.Stream[F, Message] = {
    val joinMessages: fs2.Stream[F, OutgoingMessage] =
      TrollabotDb.getJoinedStreams.flatMap(s => join(s.name)).transact(xa)

    Irc(ircConfig, joinMessages)(cm =>
      CommandRunner(Commands.commands).processMessage(cm, xa).flatMap {
        case RespondWith(s)   => fs2.Stream(Irc.privMsg(cm.channel.name, s))
        case Join(streamName) => join(streamName)
        case Part             => fs2.Stream(Irc.part(cm.channel.name))
      }
    ).stream.through(fs2.io.stdoutLines())
  }
}
