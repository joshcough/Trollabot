package com.joshcough.trollabot.twitch

import cats.effect.Async
import com.joshcough.trollabot.TrollabotDb
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Pure
import fs2.io.net.Network
import logstage.strict.LogIOStrict

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
    ).stream
  }
}
