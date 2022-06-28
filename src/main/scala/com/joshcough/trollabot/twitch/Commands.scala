package com.joshcough.trollabot.twitch

import com.joshcough.trollabot.{BuildInfo, ChannelName, ChatUser, CounterName, Quote}
import ParserCombinators._
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.api.Api
import doobie.ConnectionIO
import doobie.implicits.toDoobieStreamOps
import doobie.util.transactor.Transactor
import fs2.{Pure, Stream}
import io.circe.{Codec, Decoder, Encoder}
import io.circe.derivation
import io.circe.generic.auto._
import io.circe.syntax._
import logstage.LogstageCodec
import logstage.circe.LogstageCirceCodec
import logstage.strict.LogIOStrict

trait Permission
case object God extends Permission
case object Owner extends Permission
case object ModOnly extends Permission
case object Anyone extends Permission

sealed trait Response

object Response {
  implicit val encodeResponse: Encoder[Response] = Encoder.instance {
    case r @ RespondWith(_) => r.asJson
    case r @ Join(_)        => r.asJson
    case _ @Part            => "Part".asJson // TODO: is this correct, or BS?
    case r @ LogErr(_)      => r.asJson
  }
  implicit val decodeResponse: Decoder[Response] =
    List[Decoder[Response]](
      Decoder[RespondWith].widen,
      Decoder[Join].widen,
      Decoder[Part.type].widen,
      Decoder[LogErr].widen
    ).reduceLeft(_ or _)
  implicit val logstageCodec: LogstageCodec[Response] = LogstageCirceCodec.derived[Response]
}

case class RespondWith(s: String) extends Response
case class Join(newChannel: ChannelName) extends Response
case object Part extends Response {
  implicit val circeCodec: Codec[Part.type] = derivation.deriveCodec[Part.type]
  implicit val logstageCodec: LogstageCodec[Part.type] = LogstageCirceCodec.derived[Part.type]
}
case class LogErr(err: String) extends Response

object LogErr {
  def apply(t: Throwable): LogErr = LogErr(t.getMessage + "\n" + t.getStackTrace.mkString(","))
}

object RespondWith {
  implicit val circeCodec: Codec[RespondWith] = derivation.deriveCodec[RespondWith]
  implicit val logstageCodec: LogstageCodec[RespondWith] = LogstageCirceCodec.derived[RespondWith]
}

object Join {
  implicit val circeCodec: Codec[Join] = derivation.deriveCodec[Join]
  implicit val logstageCodec: LogstageCodec[Join] = LogstageCirceCodec.derived[Join]
}

sealed trait Action
case object PrintStreamsAction extends Action
case class GetExactQuoteAction(channelName: ChannelName, qid: Int) extends Action
case class SearchQuotesAction(channelName: ChannelName, like: String) extends Action
case class GetRandomQuoteAction(channelName: ChannelName) extends Action
case class AddQuoteAction(channelName: ChannelName, chatUser: ChatUser, text: String) extends Action
case class DelQuoteAction(channelName: ChannelName, n: Int) extends Action
// TODO: we should keep track of the user who parted.
case class PartAction(channelName: ChannelName) extends Action
case class JoinAction(newChannelName: ChannelName) extends Action
// TODO: eventually we want this: // !commandName ${c} words words ${c++} words words ${++c} words.
case class AddCounterAction(channelName: ChannelName, chatUser: ChatUser, counterName: CounterName)
    extends Action
case class IncCounterAction(channelName: ChannelName, counterName: CounterName) extends Action
case class HelpAction(commandName: String) extends Action
case object BuildInfoAction extends Action

