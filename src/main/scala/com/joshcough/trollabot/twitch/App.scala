package com.joshcough.trollabot.twitch

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.{Configuration, Logging, LoggingInstances}
import doobie.Transactor
import logstage.strict.LogIOStrict

object App extends IOApp {
  implicit val logger: LogIOStrict[IO] = Logging.impl[IO](LoggingInstances.productionLogger)

  def streamFromConfig(config: Configuration): fs2.Stream[IO, Message] =
    streamFromDb(config.irc, Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl))

  def streamFromDb(ircConfig: IrcConfig, xa: Transactor[IO]): fs2.Stream[IO, Message] =
    Chatbot[IO](ircConfig, xa).stream

  def streamFromDefaultConfig: fs2.Stream[IO, Message] =
    fs2.Stream.eval(Configuration.read()).flatMap {
      case Left(_) => fs2.Stream.empty
      case Right(config) => streamFromConfig(config)
    }

  override def run(args: List[String]): IO[ExitCode] =
    streamFromDefaultConfig.through(fs2.io.stdoutLines()).compile.drain.as(ExitCode.Success)
}
