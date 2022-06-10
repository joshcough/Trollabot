package com.joshcough.trollabot.web

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.Configuration
import doobie.util.transactor.Transactor

object App extends IOApp {
  def streamFromConfig(config: Configuration): fs2.Stream[IO, Nothing] =
    streamFromDb(Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl))

  def streamFromDb(xa: Transactor[IO]): fs2.Stream[IO, Nothing] =
    Server.stream[IO](xa)

  def streamFromDefaultConfig: fs2.Stream[IO, Nothing] =
    fs2.Stream.eval(Configuration.read()).flatMap {
      case Left(_) => fs2.Stream.empty
      case Right(config) => streamFromConfig(config)
    }

  override def run(args: List[String]): IO[ExitCode] =
    streamFromDefaultConfig.compile.drain.as(ExitCode.Success)
}
