package com.joshcough.trollabot.web

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{Quote, TrollabotDb}
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Quotes[F[_]] {
  def get(channelName: String, qid: Int): F[Option[Quote]]
}

object Quotes {
  def apply[F[_]](implicit ev: Quotes[F]): Quotes[F] = ev

  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Quotes[F] =
    (channelName: String, qid: Int) => TrollabotDb.getQuoteByQid(channelName, qid).transact(xa)
}
