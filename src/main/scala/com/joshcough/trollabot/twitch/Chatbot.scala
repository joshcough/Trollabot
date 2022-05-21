package com.joshcough.trollabot.twitch

import cats.effect.IO
import cats.implicits._
import com.joshcough.trollabot.{Configuration, TrollabotDb}

object Chatbot {
  def join(base: IrcBase, streamName: String): IO[Unit] =
    for {
      _ <- base.join(streamName)
      _ <- base.privMsg(streamName, s"Hola mi hombres muy estupido!")
    } yield ()

  def apply(config: Configuration, db: TrollabotDb[IO]): IO[Chatbot] = {
    val commands: Commands = Commands(db)

    for {
      irc <- Irc.connectFromConfig(config)((base, chatMessage) => {
        def handleResponse(chatMessage: ChatMessage, r: Response): IO[Unit] =
          r match {
            case RespondWith(s)   => base.privMsg(chatMessage.channel.name, s)
            case Join(newChannel) => join(base, newChannel)
            case Part             => base.part(chatMessage.channel.name)
          }
        for {
          responses <- commands.findAndRun(chatMessage)
          _ <- responses.map(r => handleResponse(chatMessage, r)).sequence
        } yield ()
      })
    } yield Chatbot(db, irc)
  }
}

case class Chatbot(db: TrollabotDb[IO], irc: Irc) {

  def run(): IO[Unit] =
    for {
      _ <- irc.base.login()
      streams <- db.getJoinedStreams
      _ <- IO(println("Joining these streams: " + streams))
      _ <- streams.traverse(s => Chatbot.join(irc.base, s.name))
      _ <- irc.processMessages()
      _ <- IO(println("Done processing messages, shutting down."))
    } yield ()

  def close(): IO[Unit] =
    for {
      _ <- irc.base.close()
      _ <- IO(println("Trollabot shutting down!"))
    } yield ()
}
