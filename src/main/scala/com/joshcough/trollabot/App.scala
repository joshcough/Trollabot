package com.joshcough.trollabot

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.twitch.Chatbot
import com.joshcough.trollabot.web.WebServer

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    fs2.Stream.eval(Configuration.read()).flatMap {
      case Left(_) => fs2.Stream.empty
      case Right(config) =>
        val xa = config.xa[IO]
        val bot = Chatbot.streamFromDb(config.irc, xa)
        val webapp = WebServer.stream(xa)
        bot.concurrently(webapp)
    }.compile.drain.as(ExitCode.Success)
}
