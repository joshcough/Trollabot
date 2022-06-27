package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{Quote, Queries}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Quotes[F[_]] {
  def getQuote(channelName: String, qid: Int): F[Option[Quote]]
  def getRandomQuote(channelName: String): F[Option[Quote]]
  def getQuotes(channelName: String): fs2.Stream[F, Quote]
  def searchQuotes(channelName: String, like: String): fs2.Stream[F, Quote]
  def insertQuote(text: String, username: String, streamName: String): F[Quote]
  def deleteQuote(streamName: String, qid: Int): F[Boolean]

  def countQuotes: F[Count]
  def countQuotesInStream(streamName: String): F[Count]
}

object Quotes {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Quotes[F] =
    new Quotes[F] {
      def getQuote(channelName: String, qid: Int): F[Option[Quote]] =
        QuotesDb.getQuote(channelName, qid).transact(xa)

      def getRandomQuote(channelName: String): F[Option[Quote]] =
        QuotesDb.getRandomQuote(channelName).transact(xa)

      def getQuotes(channelName: String): fs2.Stream[F, Quote] =
        QuotesDb.getQuotes(channelName).transact(xa)

      def searchQuotes(channelName: String, like: String): fs2.Stream[F, Quote] =
        QuotesDb.searchQuotes(channelName, like).transact(xa)

      def insertQuote(text: String, username: String, streamName: String): F[Quote] =
        QuotesDb.insertQuote(text, username, streamName).transact(xa)

      def deleteQuote(streamName: String, qid: Int): F[Boolean] =
        QuotesDb.deleteQuote(streamName, qid).transact(xa)

      def countQuotes: F[Count] = QuotesDb.countQuotes.transact(xa)

      def countQuotesInStream(streamName: String): F[Count] =
        QuotesDb.countQuotesInStream(streamName).transact(xa)
    }
}

object QuotesDb extends Quotes[ConnectionIO] {
  def getQuote(channelName: String, qid: Int): ConnectionIO[Option[Quote]] =
    Queries.getQuoteByQid(channelName, qid).option

  def getRandomQuote(channelName: String): ConnectionIO[Option[Quote]] =
    Queries.getRandomQuoteForStream(channelName).option

  def getQuotes(channelName: String): fs2.Stream[ConnectionIO, Quote] =
    Queries.getAllQuotesForStream(channelName).stream

  def searchQuotes(channelName: String, like: String): fs2.Stream[ConnectionIO, Quote] =
    Queries.searchQuotesForStream(channelName, like).stream

  def insertQuote(text: String, username: String, streamName: String): ConnectionIO[Quote] =
    Queries.insertQuote(text, username, streamName).unique

  def deleteQuote(streamName: String, qid: Int): ConnectionIO[Boolean] =
    Queries.deleteQuote(streamName: String, qid: Int).run.map(_ > 0)

  def countQuotes: ConnectionIO[Count] = Queries.countQuotes.unique.map(Count(_))

  def countQuotesInStream(streamName: String): ConnectionIO[Count] =
    Queries.countQuotesInStream(streamName).unique.map(Count(_))
}