object Action {
  implicit val encodeResponse: Encoder[Action] = Encoder.instance {
    case _ @PrintStreamsAction         => "PrintStreamsAction".asJson
    case r @ GetExactQuoteAction(_, _) => r.asJson
    case r @ SearchQuotesAction(_, _)  => r.asJson
    case r @ GetRandomQuoteAction(_)   => r.asJson
    case r @ AddQuoteAction(_, _, _)   => r.asJson
    case r @ DelQuoteAction(_, _)      => r.asJson
    case r @ PartAction(_)             => r.asJson
    case r @ JoinAction(_)             => r.asJson
    case r @ AddCounterAction(_, _, _) => r.asJson
    case r @ IncCounterAction(_, _)    => r.asJson
    case r @ HelpAction(_)             => r.asJson
    case _ @BuildInfoAction            => "BuildInfoAction".asJson
  }
  implicit val decodeResponse: Decoder[Action] =
    List[Decoder[Action]](
      Decoder[PrintStreamsAction.type].widen,
      Decoder[GetExactQuoteAction].widen,
      Decoder[SearchQuotesAction].widen,
      Decoder[GetRandomQuoteAction].widen,
      Decoder[AddQuoteAction].widen,
      Decoder[DelQuoteAction].widen,
      Decoder[PartAction].widen,
      Decoder[JoinAction].widen,
      Decoder[AddCounterAction].widen,
      Decoder[IncCounterAction].widen,
      Decoder[HelpAction].widen,
      Decoder[BuildInfoAction.type].widen
    ).reduceLeft(_ or _)
  implicit val logstageCodec: LogstageCodec[Action] = LogstageCirceCodec.derived[Action]
}

trait BotCommand {
  type A
  val name: String
  val permission: Permission
  val parser: Parser[A]
  override def toString: String = s"$name ${parser.describe} (permissions: $permission)"
  def help: String = toString // TODO: maybe we change this around later, but its fine for now.
  def execute(channelName: ChannelName, chatUser: ChatUser, a: A): Action
  def apply(channelName: ChannelName, chatUser: ChatUser, args: String): Either[String, Action] =
    parser(args.trim).toEither.map(a => execute(channelName, chatUser, a))
}

object BotCommand {
  def apply[T](cmdName: String, cmdParser: Parser[T], perm: Permission)(
      f: (ChannelName, ChatUser, T) => Action
  ): BotCommand =
    new BotCommand {
      override type A = T
      val name: String = cmdName
      val permission: Permission = perm
      val parser: Parser[T] = cmdParser
      def execute(channelName: ChannelName, chatUser: ChatUser, t: T): Action =
        f(channelName, chatUser, t)
    }
}

