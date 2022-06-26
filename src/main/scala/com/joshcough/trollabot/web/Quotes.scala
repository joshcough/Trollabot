package com.joshcough.trollabot.web

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{Quote, TrollabotDb}
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Quotes[F[_]] {
  def getQuote(channelName: String, qid: Int): F[Option[Quote]]
  def getQuotes(channelName: String): fs2.Stream[F, Quote]
  def searchQuotes(channelName: String, like: String): fs2.Stream[F, Quote]
}

object Quotes {
  def apply[F[_]](implicit ev: Quotes[F]): Quotes[F] = ev

  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Quotes[F] =
    new Quotes[F] {
      override def getQuote(channelName: String, qid: Int): F[Option[Quote]] =
        TrollabotDb.getQuoteByQid(channelName, qid).transact(xa)

      override def getQuotes(channelName: String): fs2.Stream[F, Quote] =
        TrollabotDb.getAllQuotesForStream(channelName).transact(xa)

      override def searchQuotes(channelName: String, like: String): fs2.Stream[F, Quote] =
        TrollabotDb.searchQuotesForStream(channelName, like).transact(xa)
    }
}
