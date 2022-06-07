package com.joshcough.trollabot.twitch

import com.joshcough.trollabot.{Quote, TrollabotDb}
import ParserCombinators._
import cats.effect.MonadCancelThrow
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
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
    case r @ Part           => r.asJson
  }
  implicit val decodeResponse: Decoder[Response] =
    List[Decoder[Response]](
      Decoder[RespondWith].widen,
      Decoder[Join].widen,
      Decoder[Part.type].widen
    ).reduceLeft(_ or _)
  implicit val logstageCodec: LogstageCodec[Response] = LogstageCirceCodec.derived[Response]
}

case class RespondWith(s: String) extends Response
case class Join(newChannel: String) extends Response
case object Part extends Response {
  implicit val circeCodec: Codec[Part.type] = derivation.deriveCodec[Part.type]
  implicit val logstageCodec: LogstageCodec[Part.type] = LogstageCirceCodec.derived[Part.type]
}

object RespondWith {
  implicit val circeCodec: Codec[RespondWith] = derivation.deriveCodec[RespondWith]
  implicit val logstageCodec: LogstageCodec[RespondWith] = LogstageCirceCodec.derived[RespondWith]
}

object Join {
  implicit val circeCodec: Codec[Join] = derivation.deriveCodec[Join]
  implicit val logstageCodec: LogstageCodec[Join] = LogstageCirceCodec.derived[Join]
}

case class ChatUserName(name: String)
case class ChatUser(username: ChatUserName, isMod: Boolean, subscriber: Boolean, badges: Map[String, String])
case class ChannelName(name: String)

object ChatUserName {
  implicit val circeCodec: Codec[ChatUserName] = derivation.deriveCodec[ChatUserName]
  implicit val logstageCodec: LogstageCodec[ChatUserName] = LogstageCirceCodec.derived[ChatUserName]
}

object ChatUser {
  implicit val circeCodec: Codec[ChatUser] = derivation.deriveCodec[ChatUser]
  implicit val logstageCodec: LogstageCodec[ChatUser] = LogstageCirceCodec.derived[ChatUser]
}

object ChannelName {
  implicit val circeCodec: Codec[ChannelName] = derivation.deriveCodec[ChannelName]
  implicit val logstageCodec: LogstageCodec[ChannelName] = LogstageCirceCodec.derived[ChannelName]
}

trait Action
case object PrintStreamsAction extends Action
case class GetExactQuoteAction(channelName: ChannelName, qid: Int) extends Action
case class GetRandomQuoteAction(channelName: ChannelName) extends Action
case class AddQuoteAction(channelName: ChannelName, chatUser: ChatUser, text: String) extends Action
case class DelQuoteAction(channelName: ChannelName, n: Int) extends Action
case class PartAction(channelName: ChannelName) extends Action
case class JoinAction(newChannelName: String) extends Action

object Action {
  implicit val encodeResponse: Encoder[Action] = Encoder.instance {
    case r @ PrintStreamsAction        => r.asJson
    case r @ GetExactQuoteAction(_, _) => r.asJson
    case r @ GetRandomQuoteAction(_)   => r.asJson
    case r @ AddQuoteAction(_, _, _)   => r.asJson
    case r @ DelQuoteAction(_, _)      => r.asJson
    case r @ PartAction(_)             => r.asJson
    case r @ JoinAction(_)             => r.asJson
  }
  implicit val decodeResponse: Decoder[Action] =
    List[Decoder[Action]](
      Decoder[PrintStreamsAction.type].widen,
      Decoder[GetExactQuoteAction].widen,
      Decoder[GetRandomQuoteAction].widen,
      Decoder[AddQuoteAction].widen,
      Decoder[DelQuoteAction].widen,
      Decoder[PartAction].widen,
      Decoder[JoinAction].widen
    ).reduceLeft(_ or _)
  implicit val logstageCodec: LogstageCodec[Action] = LogstageCirceCodec.derived[Action]
}

