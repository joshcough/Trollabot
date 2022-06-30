package com.joshcough.trollabot.api

import cats.Monad
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.twitch._
import com.joshcough.trollabot.{ChannelName, Stream}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.syntax._

abstract class Streams[F[_]: Monad] {
  def getStreams: fs2.Stream[F, Stream]
  def markParted(channelName: ChannelName): F[Boolean]
  def markJoined(channelName: ChannelName): F[Boolean]
  def insertStream(channelName: ChannelName): F[Boolean]
  def doesStreamExist(channelName: ChannelName): F[Boolean]
  def getAllStreams: fs2.Stream[F, Stream]
  def getJoinedStreams: fs2.Stream[F, Stream]

  def join(channelName: ChannelName): F[Boolean] =
    for {
      b <- doesStreamExist(channelName)
      z <- if (b) markJoined(channelName) else insertStream(channelName)
    } yield z
}

object Streams {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Streams[F] =
    new Streams[F] {
      def getStreams: fs2.Stream[F, Stream] = StreamsDb.getStreams.transact(xa)
      def markParted(channelName: ChannelName): F[Boolean] =
        StreamsDb.markParted(channelName).transact(xa)
      def markJoined(channelName: ChannelName): F[Boolean] =
        StreamsDb.markJoined(channelName).transact(xa)
      def insertStream(channelName: ChannelName): F[Boolean] =
        StreamsDb.insertStream(channelName).transact(xa)
      def doesStreamExist(channelName: ChannelName): F[Boolean] =
        StreamsDb.doesStreamExist(channelName).transact(xa)
      def getAllStreams: fs2.Stream[F, Stream] = StreamsDb.getAllStreams.transact(xa)
      def getJoinedStreams: fs2.Stream[F, Stream] = StreamsDb.getJoinedStreams.transact(xa)
    }
}

object StreamQueries {

  val getJoinedStreams: Query0[Stream] =
    sql"select * from streams where joined = true".query[Stream]

  val getAllStreams: Query0[Stream] =
    sql"select * from streams".query[Stream]

  def getStreamId(channelName: ChannelName): Query0[Int] =
    sql"select id from streams where name = ${channelName.name}".query[Int]

  def doesStreamExist(channelName: ChannelName): Query0[Boolean] =
    sql"select exists(select id from streams where name = ${channelName.name})".query[Boolean]

  // TODO: what if stream already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertStream(s: Stream): Update0 =
    sql"insert into streams (name, joined) values (${s.name}, ${s.joined})".update

  // TODO: instead of deleting - mark as deleted, by whom and when
  def deleteStream(channelName: ChannelName): Update0 =
    sql"delete from streams where name=${channelName.name}".update

  // TODO: record who did this action
  def partStream(channelName: ChannelName): Update0 =
    sql"update streams set joined=false where name=${channelName.name}".update

  // TODO: record who did this action
  def joinStream(channelName: ChannelName): Update0 =
    sql"update streams set joined=true where name=${channelName.name}".update
}

object StreamsDb extends Streams[ConnectionIO] {
  def getStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getAllStreams.stream
  def markParted(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.partStream(channelName).run.map(_ > 0)
  def markJoined(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.joinStream(channelName).run.map(_ > 0)
  def insertStream(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.insertStream(Stream(None, channelName, joined = false)).run.map(_ > 0)
  def doesStreamExist(channelName: ChannelName): ConnectionIO[Boolean] =
    StreamQueries.doesStreamExist(channelName).unique
  def getAllStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getAllStreams.stream
  def getJoinedStreams: fs2.Stream[ConnectionIO, Stream] = StreamQueries.getJoinedStreams.stream
}

object StreamCommands {

  val channelNameParser: Parser[ChannelName] = anyStringAs("channel name").map(ChannelName(_))

  lazy val streamCommands: List[BotCommand] = List(joinCommand, partCommand, printStreamsCommand)

  case object PrintStreamsAction extends Action {
    def run: fs2.Stream[ConnectionIO, Response] = printStreams
  }
  // TODO: we should keep track of the user who parted.
  case class PartAction(channelName: ChannelName) extends Action {
    def run: fs2.Stream[ConnectionIO, Response] = part(channelName)
  }
  case class JoinAction(newChannelName: ChannelName) extends Action {
    def run: fs2.Stream[ConnectionIO, Response] = join(newChannelName)
  }

  val printStreamsCommand: BotCommand =
    BotCommand[Unit, PrintStreamsAction.type]("!printStreams", empty, _ => God)((_, _, _) =>
      PrintStreamsAction
    )

  val partCommand: BotCommand =
    BotCommand[Unit, PartAction]("!part", empty, _ => Owner)((c, _, _) => PartAction(c))

  val joinCommand: BotCommand =
    BotCommand[ChannelName, JoinAction]("!join", channelNameParser, _ => God)(
      (_, _, newChannelName) => JoinAction(newChannelName)
    )

  def printStreams: fs2.Stream[ConnectionIO, Response] =
    StreamsDb.getStreams.map(_.asJson.noSpaces).reduce((l, r) => s"$l,$r").map(RespondWith(_))

  def part(channelName: ChannelName): fs2.Stream[ConnectionIO, Response] =
    fs2.Stream
      .eval(StreamsDb.markParted(channelName))
      .flatMap(_ => fs2.Stream(RespondWith("Goodbye cruel world!"), Part))

  def join(newChannelName: ChannelName): fs2.Stream[ConnectionIO, Response] =
    fs2.Stream
      .eval(StreamsDb.join(newChannelName))
      .flatMap(_ =>
        fs2.Stream(Join(newChannelName), RespondWith(s"Joining ${newChannelName.name}!"))
      )

}
