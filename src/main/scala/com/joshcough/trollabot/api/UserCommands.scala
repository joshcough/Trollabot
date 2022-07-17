package com.joshcough.trollabot.api

import cats.effect.MonadCancelThrow
import com.joshcough.trollabot.{ChannelName, ChatUser, ChatUserName}
import com.joshcough.trollabot.db.queries.{UserCommands => UserCommandQueries}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import java.sql.Timestamp

case class UserCommandName(namePotentiallyWithBang: String) extends AnyVal {
  // i need to clean this up but for now eh
  def name: String =
    if (namePotentiallyWithBang.startsWith("!")) namePotentiallyWithBang.drop(1)
    else namePotentiallyWithBang
}

case class UserCommand(
    name: UserCommandName,
    body: String,
    channel: ChannelName,
    addedBy: ChatUserName,
    addedAt: Timestamp
)

trait UserCommands[F[_]] {
  def insertUserCommand(
      channelName: ChannelName,
      chatUser: ChatUser,
      userCommandName: UserCommandName,
      body: String
  ): F[UserCommand]
  def getUserCommand(channelName: ChannelName, commandName: UserCommandName): F[Option[UserCommand]]
  def editUserCommand(
      channelName: ChannelName,
      commandName: UserCommandName,
      body: String
  ): F[Option[UserCommand]]
  def deleteUserCommand(channelName: ChannelName, commandName: UserCommandName): F[Boolean]
  def getUserCommands(channelName: ChannelName): fs2.Stream[F, UserCommand]
}

object UserCommands {
  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): UserCommands[F] =
    new UserCommands[F] {
      def getUserCommand(
          channelName: ChannelName,
          commandName: UserCommandName
      ): F[Option[UserCommand]] =
        UserCommandsDb.getUserCommand(channelName, commandName).transact(xa)

      def getUserCommands(channelName: ChannelName): fs2.Stream[F, UserCommand] =
        UserCommandsDb.getUserCommands(channelName).transact(xa)

      // TODO: this one might want to return an either or something, in case a userCommand with that name
      // already exists.
      def insertUserCommand(
          channelName: ChannelName,
          chatUser: ChatUser,
          userCommandName: UserCommandName,
          body: String
      ): F[UserCommand] =
        UserCommandsDb.insertUserCommand(channelName, chatUser, userCommandName, body).transact(xa)

      def editUserCommand(
          channelName: ChannelName,
          commandName: UserCommandName,
          body: String
      ): F[Option[UserCommand]] =
        UserCommandsDb.editUserCommand(channelName, commandName, body).transact(xa)
      def deleteUserCommand(channelName: ChannelName, commandName: UserCommandName): F[Boolean] =
        UserCommandsDb.deleteUserCommand(channelName, commandName).transact(xa)
    }
}

object UserCommandsDb extends UserCommands[ConnectionIO] {

  def getUserCommand(
      channelName: ChannelName,
      name: UserCommandName
  ): ConnectionIO[Option[UserCommand]] =
    UserCommandQueries.selectUserCommand(channelName, name).option

  def getUserCommands(channelName: ChannelName): fs2.Stream[ConnectionIO, UserCommand] =
    UserCommandQueries.selectAllUserCommandsForStream(channelName).stream

  // TODO: we need to check if a command already exists
  // and or catch the error when we get a conflict inserting
  def insertUserCommand(
      channelName: ChannelName,
      chatUser: ChatUser,
      userCommandName: UserCommandName,
      body: String
  ): ConnectionIO[UserCommand] =
    UserCommandQueries
      .insertUserCommand(userCommandName, body, chatUser.username, channelName)
      .unique

  def editUserCommand(
      channelName: ChannelName,
      commandName: UserCommandName,
      body: String
  ): ConnectionIO[Option[UserCommand]] =
    UserCommandQueries.editUserCommand(channelName, commandName, body).option

  def deleteUserCommand(
      channelName: ChannelName,
      commandName: UserCommandName
  ): ConnectionIO[Boolean] =
    UserCommandQueries.deleteUserCommand(channelName, commandName).run.map(_ == 1)
}
