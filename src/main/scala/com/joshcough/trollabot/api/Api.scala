package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import doobie.{ConnectionIO, Transactor}
import io.circe.Encoder
import io.circe.derivation.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class Count(i: Int)
object Count {
  implicit val countEncoder: Encoder[Count] = deriveEncoder[Count]
  implicit def countEntityEncoder[F[_]]: EntityEncoder[F, Count] = jsonEncoderOf
}

case class Api[F[_]](
    streams: Streams[F],
    quotes: Quotes[F],
    counters: Counters[F],
    healthCheck: HealthCheck[F],
    scores: Scores[F]
)

object Api {
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): Api[F] =
    new Api[F](
      Streams.impl(xa),
      Quotes.impl(xa),
      Counters.impl(xa),
      HealthCheck.impl,
      Scores.impl(xa)
    )

  def db: Api[ConnectionIO] =
    new Api[ConnectionIO](StreamsDb, QuotesDb, CountersDb, HealthCheckDb, ScoresDb)
}
