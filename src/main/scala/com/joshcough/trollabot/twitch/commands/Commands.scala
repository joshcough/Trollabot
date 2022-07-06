package com.joshcough.trollabot.twitch.commands

import cats.Monad
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.api.{Api, UserCommandName}
import com.joshcough.trollabot.{BuildInfo, ChannelName, ChatUser}
import doobie.implicits._
import doobie.ConnectionIO
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait Permission
case object God extends Permission
case object Streamer extends Permission
case object Mod extends Permission
case object Anyone extends Permission

sealed trait Response

case class RespondWith(s: String) extends Response
case class Join(newChannel: ChannelName) extends Response
case object Part extends Response

case class LogErr(err: String) extends Response

object LogErr {
  def apply(t: Throwable): LogErr = LogErr(t.getMessage + "\n" + t.getStackTrace.mkString(","))
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
  def mkAction(channelName: ChannelName, chatUser: ChatUser, a: A): ActionType

  override def toString: String = s"$name ${parser.describe}"
  def help: String = toString // TODO: maybe we change this around later, but its fine for now.

  def parseAndCheckPerms(
      channelName: ChannelName,
      chatUser: ChatUser,
      args: String
  ): Either[String, Option[Action]] =
    parser(args.trim).toEither.map { a =>
      val action = mkAction(channelName, chatUser, a)
      if (hasPermssion(channelName, chatUser, action)) Some(action) else None
    }

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

  def interpret(
      msg: ChatMessage,
      args: String,
      api: Api[ConnectionIO]
  ): Either[String, Option[Stream[ConnectionIO, Response]]] =
    parseAndCheckPerms(msg.channel, msg.user, args).map(_.map(_.run(api)))

  def interpretFully(
      msg: ChatMessage,
      args: String,
      api: Api[ConnectionIO]
  ): Stream[ConnectionIO, Response] =
    interpret(msg, args, api) match {
      case Left(_) => Stream(RespondWith(help))
      case Right(or) =>
        or.getOrElse(Stream.emit(RespondWith("You don't have permission to do that, man.")))
    }
}

object BotCommand {
  def apply[P, AT <: Action](cmdName: String, cmdParser: Parser[P], perm: AT => Permission)(
      f: (ChannelName, ChatUser, P) => AT
  ): BotCommand =
    new BotCommand {
      override type A = P
      override type ActionType = AT
      val name: String = cmdName
      val permission: ActionType => Permission = perm
      val parser: Parser[P] = cmdParser
      def mkAction(channelName: ChannelName, chatUser: ChatUser, t: P): AT =
        f(channelName, chatUser, t)
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
      ++ UserCommands.userCommandCommands
      ++ BuiltinCommands.builtinCommands).map(c => (c.name, c)).toMap
}

case class CommandRunner(buildInCommands: Map[String, BotCommand]) {
  // returns the command (if it exists) and the arguments to be passed to the command
  def parseMessageAndFindCommand(msg: ChatMessage): Option[(BotCommand, String)] =
    commandNameAndArgs(msg).flatMap {
      case (commandName, args) => buildInCommands.get(commandName).map((_, args))
    }

  def commandNameAndArgs(msg: ChatMessage): Option[(String, String)] = {
    val (commandName, args) = msg.body.trim.span(_ != ' ')
    if (commandName.startsWith("!")) Some((commandName, args)) else None
  }

  def processMessage(
      msg: ChatMessage,
      api: Api[ConnectionIO]
  ): Stream[ConnectionIO, Response] = {
    parseMessageAndFindCommand(msg) match {
      case Some((cmd, args)) => cmd.interpretFully(msg, args, api)
      // no command for this chat message, so just do nothing.
      case None =>
        val (commandName, _) = msg.body.trim.span(_ != ' ')
        UserCommands
          .evaluateUserCommand[ConnectionIO](api)(msg.channel, UserCommandName(commandName))
    }
  }
}
