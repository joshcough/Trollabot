package com.joshcough.trollabot.api

import cats.Show
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.{ChannelName, ChatUserName}
import com.joshcough.trollabot.db.queries.{Quotes => QuoteQueries}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import java.sql.Timestamp

case class Quote(
    qid: Int,
    text: String,
    channel: ChannelName,
    addedBy: ChatUserName,
    addedAt: Timestamp,
    deleted: Boolean,
    deletedBy: Option[ChatUserName],
    deletedAt: Option[Timestamp]
) {
  def display: String = s"Quote #$qid: $text"
}

object Quote {
  implicit val quoteShow: Show[Quote] = Show.fromToString
}

trait Quotes[F[_]] {
  def getQuote(channelName: ChannelName, qid: Int): F[Option[Quote]]
  def getRandomQuote(channelName: ChannelName): F[Option[Quote]]
  def getQuotes(channelName: ChannelName): fs2.Stream[F, Quote]
  def searchQuotes(channelName: ChannelName, like: String): fs2.Stream[F, Quote]
  def searchQuotes_Random(channelName: ChannelName, like: String): F[Option[Quote]]

  /**
    * Tries to insert a new quote, but first checks if a quote with that text already exists.
    * If it does, it returns Left of that quote (which most importantly, contains its qid)
    * If it does not, it returns Right of the new quote.
    * @param text the body of the new quote
    * @param username the user adding the quote
    * @param streamName the stream the quote is being added to.
    * @return Either ExistingQuote NewQuote
    */
  def insertQuote(
      text: String,
      username: ChatUserName,
      channelName: ChannelName
  ): F[Either[Quote, Quote]]
  def deleteQuote(channelName: ChannelName, qid: Int): F[Boolean]

  def countQuotes: F[Count]
  def countQuotesInStream(channelName: ChannelName): F[Count]
}

object Quotes {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Quotes[F] =
    new Quotes[F] {
      def getQuote(channelName: ChannelName, qid: Int): F[Option[Quote]] =
        QuotesDb.getQuote(channelName, qid).transact(xa)

      def getRandomQuote(channelName: ChannelName): F[Option[Quote]] =
        QuotesDb.getRandomQuote(channelName).transact(xa)

      def getQuotes(channelName: ChannelName): fs2.Stream[F, Quote] =
        QuotesDb.getQuotes(channelName).transact(xa)

      def searchQuotes(channelName: ChannelName, like: String): fs2.Stream[F, Quote] =
        QuotesDb.searchQuotes(channelName, like).transact(xa)

      def searchQuotes_Random(channelName: ChannelName, like: String): F[Option[Quote]] =
        QuotesDb.searchQuotes_Random(channelName, like).transact(xa)

      def insertQuote(
          text: String,
          username: ChatUserName,
          channelName: ChannelName
      ): F[Either[Quote, Quote]] =
        QuotesDb.insertQuote(text, username, channelName).transact(xa)

      def deleteQuote(channelName: ChannelName, qid: Int): F[Boolean] =
        QuotesDb.deleteQuote(channelName, qid).transact(xa)

      def countQuotes: F[Count] = QuotesDb.countQuotes.transact(xa)

      def countQuotesInStream(channelName: ChannelName): F[Count] =
        QuotesDb.countQuotesInStream(channelName).transact(xa)
    }
}

object QuotesDb extends Quotes[ConnectionIO] {
  def getQuote(channelName: ChannelName, qid: Int): ConnectionIO[Option[Quote]] =
    QuoteQueries.getQuoteByQid(channelName, qid).option

  def getRandomQuote(channelName: ChannelName): ConnectionIO[Option[Quote]] =
    QuoteQueries.getRandomQuoteForStream(channelName).option

  def getQuotes(channelName: ChannelName): fs2.Stream[ConnectionIO, Quote] =
    QuoteQueries.getAllQuotesForStream(channelName).stream

  def searchQuotes(channelName: ChannelName, like: String): fs2.Stream[ConnectionIO, Quote] =
    QuoteQueries.searchQuotesForStream(channelName, like).stream

  def searchQuotes_Random(channelName: ChannelName, like: String): ConnectionIO[Option[Quote]] =
    QuoteQueries.searchQuotesForStream_Random(channelName, like).option

  def insertQuote(
      text: String,
      username: ChatUserName,
      channelName: ChannelName
  ): ConnectionIO[Either[Quote, Quote]] =
    for {
      o <- QuoteQueries.getQuoteByText(channelName, text).option
      r <- o match {
        case None    => QuoteQueries.insertQuote(text, username, channelName).unique.map(Right(_))
        case Some(q) => Left(q).pure[ConnectionIO]
      }
    } yield r

  def deleteQuote(channelName: ChannelName, qid: Int): ConnectionIO[Boolean] =
    QuoteQueries.deleteQuote(channelName, qid: Int).run.map(_ > 0)

  def countQuotes: ConnectionIO[Count] = QuoteQueries.countQuotes.unique.map(Count)

  def countQuotesInStream(channelName: ChannelName): ConnectionIO[Count] =
    QuoteQueries.countQuotesInStream(channelName).unique.map(Count)
}
