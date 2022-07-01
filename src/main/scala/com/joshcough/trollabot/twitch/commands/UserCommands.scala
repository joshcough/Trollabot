package com.joshcough.trollabot.twitch.commands

import cats.Monad
import cats.implicits._
import com.joshcough.trollabot.ParserCombinators._
import com.joshcough.trollabot.api.{Api, UserCommandName}
import com.joshcough.trollabot.{ChannelName, ChatUser}
import fs2.{Pure, Stream}

object UserCommands {

  lazy val userCommandCommands: List[BotCommand] =
    List(addUserCommandCommand, editUserCommandCommand, deleteUserCommandCommand)

  case class GetUserCommandAction(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ) extends Action {
    override def run[F[_]: Monad](api: Api[F]): Stream[F, Response] =
      getUserCommand(api)(channelName, userCommandName)
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
    Stream
      .eval(
        api.userCommands
          .insertUserCommand(channelName, chatUser, userCommandName, body)
          .map(_ => RespondWith(s"Ok I added it."))
      )
      .handleErrorWith { e =>
        errHandler(
          e,
          s"I couldn't add command for ${userCommandName.name} stream ${channelName.name}"
        )
      }

  def editUserCommand[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      userCommandName: UserCommandName,
      body: String
  ): Stream[F, Response] =
    Stream
      .eval(
        api.userCommands
          .editUserCommand(channelName, userCommandName, body)
          .map(_ => RespondWith(s"Ok I edited it."))
      )
      .handleErrorWith { e =>
        errHandler(
          e,
          s"I couldn't edit command for ${userCommandName.name} stream ${channelName.name}"
        )
      }

  def deleteUserCommand[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ): Stream[F, Response] =
    Stream
      .eval(
        api.userCommands
          .deleteUserCommand(channelName, userCommandName)
          .map(b => RespondWith(if (b) s"Ok I deleted it." else "Couldn't delete that quote man."))
      )
      .handleErrorWith { e =>
        errHandler(
          e,
          s"I couldn't edit command for ${userCommandName.name} stream ${channelName.name}"
        )
      }

  def getUserCommand[F[_]: Monad](api: Api[F])(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ): Stream[F, Response] =
    Stream
      .eval(
        api.userCommands
          .getUserCommand(channelName, userCommandName)
          .map(o => RespondWith(o.fold("Couldn't find that command, man.")(_.body)))
      )
      .handleErrorWith { e =>
        errHandler(
          e,
          s"I couldn't edit command for ${userCommandName.name} stream ${channelName.name}"
        )
      }

  private def err(msg: String): String = s"Something went wrong! $msg. Somebody tell @artofthetroll"

  private def errHandler(e: Throwable, msg: String): Stream[Pure, Response] =
    Stream.emits(List[Response](RespondWith(err(msg)), LogErr(e)))

}
