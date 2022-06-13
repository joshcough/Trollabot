package com.joshcough.trollabot.web

import cats.effect.Sync
import cats.implicits._
import com.joshcough.trollabot.Quote
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl

object Routes {

  def quoteRoutes[F[_]: Sync](Q: Quotes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      // TODO: figure out how to type these params as Int, a custom type, or whatever.
      case GET -> Root / "quote" / stream / qid =>
        for {
          quote <- Q.getQuote(stream, qid.toInt)
          resp: Response[F] <- Ok(quote.getOrElse(Quote(None, -1, "No quote found", "", 1)))
        } yield resp

      case GET -> Root / "quotes" / stream => Ok(Q.getQuotes(stream))
    }
  }

  def healthRoutes[F[_]: Sync](H: HealthCheck[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "health" => H.health *> Ok()
    }
  }
}
