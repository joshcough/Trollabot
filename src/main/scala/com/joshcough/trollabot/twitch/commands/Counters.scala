package com.joshcough.trollabot.twitch.commands

import cats.implicits._
import cats.Monad
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.api.{Api, CounterName}
import com.joshcough.trollabot.{ChannelName, ChatUser}
import fs2.{Pure, Stream}

object Counters {

  lazy val counterCommands: List[BotCommand] = List(addCounterCommand, incCounterCommand)

  // TODO: eventually we want this: // !commandName ${c} words words ${c++} words words ${++c} words.
  case class AddCounterAction(
      channelName: ChannelName,
      chatUser: ChatUser,
      counterName: CounterName
  ) extends Action {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      addCounter(api)(channelName, chatUser, counterName)
  }
  case class IncCounterAction(channelName: ChannelName, counterName: CounterName) extends Action {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      incCounter(api)(channelName, counterName)
  }

  val counterNameParser: Parser[CounterName] = anyStringAs("counter name").map(CounterName(_))

  val addCounterCommand: BotCommand =
    BotCommand[CounterName, AddCounterAction]("!addCounter", counterNameParser, _ => God)(
      (channelName, chatUser, name) => AddCounterAction(channelName, chatUser, name)
    )

  val incCounterCommand: BotCommand =
    BotCommand[CounterName, IncCounterAction]("!inc", counterNameParser, _ => Anyone)(
      (channelName, _, name) => IncCounterAction(channelName, name)
    )

  def addCounter[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      chatUser: ChatUser,
      counterName: CounterName
  ): Stream[F, Response] =
    Stream
      .eval(
        api.counters
          .insertCounter(channelName, chatUser, counterName)
          .map(c => RespondWith(s"Ok I added it. ${c.name.name}:${c.count}"))
      )
      .handleErrorWith { e =>
        errHandler(e, s"I couldn't add counter for ${counterName.name} stream ${channelName.name}")
      }

  def incCounter[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      counterName: CounterName
  ): Stream[F, Response] =
    Stream
      .eval(
        api.counters
          .incrementCounter(channelName, counterName)
          .map{
            case Some(c) => RespondWith(s"Ok I incremented it. ${c.name.name}:${c.count}")
            case None => RespondWith(s"Ok I couldn't find counter: ${counterName.name}, man.")
          }
      )

  private def err(msg: String): String = s"Something went wrong! $msg. Somebody tell @artofthetroll"

  private def errHandler(e: Throwable, msg: String): Stream[Pure, Response] =
    Stream.emits(List[Response](RespondWith(err(msg)), LogErr(e)))

}