trait BotCommand {
  type A
  val name: String
  val permission: Permission
  val parser: Parser[A]
  override def toString: String = s"Command(name: $name, perm: $permission)"
  def execute(channelName: ChannelName, chatUser: ChatUser, a: A): Action
  def apply(channelName: ChannelName, chatUser: ChatUser, args: String): Either[String, Action] =
    parser(args.trim).toEither.map(a => execute(channelName, chatUser, a))
}

object CommandInterpreter {
  val db = TrollabotDb

  def interpret(a: Action): Stream[ConnectionIO, Response] =
    a match {
      case PrintStreamsAction                          => printStreams
      case GetExactQuoteAction(channelName, qid)       => getExactQuote(channelName, qid)
      case GetRandomQuoteAction(channelName)           => getRandomQuote(channelName)
      case AddQuoteAction(channelName, chatUser, text) => addQuote(channelName, chatUser, text)
      case DelQuoteAction(channelName, qid)            => deleteQuote(channelName, qid)
      case PartAction(channelName)                     => part(channelName)
      case JoinAction(newChannelName: String)          => join(newChannelName)
    }

  def printStreams: Stream[ConnectionIO, Response] =
    db.getAllStreams.map(_.toString).reduce((l, r) => s"$l, $r").map(RespondWith(_))

  def getExactQuote(channelName: ChannelName, qid: Int): Stream[ConnectionIO, Response] =
    Stream.eval(withQuoteOr(db.getQuoteByQid(channelName.name, qid), s"I couldn't find quote #$qid, man."))

  def getRandomQuote(channelName: ChannelName): Stream[ConnectionIO, Response] =
    Stream.eval(withQuoteOr(db.getRandomQuoteForStream(channelName.name), "I couldn't find any quotes, man."))

  def addQuote(channelName: ChannelName, chatUser: ChatUser, text: String): Stream[ConnectionIO, Response] = {
    val f = db.insertQuote(text, chatUser.username.name, channelName.name).map(q => RespondWith(q.display))
    val msg = err(s"I couldn't add quote for stream ${channelName.name}")
    Stream.eval(f).handleErrorWith(_ => Stream.emit(RespondWith(msg)))
  }

  // TODO: maybe we should mark the quote deleted instead of deleting it
  // and then we could take the user who deleted it too.
  // we could add two new columns to quote: deletedAt and deletedBy
  def deleteQuote(channelName: ChannelName, n: Int): Stream[ConnectionIO, Response] =
    Stream.eval(db.deleteQuote(channelName.name, n).map {
      case 1 => RespondWith("Ok I deleted it.")
      case _ => RespondWith(err(s"I couldn't delete quote $n for channel ${channelName.name}"))
    })

  def part(channelName: ChannelName): Stream[ConnectionIO, Response] =
    Stream
      .eval(db.partStream(channelName.name))
      .flatMap(_ => Stream(RespondWith("Goodbye cruel world!"), Part))

  def join(newChannelName: String): Stream[ConnectionIO, Response] =
    Stream
      .eval(for {
        b <- db.doesStreamExist(newChannelName)
        z <- if (b) db.insertStream(newChannelName) else db.joinStream(newChannelName)
      } yield z)
      .flatMap(_ => Stream(Join(newChannelName), RespondWith(s"Joining $newChannelName!")))

