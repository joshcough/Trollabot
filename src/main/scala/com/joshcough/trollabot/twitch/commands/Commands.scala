package com.joshcough.trollabot.twitch.commands

import cats.Monad
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.api.Api
import com.joshcough.trollabot.{BuildInfo, ChannelName, ChatUser}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Codec, Decoder, Encoder, derivation}
import logstage.LogstageCodec
import logstage.circe.LogstageCirceCodec
import logstage.strict.LogIOStrict

sealed trait Permission
case object God extends Permission
case object Streamer extends Permission
case object Mod extends Permission
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

trait Action {
  def run[F[_]: Monad](api: Api[F]): Stream[F, Response]
}

case class HelpAction(commandName: String) extends Action {
  override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
    help(commandName, Commands.commands)

  def help[F[_]](
      commandName: String,
      knownCommands: Map[String, BotCommand]
  ): Stream[F, Response] =
    Stream.emit(RespondWith(knownCommands.get(commandName) match {
      case None    => s"Unknown command: $commandName"
      case Some(c) => c.toString
    }))
}
case object BuildInfoAction extends Action {
  override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
    Stream.emit(RespondWith(BuildInfo().asJson.noSpaces))
}

trait BotCommand {
  type A
  type ActionType <: Action
  val name: String
  val parser: Parser[A]

  val permission: ActionType => Permission

  override def toString: String = s"$name ${parser.describe}"
  def help: String = toString // TODO: maybe we change this around later, but its fine for now.

  def execute(channelName: ChannelName, chatUser: ChatUser, a: A): ActionType

  def apply(
      channelName: ChannelName,
      chatUser: ChatUser,
      args: String
  ): Either[String, ActionType] =
    parser(args.trim).toEither.map(a => execute(channelName, chatUser, a))

  // returns true if the given user is allowed to run the command on the given channel
  // false if not.
  def hasPermssion(channelName: ChannelName, chatUser: ChatUser, a: ActionType): Boolean = {
    def isStreamer = chatUser.username.name.toLowerCase == channelName.name.toLowerCase
    def isGod: Boolean = chatUser.username.name.toLowerCase == "artofthetroll"
    permission(a) match {
      case God      => isGod
      case Streamer => isStreamer || isGod
      case Mod      => chatUser.isMod || isStreamer || isGod
      case Anyone   => true
    }
  }
}

object BotCommand {
  def apply[T, AT <: Action](cmdName: String, cmdParser: Parser[T], perm: AT => Permission)(
      f: (ChannelName, ChatUser, T) => AT
  ): BotCommand =
    new BotCommand {
      override type A = T
      override type ActionType = AT
      val name: String = cmdName
      val permission: ActionType => Permission = perm
      val parser: Parser[T] = cmdParser
      def execute(channelName: ChannelName, chatUser: ChatUser, t: T): AT =
        f(channelName, chatUser, t)
    }
}

object CommandInterpreter {

  def interpret[F[_]: MonadCancelThrow](
      msg: ChatMessage,
      cmd: BotCommand,
      args: String,
      api: Api[ConnectionIO],
      xa: Transactor[F],
      L: LogIOStrict[F]
  ): Either[String, Stream[F, Response]] =
    cmd.apply(msg.channel, msg.user, args).map { action =>
      val e = s"{cmdName: ${cmd.name}, user: ${msg.user}, channel: ${msg.channel}, action: $action}"
      if (cmd.hasPermssion(msg.channel, msg.user, action)) for {
        _ <- Stream.eval(L.debug(s"executing $e"))
        res <- action.run(api).transact(xa)
        _ <- Stream.eval(L.debug(s"done executing $e"))
      } yield res
      // user doesnt' have permission to execute this command, so just do nothing.
      // we _could_ send back a message saying "you can't do that", but i think its too noisy.
      else
        Stream
          .eval(L.debug(s"user ${msg.user} lacks permission to run: ${cmd.name}")) *> Stream.empty
    }
}

case class ChatMessage(user: ChatUser, channel: ChannelName, body: String)

object BuiltinCommands {
  val commandNameParser: Parser[String] = anyStringAs("command_name")

  val helpCommand: BotCommand =
    BotCommand[String, HelpAction]("!help", commandNameParser, _ => Anyone)((_, _, command) =>
      HelpAction(command)
    )

  val buildInfoCommand: BotCommand =
    BotCommand[Unit, BuildInfoAction.type]("!buildInfo", empty, _ => God)((_, _, _) =>
      BuildInfoAction
    )

  val builtinCommands: List[BotCommand] = List(
    helpCommand,
    buildInfoCommand
  )
}

case object Commands {
  val commands: Map[String, BotCommand] =
    (Streams.streamCommands
      ++ Quotes.quoteCommands
      ++ Counters.counterCommands
      ++ Scores.scoreCommands
      ++ BuiltinCommands.builtinCommands).map(c => (c.name, c)).toMap
}

case class CommandRunner(commands: Map[String, BotCommand]) {
  // returns the command (if it exists) and the arguments to be passed to the command
  def parseMessageAndFindCommand(msg: ChatMessage): Option[(BotCommand, String)] = {
    val (commandName, args) = msg.body.trim.span(_ != ' ')
    commands.get(commandName).map((_, args))
  }

  def processMessage[F[_]: MonadCancelThrow](
      msg: ChatMessage,
      api: Api[ConnectionIO],
      xa: Transactor[F]
  )(implicit
      L: LogIOStrict[F]
  ): Stream[F, Response] = {
    parseMessageAndFindCommand(msg) match {
      case Some((cmd, args)) =>
        CommandInterpreter.interpret(msg, cmd, args, api, xa, L) match {
          case Left(_)  => Stream(RespondWith(cmd.help))
          case Right(r) => r
        }
      // no command for this chat message, so just do nothing.
      case None => Stream.empty
    }
  }
}
