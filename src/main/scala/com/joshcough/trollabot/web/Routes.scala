package com.joshcough.trollabot.web

import cats.effect.Sync
import cats.implicits._
import com.joshcough.trollabot.{BuildInfo, ChannelName}
import com.joshcough.trollabot.api.{Api, Counters, HealthCheck, Quotes, Streams}
import com.joshcough.trollabot.TimestampInstances._
import io.circe.generic.auto._
import org.http4s.{BuildInfo => _, _}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object Routes {

  def quoteRoutes[F[_]: Sync](Q: Quotes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "quotes" / stream / IntVar(qid) => Ok(Q.getQuote(ChannelName(stream), qid))
      case GET -> Root / "quotes" / stream / like        => Ok(Q.searchQuotes(ChannelName(stream), like))
      case GET -> Root / "quotes" / stream               => Ok(Q.getQuotes(ChannelName(stream)))
    }
  }

  def counterRoutes[F[_]: Sync](C: Counters[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "counters" / stream => Ok(C.getCounters(ChannelName(stream)))
    }
  }

  def inspectionRoutes[F[_]: Sync](Q: Quotes[F], S: Streams[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "inspect" / "build_info"         => Ok(BuildInfo())
      case GET -> Root / "inspect" / "streams" / "joined" => Ok(S.getJoinedStreams)
      case GET -> Root / "inspect" / "streams"            => Ok(S.getAllStreams)
      case GET -> Root / "inspect" / "quotes" / "count" / stream =>
        Ok(Q.countQuotesInStream(ChannelName(stream)))
      case GET -> Root / "inspect" / "quotes" / "count" => Ok(Q.countQuotes)
    }
  }

  def healthRoutes[F[_]: Sync](H: HealthCheck[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "health" => H.health *> Ok()
    }
  }

  def apply[F[_]: Sync](api: Api[F]) =
    healthRoutes[F](api.healthCheck) <+>
      quoteRoutes[F](api.quotes) <+>
      inspectionRoutes[F](api.quotes, api.streams) <+>
      counterRoutes[F](api.counters)
}

// Could potentially be useful later:
//import io.circe.syntax.EncoderOps
//    def arrayifyStream(s: fs2.Stream[F, Quote]): fs2.Stream[F, String] =
//      fs2.Stream.emit("[") ++ s.map(_.asJson.noSpaces).intersperse(",") ++ fs2.Stream.emit("]")
