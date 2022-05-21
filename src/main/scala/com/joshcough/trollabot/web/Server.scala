package com.joshcough.trollabot.web

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import com.joshcough.trollabot.TrollabotDb
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object Server {

  def stream[F[_]: Async](db: TrollabotDb[F]): Stream[F, Nothing] = {
    val httpApp = (
      Routes.healthRoutes[F](HealthCheck.impl[F]) <+>
        Routes.quoteRoutes[F](Quotes.impl[F](db))
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
