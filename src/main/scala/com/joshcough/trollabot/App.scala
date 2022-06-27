package com.joshcough.trollabot

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.joshcough.trollabot.twitch.{Chatbot, Message}
import com.joshcough.trollabot.web.WebServer
import fs2._
import logstage.strict.LogIOStrict

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    streamFromDefaultConfig(LoggingImplicits.productionLogger).compile.drain.as(ExitCode.Success)

  def streamFromDefaultConfig(implicit L: LogIOStrict[IO]): Stream[IO, Message] =
    Stream.eval(Configuration.read()).flatMap {
      case Left(err) =>
        val errMsg = s"Couldn't read configuration ${err.prettyPrint()}"
        logDebugS(errMsg) *> Stream.raiseError[IO](new RuntimeException(errMsg))
      case Right(config) => streamFromConfig(config)
    }

  def streamFromConfig(config: Configuration)(implicit L: LogIOStrict[IO]): Stream[IO, Message] = {
    val xa = config.xa[IO]
    val bot = Chatbot.streamFromDb(config.irc, xa)
    val webapp = WebServer.stream(api.Api(xa))
    logDebugS("=== Welcome to trollabot! ===") *> bot.concurrently(webapp)
  }

  def logDebugS(msg: String)(implicit L: LogIOStrict[IO]): Stream[IO, Unit] = Stream.eval(L.debug(s"$msg"))
}
