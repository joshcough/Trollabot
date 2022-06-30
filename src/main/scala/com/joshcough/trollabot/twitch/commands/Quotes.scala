package com.joshcough.trollabot.twitch.commands

import cats.Monad
import cats.implicits._
import com.joshcough.trollabot.ParserCombinators.{int, slurp}
import com.joshcough.trollabot.api.Api
import com.joshcough.trollabot.{ChannelName, ChatUser, Quote}
import fs2.{Pure, Stream}

object Quotes {

  lazy val quoteCommands: List[BotCommand] = List(
    getQuoteCommand,
    searchQuotesCommand,
    addQuoteCommand,
    delQuoteCommand
  )

  trait QuoteAction extends Action

  sealed trait GetQuoteAction extends QuoteAction
  case class GetExactQuoteAction(channelName: ChannelName, qid: Int) extends GetQuoteAction {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      getExactQuote(api)(channelName, qid)
  }
  case class GetRandomQuoteAction(channelName: ChannelName) extends GetQuoteAction {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      getRandomQuote(api)(channelName)
  }
  case class SearchQuotesAction(channelName: ChannelName, like: String) extends QuoteAction {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] = search(api)(channelName, like)
  }
  case class AddQuoteAction(channelName: ChannelName, chatUser: ChatUser, text: String)
      extends QuoteAction {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      addQuote(api)(channelName, chatUser, text)
  }
  case class DelQuoteAction(channelName: ChannelName, n: Int) extends QuoteAction {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      deleteQuote(api)(channelName, n)
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

  def addQuote[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      chatUser: ChatUser,
      text: String
  ): Stream[F, Response] = {
    val q = api.quotes.insertQuote(text, chatUser.username, channelName).map {
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
  def deleteQuote[F[_]: Monad](api: Api[F])(channelName: ChannelName, n: Int): Stream[F, Response] =
    Stream.eval(api.quotes.deleteQuote(channelName, n).map {
      case true  => RespondWith("Ok I deleted it.")
      case false => RespondWith(err(s"I couldn't delete quote $n for channel ${channelName.name}"))
    })

  private def withQuoteOr[F[_]: Monad](
      foq: F[Option[Quote]],
      msg: String
  ): Stream[F, Response] =
    Stream.eval(foq.map(oq => RespondWith(oq.map(_.display).getOrElse(msg))))

  def getExactQuote[F[_]: Monad](
      api: Api[F]
  )(channelName: ChannelName, qid: Int): Stream[F, Response] =
    withQuoteOr(api.quotes.getQuote(channelName, qid), s"I couldn't find quote #$qid, man.")

  def getRandomQuote[F[_]: Monad](api: Api[F])(channelName: ChannelName): Stream[F, Response] =
    withQuoteOr(api.quotes.getRandomQuote(channelName), "I couldn't find any quotes, man.")

  // TODO: this take(1) here is a little sus.
  // What do we really want to return here? Maybe we just want to return a link...
  // or a random one? Who knows.
  def search[F[_]: Monad](
      api: Api[F]
  )(channelName: ChannelName, like: String): Stream[F, Response] =
    Stream.eval(api.quotes.searchQuotes_Random(channelName, like).map {
      case Some(q) => RespondWith(q.display)
      case None    => RespondWith("Couldn't find any quotes that match that.")
    })

  private def err(msg: String): String = s"Something went wrong! $msg. Somebody tell @artofthetroll"

  private def errHandler(e: Throwable, msg: String): Stream[Pure, Response] =
    Stream.emits(List[Response](RespondWith(err(msg)), LogErr(e)))

}
