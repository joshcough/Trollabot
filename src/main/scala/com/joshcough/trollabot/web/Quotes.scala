package com.joshcough.trollabot.web

import com.joshcough.trollabot.{Quote, TrollabotDb}

trait Quotes[F[_]] {
  def get(channelName: String, qid: Int): F[Option[Quote]]
}

object Quotes {
  def apply[F[_]](implicit ev: Quotes[F]): Quotes[F] = ev

  final case class QuoteError(e: Throwable) extends RuntimeException

  def impl[F[_]](db: TrollabotDb[F]): Quotes[F] =
    (channelName: String, qid: Int) => db.getQuoteByQid(channelName, qid)
}
