package com.joshcough.trollabot.twitch.commands

import cats.Monad
import cats.implicits._
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.api.{Api, CounterName, UserCommandName}
import com.joshcough.trollabot.{ChannelName, ChatUser}
import fs2.Stream

object UserCommands {

  lazy val userCommandCommands: List[BotCommand] =
    List(addUserCommandCommand, editUserCommandCommand, deleteUserCommandCommand)

  case class GetUserCommandAction(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ) extends Action {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      evaluateUserCommand(api)(channelName, userCommandName)
  }

  // TODO: eventually we want this: // !commandName ${c} words words ${c++} words words ${++c} words.
  case class AddUserCommandAction(
      channelName: ChannelName,
      chatUser: ChatUser,
      userCommandName: UserCommandName,
      body: String
  ) extends Action {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      addUserCommand(api)(channelName, chatUser, userCommandName, body)
  }

  case class EditUserCommandAction(
      channelName: ChannelName,
      userCommandName: UserCommandName,
      body: String
  ) extends Action {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      editUserCommand(api)(channelName, userCommandName, body)
  }

  case class DeleteUserCommandAction(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ) extends Action {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      deleteUserCommand(api)(channelName, userCommandName)
  }

  val userCommandNameParser: Parser[UserCommandName] = // "!" ~>
    anyStringAs("command name").map(UserCommandName(_))

  val addUserCommandCommand: BotCommand =
    BotCommand[UserCommandName ~ String, AddUserCommandAction](
      "!add",
      userCommandNameParser ~ slurp,
      _ => Mod
    ) {
      case (channelName, chatUser, userCommandName ~ body) =>
        AddUserCommandAction(channelName, chatUser, userCommandName, body)
    }

  val editUserCommandCommand: BotCommand =
    BotCommand[UserCommandName ~ String, EditUserCommandAction](
      "!edit",
      userCommandNameParser ~ slurp,
      _ => Mod
    ) {
      case (channelName, _, userCommandName ~ body) =>
        EditUserCommandAction(channelName, userCommandName, body)
    }

  val deleteUserCommandCommand: BotCommand =
    BotCommand[UserCommandName, DeleteUserCommandAction](
      "!delete",
      userCommandNameParser,
      _ => Mod
    ) {
      case (channelName, _, userCommandName) =>
        DeleteUserCommandAction(channelName, userCommandName)
    }

  def addUserCommand[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      chatUser: ChatUser,
      userCommandName: UserCommandName,
      body: String
  ): Stream[F, Response] =
    Stream.eval(
      api.userCommands
        .insertUserCommand(channelName, chatUser, userCommandName, body)
        .map(_ => RespondWith(s"Ok I added it."))
    )

  def editUserCommand[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      userCommandName: UserCommandName,
      body: String
  ): Stream[F, Response] =
    Stream.eval(
      api.userCommands
        .editUserCommand(channelName, userCommandName, body)
        .map(_ => RespondWith(s"Ok I edited it."))
    )

  def deleteUserCommand[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ): Stream[F, Response] =
    Stream.eval(
      api.userCommands
        .deleteUserCommand(channelName, userCommandName)
        .map(b => RespondWith(if (b) s"Ok I deleted it." else "Couldn't delete that quote man."))
    )

  def evaluateUserCommand[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ): Stream[F, Response] = Stream.eval(for {
    c <- api.userCommands.getUserCommand(channelName, userCommandName)
    r <- c match {
      case None => (RespondWith("Couldn't find that command, man."): Response).pure[F]
      case Some(cmd) => UserCommandInterpreter.interpret(api)(channelName, cmd.body)
    }
  } yield r)
}

object UserCommandInterpreter {

  trait Node {
    def interp[F[_]: Monad](api: Api[F]): F[String]
  }
  case class TextNode(s: String) extends Node {
    def interp[F[_]: Monad](api: Api[F]): F[String] = s.pure[F]
  }
  case class CounterNode(channelName: ChannelName, counterName: CounterName) extends Node {
    def interp[F[_]: Monad](api: Api[F]): F[String] =
      api.counters.incrementCounter(channelName, counterName).map(_.fold("-1")(_.count.toString))
  }

  /*
  parser for: "I have been F'd ${f++} times! and another ${++f}"

  until theres nothing left
    take characters until we encounter ${, emiting (Text ...)
    then take until we encounter } (though eventually we will probably want escaping)
      then we have to parse the inside of ${}, which could be pre/post increment or just a CounterValue

  the parser for the inside of ${} looks like so:
    if it starts with ++ and then contains an identifier (counter name) then yield Preincrement(identifier)
    if it starts with an identifier (counter name) and then ++ then yield Postincrement(identifier)
    if it starts with an identifier and nothing after it, then yield CounterValue(identifier)
   */
  def parse(channelName: ChannelName, body: String): Either[String, List[Node]] = {
    def parseText(rest: String): (String, String) = {
      val index = rest.indexOf("${")
      if (index > -1) (rest.take(index), rest.drop(index)) else (rest, "")
    }
    def parseIdentifier(rest: String): (String, String) = {
      val (l, r) = rest.span(_ != '}')
      // TODO: what would it mean if l _didn't start with ${ ?
      val l_ = if(l.startsWith("${")) l.drop(2) else l
      // TODO: similarly, what would it mean if r didn't start with } ?
      val r_ = if(r.startsWith("}")) r.drop(1) else r
      (l_, r_)
    }

    sealed trait Mode
    case object TextMode extends Mode
    case object IdentifierMode extends Mode

    def go(input: String, mode: Mode): List[Node] = {
      if(input.isEmpty) Nil else mode match {
        case TextMode =>
          val (s, r) = parseText(input)
          TextNode(s) :: go(r, IdentifierMode)
        case IdentifierMode =>
          val (s, r) = parseIdentifier(input)
          CounterNode(channelName, CounterName(s)) :: go(r, TextMode)
      }
    }
    // TODO: right now we are never failing... but we could in various ways
    // easiest one to understand is if we had something like "hello ${joe"
    // the ${ is never closed, and so its just broken.
    // but we could also have stuff like "hello ${joe 4}"
    // which is an invalid identifier.
    Right(go(body, TextMode))
  }

  def interpNodes[F[_]: Monad](api: Api[F])(nodes: List[Node]): F[String] =
    nodes.traverse(_.interp(api)).map(_.mkString(""))

  def interpret[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      body: String
  ): F[Response] = parse(channelName, body) match {
    case Left(err) => (RespondWith(err): Response).pure[F]
    case Right(nodes) => interpNodes(api)(nodes).map(RespondWith(_))
  }
}
