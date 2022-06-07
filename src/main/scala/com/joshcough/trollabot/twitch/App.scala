package com.joshcough.trollabot.twitch

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.{Configuration, IrcConfig, Logging, LoggingInstances, TrollabotDb}
import doobie.Transactor
import logstage.strict.LogIOStrict

object App extends IOApp {
  implicit val logger: LogIOStrict[IO] = Logging.impl[IO](LoggingInstances.productionLogger)

  def streamFromConfig(config: Configuration): fs2.Stream[IO, Message] =
    streamFromDb(config.irc, Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl))

  def streamFromDb(ircConfig: IrcConfig, xa: Transactor[IO]): fs2.Stream[IO, Message] =
    Chatbot[IO](ircConfig, TrollabotDb(xa)).stream

  override def run(args: List[String]): IO[ExitCode] = {
    val messages = fs2.Stream.eval(Configuration.read()).flatMap(streamFromConfig)
    messages.through(fs2.io.stdoutLines()).compile.drain.as(ExitCode.Success)
  }
}