case class CommandInterpreter(api: Api[ConnectionIO]) {
  import api._

  def interpret(a: Action): Stream[ConnectionIO, Response] =
    a match {
      case PrintStreamsAction                          => printStreams
      case GetExactQuoteAction(channelName, qid)       => getExactQuote(channelName, qid)
      case GetRandomQuoteAction(channelName)           => getRandomQuote(channelName)
      case SearchQuotesAction(channelName, like)       => search(channelName, like)
      case AddQuoteAction(channelName, chatUser, text) => addQuote(channelName, chatUser, text)
      case DelQuoteAction(channelName, qid)            => deleteQuote(channelName, qid)
      case PartAction(channelName)                     => part(channelName)
      case JoinAction(newChannelName)                  => join(newChannelName)
      case AddCounterAction(channel, user, counter)    => addCounter(channel, user, counter)
      case IncCounterAction(channel, counter)          => incCounter(channel, counter)
      case HelpAction(commandName)                     => help(commandName, Commands.commands)
      case BuildInfoAction                             => buildInfo
    }

  def printStreams: Stream[ConnectionIO, Response] =
    streams.getStreams.map(_.asJson.noSpaces).reduce((l, r) => s"$l,$r").map(RespondWith(_))

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

  def part(channelName: ChannelName): Stream[ConnectionIO, Response] =
    Stream
      .eval(streams.markParted(channelName))
      .flatMap(_ => Stream(RespondWith("Goodbye cruel world!"), Part))

  def join(newChannelName: ChannelName): Stream[ConnectionIO, Response] =
    Stream
      .eval(streams.join(newChannelName))
      .flatMap(_ => Stream(Join(newChannelName), RespondWith(s"Joining ${newChannelName.name}!")))

  def addCounter(
      channelName: ChannelName,
      chatUser: ChatUser,
      counterName: CounterName
  ): Stream[ConnectionIO, Response] =
    Stream
      .eval(
        counters
          .insertCounter(channelName, chatUser, counterName)
          .map(c => RespondWith(s"Ok I added it. ${c.name.name}:${c.count}"))
      )
      .handleErrorWith { e =>
        errHandler(e, s"I couldn't add counter for ${counterName.name} stream ${channelName.name}")
      }

  def incCounter(
      channelName: ChannelName,
      counterName: CounterName
  ): Stream[ConnectionIO, Response] =
    Stream
      .eval(
        counters
          .incrementCounter(channelName, counterName)
          .map(c => RespondWith(s"Ok I incremented it. ${c.name.name}:${c.count}"))
      )
      .handleErrorWith { e =>
        errHandler(
          e,
          s"I couldn't increment counter for ${counterName.name} stream ${channelName.name}"
        )
      }

  def help(
      commandName: String,
      knownCommands: Map[String, BotCommand]
  ): Stream[ConnectionIO, Response] =
    Stream.emit(RespondWith(knownCommands.get(commandName) match {
      case None    => s"Unknown command: $commandName"
      case Some(c) => c.toString
    }))

  val buildInfo: Stream[ConnectionIO, Response] =
    Stream.emit(RespondWith(BuildInfo().asJson.noSpaces))

  // returns true if the given user is allowed to run the command on the given channel
  // false if not.
  def checkPermissions(cmd: BotCommand, chatUser: ChatUser, channelName: ChannelName): Boolean = {
    def isStreamer = chatUser.username.name.toLowerCase == channelName.name.toLowerCase
    def isGod: Boolean = chatUser.username.name.toLowerCase == "artofthetroll"

    cmd.permission match {
      case God if isGod                                     => true
      case Owner if isStreamer || isGod                     => true
      case ModOnly if chatUser.isMod || isStreamer || isGod => true
      case Anyone                                           => true
      case _                                                => false
    }
  }

  private def withQuoteOr(
      foq: ConnectionIO[Option[Quote]],
      msg: String
  ): Stream[ConnectionIO, Response] =
    Stream.eval(foq.map(oq => RespondWith(oq.map(_.display).getOrElse(msg))))

  private def err(msg: String): String = s"Something went wrong! $msg. Somebody tell @artofthetroll"

  private def errHandler(e: Throwable, msg: String): Stream[Pure, Response] =
    Stream.emits(List[Response](RespondWith(err(msg)), LogErr(e)))
}

object CommandInterpreter {
  // This class is just for logging.
  case class Executing(cmdName: String, user: ChatUser, channel: ChannelName, action: Action)
  object Executing {
    implicit val circeCodec: Codec[Executing] = derivation.deriveCodec[Executing]
    implicit val logstageCodec: LogstageCodec[Executing] = LogstageCirceCodec.derived[Executing]
  }

  def interpret[F[_]: MonadCancelThrow](
      msg: ChatMessage,
      cmd: BotCommand,
      action: Action,
      api: Api[ConnectionIO],
      xa: Transactor[F],
      L: LogIOStrict[F]
  ): Stream[F, Response] = {
    val interpreter: CommandInterpreter = CommandInterpreter(api)

    val e = Executing(cmd.name, msg.user, msg.channel, action)

    if (interpreter.checkPermissions(cmd, msg.user, msg.channel)) for {
      _ <- Stream.eval(L.debug(s"executing $e"))
      res <- interpreter.interpret(action).transact(xa)
      _ <- Stream.eval(L.debug(s"done executing $e"))
    } yield res
    // user doesnt' have permission to execute this command, so just do nothing.
    // we _could_ send back a message saying "you can't do that", but i think its too noisy.
    else
      Stream.eval(L.debug(s"user ${msg.user} lacks permission to run: ${cmd.name}")) *> Stream.empty
  }

}

