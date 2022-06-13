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
      case GET -> Root / "quote" / stream / IntVar(qid) =>
        for {
          quote <- Q.getQuote(stream, qid)
          resp: Response[F] <- Ok(quote.getOrElse(Quote(None, -1, "No quote found", "", 1)))
        } yield resp

      case GET -> Root / "quotes" / stream => Ok(Q.getQuotes(stream))
    }
  }

  def inspectionRoutes[F[_]: Sync](I: Inspections[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "inspect" / "streams" / "joined" => Ok(I.getAllStreams)
      case GET -> Root / "inspect" / "streams" => Ok(I.getAllStreams)
      case GET -> Root / "inspect" / "quotes" / "count" / streamName =>
        Ok(I.countQuotesInStream(streamName))
      case GET -> Root / "inspect" / "quotes" / "count" =>
        Ok(I.countQuotes)
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