  // returns true if the given user is allowed to run the command on the given channel
  // false if not.
  def checkPermissions(cmd: BotCommand, chatUser: ChatUser, channelName: ChannelName): Boolean = {
    def isStreamer = chatUser.username.name.toLowerCase == channelName.name.toLowerCase
    def isGod: Boolean = chatUser.username.name.toLowerCase == "artofthetroll"
    if (isGod) true
    else
      cmd.permission match {
        case God if isGod                                     => true
        case Owner if isStreamer || isGod                     => true
        case ModOnly if chatUser.isMod || isStreamer || isGod => true
        case Anyone                                           => true
        case _                                                => false
      }
  }

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
      xa: Transactor[F],
      L: LogIOStrict[F]
  ): Stream[F, Response] = {
    val e = Executing(cmd.name, msg.user, msg.channel, action)
    if (CommandInterpreter.checkPermissions(cmd, msg.user, msg.channel)) for {
      _ <- Stream.eval(L.debug(s"executing $e"))
      res <- CommandInterpreter.interpret(action).transact(xa)
      _ <- Stream.eval(L.debug(s"done executing $e"))
    } yield res
    // user doesnt' have permission to execute this command, so just do nothing.
    // we _could_ send back a message saying "you can't do that", but i think its too noisy.
    else Stream.eval(L.debug(s"user ${msg.user} lacks permission to run: ${cmd.name}")) *> Stream.empty
  }

  private def withQuoteOr(foq: ConnectionIO[Option[Quote]], msg: String): ConnectionIO[Response] =
    foq.map(oq => RespondWith(oq.map(_.display).getOrElse(msg)))

  private def err(msg: String): String = s"Something went wrong! $msg. Somebody tell @artofthetroll"
}

case class ChatMessage(user: ChatUser, channel: ChannelName, body: String)

case object Commands {

  val printStreamsCommand: BotCommand = new BotCommand {
    override type A = Unit
    val name: String = "!printStreams"
    val permission: Permission = God
    val parser: Parser[Unit] = empty
    def execute(channelName: ChannelName, chatUser: ChatUser, _r: Unit): Action = PrintStreamsAction
  }

  val getQuoteCommand: BotCommand = new BotCommand {
    override type A = Option[Int]
    val name: String = "!quote"
    val permission: Permission = Anyone
    val parser: Parser[Option[Int]] = int.?
    def execute(channelName: ChannelName, chatUser: ChatUser, mn: Option[Int]): Action =
      mn.fold[Action](GetRandomQuoteAction(channelName))(n => GetExactQuoteAction(channelName, n))
  }

  val addQuoteCommand: BotCommand = new BotCommand {
    override type A = String
    val name: String = "!addQuote"
    val permission: Permission = ModOnly
    val parser: Parser[String] = slurp
    def execute(channelName: ChannelName, chatUser: ChatUser, text: String): Action =
      AddQuoteAction(channelName, chatUser, text)
  }

  val delQuoteCommand: BotCommand = new BotCommand {
    override type A = Int
    val name: String = "!delQuote"
    val permission: Permission = ModOnly
    val parser: Parser[Int] = int
    def execute(channelName: ChannelName, chatUser: ChatUser, n: Int): Action = DelQuoteAction(channelName, n)
  }

  val partCommand: BotCommand = new BotCommand {
    override type A = Unit
    val name: String = "!part"
    val permission: Permission = Owner
    val parser: Parser[Unit] = empty
    override def execute(c: ChannelName, _u: ChatUser, _x: Unit): Action = PartAction(c)
  }

  val joinCommand: BotCommand = new BotCommand {
    override type A = String
    val name: String = "!join"
    val permission: Permission = God
    val parser: Parser[String] = anyString
    def execute(channelName: ChannelName, chatUser: ChatUser, newChannelName: String): Action =
      JoinAction(newChannelName)
  }

  val commands: Map[String, BotCommand] = List(
    joinCommand,
    partCommand,
    getQuoteCommand,
    addQuoteCommand,
    delQuoteCommand,
    printStreamsCommand
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

  def processMessage[F[_]: MonadCancelThrow](msg: ChatMessage, xa: Transactor[F])(implicit
      L: LogIOStrict[F]
  ): Stream[F, Response] =
    parseFully(msg) match {
      case Some((cmd, Right(action))) => CommandInterpreter.interpret(msg, cmd, action, xa, L)
      case Some((_, Left(err)))       => Stream(RespondWith(err))
      // no command for this chat message, so just do nothing.
      case None => Stream.empty
    }
}
