package com.joshcough.trollabot.twitch.commands

import cats.Monad
import com.joshcough.trollabot.api.Api
import com.joshcough.trollabot.{ChannelName, ChatUserName}
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.TimestampInstances._
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._

object Streams {

  val channelNameParser: Parser[ChannelName] = anyStringAs("channel name").map(ChannelName)

  lazy val streamCommands: List[BotCommand] = List(joinCommand, partCommand, printStreamsCommand)

  sealed trait StreamsAction extends Action

  case class PrintStreamsAction() extends StreamsAction {
    def run[F[_]: Monad](api: Api[F]): fs2.Stream[F, Response] = printStreams(api)
  }
  // TODO: we should keep track of the user who parted.
  case class PartAction(channelName: ChannelName) extends StreamsAction {
    def run[F[_]: Monad](api: Api[F]): fs2.Stream[F, Response] = part(api)(channelName)
  }
  case class JoinAction(newChannelName: ChannelName, chatUserName: ChatUserName)
      extends StreamsAction {
    def run[F[_]: Monad](api: Api[F]): fs2.Stream[F, Response] =
      join(api)(newChannelName, chatUserName)
  }

  val printStreamsCommand: BotCommand =
    BotCommand[Unit, PrintStreamsAction]("!printStreams", empty, _ => God)((_, _, _) =>
      PrintStreamsAction()
    )

  val partCommand: BotCommand =
    BotCommand[Unit, PartAction]("!part", empty, _ => Streamer)((c, _, _) => PartAction(c))

  val joinCommand: BotCommand =
    BotCommand[ChannelName, JoinAction]("!join", channelNameParser, _ => God)(
      (_, u, newChannelName) => JoinAction(newChannelName, u.username)
    )

  def printStreams[F[_]](api: Api[F]): fs2.Stream[F, Response] =
    api.streams.getStreams.map(_.asJson.noSpaces).reduce((l, r) => s"$l,$r").map(RespondWith)

  def part[F[_]](api: Api[F])(channelName: ChannelName): fs2.Stream[F, Response] =
    fs2.Stream
      .eval(api.streams.markParted(channelName))
      .flatMap(_ => fs2.Stream(RespondWith("Goodbye cruel world!"), Part))

  def join[F[_]](
      api: Api[F]
  )(newChannelName: ChannelName, username: ChatUserName): fs2.Stream[F, Response] =
    fs2.Stream
      .eval(api.streams.join(newChannelName, username))
      .flatMap(_ =>
        fs2.Stream(Join(newChannelName), RespondWith(s"Joining ${newChannelName.name}!"))
      )

}
