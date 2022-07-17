package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import doobie.{ConnectionIO, Transactor}

case class Count(i: Int)

case class Api[F[_]](
    streams: Streams[F],
    quotes: Quotes[F],
    counters: Counters[F],
    healthCheck: HealthCheck[F],
    scores: Scores[F],
    userCommands: UserCommands[F]
)

object Api {
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): Api[F] =
    new Api[F](
      Streams.impl(xa),
      Quotes.impl(xa),
      Counters.impl(xa),
      HealthCheck.impl,
      Scores.impl(xa),
      UserCommands.impl(xa)
    )

  def db: Api[ConnectionIO] =
    new Api[ConnectionIO](StreamsDb, QuotesDb, CountersDb, HealthCheckDb, ScoresDb, UserCommandsDb)
}
