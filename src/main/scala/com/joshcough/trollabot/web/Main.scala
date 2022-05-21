package com.joshcough.trollabot.web

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.{Configuration, TrollabotDb}
import doobie.util.transactor.Transactor

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Configuration.read()
      db: TrollabotDb[IO] = TrollabotDb(Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl))
      exitCode <- Server.stream[IO](db).compile.drain.as(ExitCode.Success)
    } yield exitCode
}
