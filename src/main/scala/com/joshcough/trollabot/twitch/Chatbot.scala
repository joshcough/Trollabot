package com.joshcough.trollabot.twitch

import cats.effect.{Async, IO}
import cats.implicits.catsSyntaxApply
import com.joshcough.trollabot.api.Api
import com.joshcough.trollabot.{ChannelName, Configuration}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Pure
import fs2.io.net.Network
import logstage.strict.LogIOStrict

object Chatbot {

  def streamFromConfig(
      config: Configuration
  )(implicit L: LogIOStrict[IO]): fs2.Stream[IO, Message] =
    streamFromDb(config.irc, config.xa)

  def streamFromDb(ircConfig: IrcConfig, xa: Transactor[IO])(implicit
      L: LogIOStrict[IO]
  ): fs2.Stream[IO, Message] =
    Chatbot[IO](ircConfig, xa).stream(Api.db)

  def streamFromDefaultConfig(implicit L: LogIOStrict[IO]): fs2.Stream[IO, Message] =
    fs2.Stream.eval(Configuration.read()).flatMap {
      case Left(_)       => fs2.Stream.empty
      case Right(config) => streamFromConfig(config)
    }
}

case class Chatbot[F[_]: Async: Network](ircConfig: IrcConfig, xa: Transactor[F]) {

  def join(channelName: ChannelName): fs2.Stream[Pure, OutgoingMessage] =
    fs2.Stream(
      Irc.join(channelName),
      Irc.privMsg(channelName, s"Hola mujeres!")
    )

  def stream(api: Api[ConnectionIO])(implicit L: LogIOStrict[F]): fs2.Stream[F, Message] = {
    val joinMessages: fs2.Stream[F, OutgoingMessage] =
      api.streams.getJoinedStreams.flatMap(s => join(s.name)).transact(xa)

    Irc(ircConfig, joinMessages)(cm =>
      CommandRunner(Commands.commands).processMessage(cm, xa).flatMap {
        case RespondWith(s)   => fs2.Stream(Irc.privMsg(cm.channel, s))
        case Join(streamName) => join(streamName)
        case Part             => fs2.Stream(Irc.part(cm.channel))
        case LogErr(msg)      => fs2.Stream.eval(L.debug(s"$msg")) *> fs2.Stream.empty
      }
    ).stream.through(fs2.io.stdoutLines())
  }
}
