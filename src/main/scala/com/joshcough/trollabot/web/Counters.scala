package com.joshcough.trollabot.web

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{Counter, TrollabotDb}
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Counters[F[_]] {
  def getCounters(channelName: String): fs2.Stream[F, Counter]
}

object Counters {
  def apply[F[_]](implicit ev: Counters[F]): Counters[F] = ev

  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Counters[F] =
    (channelName: String) => TrollabotDb.getAllCountersForStream(channelName).transact(xa)
}
