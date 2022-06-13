package com.joshcough.trollabot.twitch

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.{Logging, LoggingInstances}
import logstage.strict.LogIOStrict

object App extends IOApp {
  implicit val logger: LogIOStrict[IO] = Logging.impl[IO](LoggingInstances.productionLogger)

  override def run(args: List[String]): IO[ExitCode] =
    Chatbot.streamFromDefaultConfig.compile.drain.as(ExitCode.Success)
}
