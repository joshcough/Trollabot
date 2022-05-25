package com.joshcough.trollabot.twitch

import cats.effect.{ExitCode, IO, IOApp}
import com.joshcough.trollabot.{BuildInfo, Configuration, IrcConfig, Logging, LoggingInstances, TrollabotDb}
import doobie.Transactor
import logstage.strict.LogIOStrict

object App extends IOApp {
  implicit val logger: LogIOStrict[IO] = Logging.impl[IO](LoggingInstances.productionLogger)

  def streamFromConfig(config: Configuration): fs2.Stream[IO, Message] =
    streamFromDb(config.irc, Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl))

  def streamFromDb(ircConfig: IrcConfig, xa: Transactor[IO]): fs2.Stream[IO, Message] =
    for {
      m <- Chatbot[IO](ircConfig, TrollabotDb(xa)).stream
      _ <- fs2.Stream.eval(IO(println(s"Message sent: $m")))
    } yield m

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- IO(println(s"Build time: ${BuildInfo.buildTime}"))
    messages <- fs2.Stream.eval(Configuration.read()).flatMap(streamFromConfig).compile.toList
    _ <- IO(println("messages: " + messages))
  } yield ExitCode.Success
}
