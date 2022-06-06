package com.joshcough.trollabot.twitch

import cats.effect.Async
import com.joshcough.trollabot.{IrcConfig, TrollabotDb}
import fs2.Pure
import fs2.io.net.Network
import logstage.strict.LogIOStrict

case class Chatbot[F[_]: Async: Network](ircConfig: IrcConfig, db: TrollabotDb[F]) {

  val commands: Commands[F] = Commands(db)

  def join(streamName: String): fs2.Stream[Pure, OutgoingMessage] =
    fs2.Stream(
      Irc.join(streamName),
      Irc.privMsg(streamName, s"Hola mujeres!")
    )

  def stream(implicit L: LogIOStrict[F]): fs2.Stream[F, Message] = {
    val joinMessages: fs2.Stream[F, OutgoingMessage] = db.getJoinedStreams.flatMap(s => join(s.name))
    Irc(ircConfig, joinMessages)(cm =>
      commands.findAndRun(cm).flatMap {
        case RespondWith(s) => fs2.Stream(Irc.privMsg(cm.channel.name, s))
        case Join(streamName) =>
          for {
            _ <- fs2.Stream.eval(L.debug(s"joining stream? $streamName"))
            j <- join(streamName)
          } yield j
        case Part => fs2.Stream(Irc.part(cm.channel.name))
      }
    ).stream
  }
}
