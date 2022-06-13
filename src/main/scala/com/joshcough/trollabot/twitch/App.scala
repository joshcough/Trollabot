package com.joshcough.trollabot.twitch

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.LoggingImplicits.productionLogger

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    Chatbot.streamFromDefaultConfig.compile.drain.as(ExitCode.Success)
}
