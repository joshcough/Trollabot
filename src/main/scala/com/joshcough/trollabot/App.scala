package com.joshcough.trollabot

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.joshcough.trollabot.twitch.{Chatbot, Message}
import com.joshcough.trollabot.web.WebServer
import fs2._
import logstage.strict.LogIOStrict

object App extends IOApp {
  implicit val logger: LogIOStrict[IO] = Logging.impl[IO](LoggingInstances.productionLogger)

  override def run(args: List[String]): IO[ExitCode] =
    streamFromDefaultConfig.compile.drain.as(ExitCode.Success)

  def streamFromDefaultConfig: Stream[IO, Message] =
    Stream.eval(Configuration.read()).flatMap {
      case Left(err) =>
        val errMsg = s"Couldn't read configuration ${err.prettyPrint()}"
        logDebugS(errMsg) *> Stream.raiseError[IO](new RuntimeException(errMsg))
      case Right(config) => streamFromConfig(config)
    }

  def streamFromConfig(config: Configuration): Stream[IO, Message] = {
    val xa = config.xa[IO]
    val bot = Chatbot.streamFromDb(config.irc, xa)
    val webapp = WebServer.stream(xa)
    logDebugS("=== Welcome to trollabot! ===") *> bot.concurrently(webapp)
  }

  def logDebugS(msg: String): Stream[IO, Unit] = Stream.eval(logger.debug(s"$msg"))
}
