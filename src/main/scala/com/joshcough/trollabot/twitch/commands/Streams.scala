package com.joshcough.trollabot.twitch.commands

import cats.Monad
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.ChannelName
import com.joshcough.trollabot.api.Api
import io.circe.syntax.EncoderOps

object Streams {

  val channelNameParser: Parser[ChannelName] = anyStringAs("channel name").map(ChannelName(_))

  lazy val streamCommands: List[BotCommand] = List(joinCommand, partCommand, printStreamsCommand)

  case object PrintStreamsAction extends Action {
    def run[F[_]: Monad](api: Api[F]): fs2.Stream[F, Response] = printStreams(api)
  }
  // TODO: we should keep track of the user who parted.
  case class PartAction(channelName: ChannelName) extends Action {
    def run[F[_]: Monad](api: Api[F]): fs2.Stream[F, Response] = part(api)(channelName)
  }
  case class JoinAction(newChannelName: ChannelName) extends Action {
    def run[F[_]: Monad](api: Api[F]): fs2.Stream[F, Response] = join(api)(newChannelName)
  }

  val printStreamsCommand: BotCommand =
    BotCommand[Unit, PrintStreamsAction.type]("!printStreams", empty, _ => God)((_, _, _) =>
      PrintStreamsAction
    )

  val partCommand: BotCommand =
    BotCommand[Unit, PartAction]("!part", empty, _ => Streamer)((c, _, _) => PartAction(c))

  val joinCommand: BotCommand =
    BotCommand[ChannelName, JoinAction]("!join", channelNameParser, _ => God)(
      (_, _, newChannelName) => JoinAction(newChannelName)
    )

  def printStreams[F[_]](api: Api[F]): fs2.Stream[F, Response] =
    api.streams.getStreams.map(_.asJson.noSpaces).reduce((l, r) => s"$l,$r").map(RespondWith(_))

  def part[F[_]](api: Api[F])(channelName: ChannelName): fs2.Stream[F, Response] =
    fs2.Stream
      .eval(api.streams.markParted(channelName))
      .flatMap(_ => fs2.Stream(RespondWith("Goodbye cruel world!"), Part))

  def join[F[_]](api: Api[F])(newChannelName: ChannelName): fs2.Stream[F, Response] =
    fs2.Stream
      .eval(api.streams.join(newChannelName))
      .flatMap(_ =>
        fs2.Stream(Join(newChannelName), RespondWith(s"Joining ${newChannelName.name}!"))
      )

}
