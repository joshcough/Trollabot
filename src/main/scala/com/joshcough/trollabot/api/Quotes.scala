package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.ParserCombinators.{int, slurp}
import com.joshcough.trollabot.twitch._
import com.joshcough.trollabot.{ChannelName, ChatUser, ChatUserName, Quote}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.{Pure, Stream}

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

object QuoteQueries {

  import doobie.implicits.javasql._

  def quotesJoinStreams(channelName: ChannelName): Fragment =
    fr"""
      from quotes q
      join streams s on s.id = q.channel
      where s.name = ${channelName.name}
      """

  def getRandomQuoteForStream(channelName: ChannelName): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(channelName) ++ fr"order by random() limit 1").query[Quote]

  val countQuotes: Query0[Int] = sql"select count(*) from quotes".query[Int]
  def countQuotesInStream(channelName: ChannelName): Query0[Int] =
    (fr"select count(*)" ++ quotesJoinStreams(channelName)).query[Int]

  def getAllQuotesForStream(channelName: ChannelName): Query0[Quote] =
    selectQuotes(channelName).query[Quote]

  def searchQuotesForStream(channelName: ChannelName, like: String): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text LIKE $like order by q.qid ASC")
      .queryWithLogHandler[Quote](LogHandler.jdkLogHandler)

  def searchQuotesForStream_Random(channelName: ChannelName, like: String): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text LIKE $like order by random() limit 1")
      .queryWithLogHandler[Quote](LogHandler.jdkLogHandler)

  def getQuoteByQid(channelName: ChannelName, qid: Int): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.qid = $qid").query[Quote]

  def getQuoteByText(channelName: ChannelName, text: String): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text = $text").query[Quote]

  def selectQuotes(channelName: ChannelName): Fragment =
    fr"select q.*" ++ quotesJoinStreams(channelName)

  // TODO: instead of deleting - mark as deleted, by whom and when
  def deleteQuote(channelName: ChannelName, qid: Int): Update0 =
    sql"""delete from quotes q
          using streams s
          where s.id = q.channel and s.name = ${channelName.name} and q.qid = $qid
       """.update

  def nextQidForChannel_(channelName: ChannelName): Fragment =
    fr"select coalesce(max(q.qid) + 1, 0)" ++ quotesJoinStreams(channelName)

  def nextQidForChannel(channelName: ChannelName): Query0[Int] =
    nextQidForChannel_(channelName).query[Int]

  // TODO: what if quote already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertQuote(text: String, username: ChatUserName, channelName: ChannelName): Query0[Quote] =
    (fr"insert into quotes (qid, text, channel, added_by)" ++
      fr"select" ++
      fr"(" ++ nextQidForChannel_(channelName) ++ fr")," ++
      fr"""$text,
             s.id,
             ${username.name}
             from streams s where s.name = ${channelName.name}
             returning *""").query[Quote]

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

  def countQuotes: ConnectionIO[Count] = QuoteQueries.countQuotes.unique.map(Count(_))

  def countQuotesInStream(channelName: ChannelName): ConnectionIO[Count] =
    QuoteQueries.countQuotesInStream(channelName).unique.map(Count(_))
}

object QuoteCommands {

  lazy val quoteCommands: List[BotCommand] = List(
    getQuoteCommand,
    searchQuotesCommand,
    addQuoteCommand,
    delQuoteCommand
  )

  val quotes = QuotesDb

  trait QuoteAction extends Action

  sealed trait GetQuoteAction extends QuoteAction
  case class GetExactQuoteAction(channelName: ChannelName, qid: Int) extends GetQuoteAction {
    override def run: Stream[ConnectionIO, Response] = getExactQuote(channelName, qid)
  }
  case class GetRandomQuoteAction(channelName: ChannelName) extends GetQuoteAction {
    override def run: Stream[ConnectionIO, Response] = getRandomQuote(channelName)
  }
  case class SearchQuotesAction(channelName: ChannelName, like: String) extends QuoteAction {
    override def run: Stream[ConnectionIO, Response] = search(channelName, like)
  }
  case class AddQuoteAction(channelName: ChannelName, chatUser: ChatUser, text: String)
      extends QuoteAction {
    override def run: Stream[ConnectionIO, Response] = addQuote(channelName, chatUser, text)
  }
  case class DelQuoteAction(channelName: ChannelName, n: Int) extends QuoteAction {
    override def run: Stream[ConnectionIO, Response] = deleteQuote(channelName, n)
  }

