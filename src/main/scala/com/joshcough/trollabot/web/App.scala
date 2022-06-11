package com.joshcough.trollabot.web

import cats.effect.{ExitCode, IO, IOApp}

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    WebServer.streamFromDefaultConfig.compile.drain.as(ExitCode.Success)
}
