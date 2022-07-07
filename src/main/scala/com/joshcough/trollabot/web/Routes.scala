package com.joshcough.trollabot.web

import cats.effect.Sync
import cats.implicits._
import com.joshcough.trollabot.{BuildInfo, ChannelName}
import com.joshcough.trollabot.api.{
  Api,
  Counters,
  HealthCheck,
  Quotes,
  Scores,
  Streams,
  UserCommands
}
import com.joshcough.trollabot.TimestampInstances._
import io.circe.generic.auto._
import org.http4s.{BuildInfo => _, _}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object Routes {

  def streamRoutes[F[_]: Sync](S: Streams[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "streams"            => Ok(S.getAllStreams)
      case GET -> Root / "streams" / "joined" => Ok(S.getJoinedStreams)
      case GET -> Root / "streams" / stream   => Ok(S.getStreamByName(ChannelName(stream)))
    }
  }

  def quoteRoutes[F[_]: Sync](Q: Quotes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "quotes" / stream / IntVar(qid) => Ok(Q.getQuote(ChannelName(stream), qid))
      case GET -> Root / "quotes" / "count" / stream =>
        Ok(Q.countQuotesInStream(ChannelName(stream)))
      case GET -> Root / "quotes" / "count"       => Ok(Q.countQuotes)
      case GET -> Root / "quotes" / stream / like => Ok(Q.searchQuotes(ChannelName(stream), like))
      case GET -> Root / "quotes" / stream        => Ok(Q.getQuotes(ChannelName(stream)))
    }
  }

  def counterRoutes[F[_]: Sync](C: Counters[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "counters" / stream => Ok(C.getCounters(ChannelName(stream)))
    }
  }

  def scoreRoutes[F[_]: Sync](S: Scores[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "scores" / stream => Ok(S.getScore(ChannelName(stream)))
    }
  }

  def userCommandRoutes[F[_]: Sync](U: UserCommands[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "commands" / stream => Ok(U.getUserCommands(ChannelName(stream)))
    }
  }

  def inspectionRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "inspect" / "build_info" => Ok(BuildInfo())
    }
  }

  def healthRoutes[F[_]: Sync](H: HealthCheck[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "health" => H.health *> Ok()
    }
  }

  def apply[F[_]: Sync](api: Api[F]) = {
    healthRoutes[F](api.healthCheck) <+>
      streamRoutes[F](api.streams) <+>
      quoteRoutes[F](api.quotes) <+>
      scoreRoutes[F](api.scores) <+>
      userCommandRoutes[F](api.userCommands) <+>
      inspectionRoutes[F] <+>
      counterRoutes[F](api.counters)
  }
}

// Could potentially be useful later:
//import io.circe.syntax.EncoderOps
//    def arrayifyStream(s: fs2.Stream[F, Quote]): fs2.Stream[F, String] =
//      fs2.Stream.emit("[") ++ s.map(_.asJson.noSpaces).intersperse(",") ++ fs2.Stream.emit("]")