  val getQuoteCommand: BotCommand =
    BotCommand[Option[Int], GetQuoteAction]("!quote", int.?, _ => Anyone)((channelName, _, mn) =>
      mn.fold[GetQuoteAction](GetRandomQuoteAction(channelName))(n =>
        GetExactQuoteAction(channelName, n)
      )
    )

  val searchQuotesCommand: BotCommand =
    BotCommand[String, SearchQuotesAction]("!search", slurp, _ => Anyone)((channelName, _, like) =>
      SearchQuotesAction(channelName, like)
    )

  val addQuoteCommand: BotCommand =
    BotCommand[String, AddQuoteAction]("!addQuote", slurp, _ => ModOnly)(
      (channelName, chatUser, text) => AddQuoteAction(channelName, chatUser, text)
    )

  val delQuoteCommand: BotCommand =
    BotCommand[Int, DelQuoteAction]("!delQuote", int, _ => ModOnly)((channelName, _, n) =>
      DelQuoteAction(channelName, n)
    )

  def addQuote(
      channelName: ChannelName,
      chatUser: ChatUser,
      text: String
  ): Stream[ConnectionIO, Response] = {
    val q = quotes.insertQuote(text, chatUser.username, channelName).map {
      case Right(q) => RespondWith(q.display)
      case Left(q)  => RespondWith(s"That quote already exists man! It's #${q.qid}")
    }
    def onErr(e: Throwable): Stream[Pure, Response] =
      errHandler(e, s"I couldn't add quote for stream ${channelName.name}")
    Stream.eval(q).handleErrorWith(onErr)
  }

  // TODO: maybe we should mark the quote deleted instead of deleting it
  // and then we could take the user who deleted it too.
  // we could add two new columns to quote: deletedAt and deletedBy
  def deleteQuote(channelName: ChannelName, n: Int): Stream[ConnectionIO, Response] =
    Stream.eval(quotes.deleteQuote(channelName, n).map {
      case true  => RespondWith("Ok I deleted it.")
      case false => RespondWith(err(s"I couldn't delete quote $n for channel ${channelName.name}"))
    })

  private def withQuoteOr(
      foq: ConnectionIO[Option[Quote]],
      msg: String
  ): Stream[ConnectionIO, Response] =
    Stream.eval(foq.map(oq => RespondWith(oq.map(_.display).getOrElse(msg))))

  def getExactQuote(channelName: ChannelName, qid: Int): Stream[ConnectionIO, Response] =
    withQuoteOr(quotes.getQuote(channelName, qid), s"I couldn't find quote #$qid, man.")

  def getRandomQuote(channelName: ChannelName): Stream[ConnectionIO, Response] =
    withQuoteOr(quotes.getRandomQuote(channelName), "I couldn't find any quotes, man.")

  // TODO: this take(1) here is a little sus.
  // What do we really want to return here? Maybe we just want to return a link...
  // or a random one? Who knows.
  def search(channelName: ChannelName, like: String): Stream[ConnectionIO, Response] =
    Stream.eval(quotes.searchQuotes_Random(channelName, like).map {
      case Some(q) => RespondWith(q.display)
      case None    => RespondWith("Couldn't find any quotes that match that.")
    })

  private def err(msg: String): String = s"Something went wrong! $msg. Somebody tell @artofthetroll"

  private def errHandler(e: Throwable, msg: String): Stream[Pure, Response] =
    Stream.emits(List[Response](RespondWith(err(msg)), LogErr(e)))

}
