package com.joshcough.trollabot.web

import cats.effect.{Async, IO, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import com.joshcough.trollabot.Configuration
import doobie.util.transactor.Transactor
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object WebServer {

  def streamFromConfig(config: Configuration): fs2.Stream[IO, Nothing] = stream(config.xa[IO])

  def streamFromDefaultConfig: fs2.Stream[IO, Nothing] =
    fs2.Stream.eval(Configuration.read()).flatMap {
      case Left(_) => fs2.Stream.empty
      case Right(config) => streamFromConfig(config)
    }

  def stream[F[_]: Async](xa: Transactor[F]): Stream[F, Nothing] = {
    val httpApp = (
      Routes.healthRoutes[F](HealthCheck.impl[F]) <+>
        Routes.quoteRoutes[F](Quotes.impl[F](xa))
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