case class ChatMessage(user: ChatUser, channel: ChannelName, body: String)

case object Commands {

  val channelNameParser: Parser[ChannelName] = anyStringAs("channel name").map(ChannelName(_))
  val counterNameParser: Parser[CounterName] = anyStringAs("counter name").map(CounterName(_))

  val printStreamsCommand: BotCommand =
    BotCommand[Unit]("!printStreams", empty, God)((_, _, _) => PrintStreamsAction)

  val getQuoteCommand: BotCommand =
    BotCommand[Option[Int]]("!quote", int.?, Anyone)((channelName, _, mn) =>
      mn.fold[Action](GetRandomQuoteAction(channelName))(n => GetExactQuoteAction(channelName, n))
    )

  val searchQuotesCommand: BotCommand =
    BotCommand[String]("!search", slurp, Anyone)((channelName, _, like) =>
      SearchQuotesAction(channelName, like)
    )

  val addQuoteCommand: BotCommand =
    BotCommand[String]("!addQuote", slurp, ModOnly)((channelName, chatUser, text) =>
      AddQuoteAction(channelName, chatUser, text)
    )

  val delQuoteCommand: BotCommand =
    BotCommand[Int]("!delQuote", int, ModOnly)((channelName, _, n) =>
      DelQuoteAction(channelName, n)
    )

  val partCommand: BotCommand =
    BotCommand[Unit]("!part", empty, Owner)((c, _, _) => PartAction(c))

  val joinCommand: BotCommand =
    BotCommand[ChannelName]("!join", channelNameParser, God)((_, _, newChannelName) =>
      JoinAction(newChannelName)
    )

  val addCounterCommand: BotCommand =
    BotCommand[CounterName]("!addCounter", counterNameParser, God)((channelName, chatUser, name) =>
      AddCounterAction(channelName, chatUser, name)
    )

  val incCounterCommand: BotCommand =
    BotCommand[CounterName]("!inc", counterNameParser, Anyone)((channelName, _, name) =>
      IncCounterAction(channelName, name)
    )

  val helpCommand: BotCommand =
    BotCommand[String]("!help", anyStringAs("command_name"), Anyone)((_, _, command) =>
      HelpAction(command)
    )

  val buildInfoCommand: BotCommand =
    BotCommand[Unit]("!buildInfo", empty, God)((_, _, _) => BuildInfoAction)

  val commands: Map[String, BotCommand] = List(
    joinCommand,
    partCommand,
    getQuoteCommand,
    searchQuotesCommand,
    addQuoteCommand,
    delQuoteCommand,
    printStreamsCommand,
    addCounterCommand,
    incCounterCommand,
    helpCommand,
    buildInfoCommand
  ).map(c => (c.name, c)).toMap

}

case class CommandRunner(commands: Map[String, BotCommand]) {
  // returns the command (if it exists) and the arguments to be passed to the command
  def parseMessageAndFindCommand(msg: ChatMessage): Option[(BotCommand, String)] = {
    val (commandName, args) = msg.body.trim.span(_ != ' ')
    commands.get(commandName).map((_, args))
  }

  def parseFully(msg: ChatMessage): Option[(BotCommand, Either[String, Action])] =
    parseMessageAndFindCommand(msg).map {
      case (cmd, args) => (cmd, cmd.apply(msg.channel, msg.user, args))
    }

  def processMessage[F[_]: MonadCancelThrow](
      msg: ChatMessage,
      api: Api[ConnectionIO],
      xa: Transactor[F]
  )(implicit
      L: LogIOStrict[F]
  ): Stream[F, Response] =
    parseFully(msg) match {
      case Some((cmd, Right(action))) => CommandInterpreter.interpret(msg, cmd, action, api, xa, L)
      case Some((cmd, Left(_)))       => Stream(RespondWith(cmd.help))
      // no command for this chat message, so just do nothing.
      case None => Stream.empty
    }
}
