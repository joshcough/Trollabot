package com.joshcough.trollabot.web

import cats.effect.Sync
import cats.implicits._
import com.joshcough.trollabot.BuildInfo
import org.http4s.{BuildInfo => _, _}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object Routes {

  def quoteRoutes[F[_]: Sync](Q: Quotes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "quotes" / stream / IntVar(qid) => Ok(Q.getQuote(stream, qid))
      case GET -> Root / "quotes" / stream / like        => Ok(Q.searchQuotes(stream, like))
      case GET -> Root / "quotes" / stream               => Ok(Q.getQuotes(stream))
    }
  }

  def counterRoutes[F[_]: Sync](Q: Counters[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "counters" / stream => Ok(Q.getCounters(stream))
    }
  }

  def inspectionRoutes[F[_]: Sync](I: Inspections[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "inspect" / "build_info"                    => Ok(BuildInfo())
      case GET -> Root / "inspect" / "streams" / "joined"            => Ok(I.getAllStreams)
      case GET -> Root / "inspect" / "streams"                       => Ok(I.getAllStreams)
      case GET -> Root / "inspect" / "quotes" / "count" / streamName => Ok(I.countQuotesInStream(streamName))
      case GET -> Root / "inspect" / "quotes" / "count"              => Ok(I.countQuotes)
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

// Could potentially be useful later:
//import io.circe.syntax.EncoderOps
//    def arrayifyStream(s: fs2.Stream[F, Quote]): fs2.Stream[F, String] =
//      fs2.Stream.emit("[") ++ s.map(_.asJson.noSpaces).intersperse(",") ++ fs2.Stream.emit("]")
