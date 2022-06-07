package com.joshcough.trollabot.web

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.Configuration
import doobie.util.transactor.Transactor

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Configuration.read()
      xa: Transactor.Aux[IO, Unit] = Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl)
      exitCode <- Server.stream[IO](xa).compile.drain.as(ExitCode.Success)
    } yield exitCode
}
