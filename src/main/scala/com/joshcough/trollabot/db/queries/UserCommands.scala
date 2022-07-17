package com.joshcough.trollabot.db.queries

import com.joshcough.trollabot.{ChannelName, ChatUserName}
import com.joshcough.trollabot.api.{UserCommand, UserCommandName}
import doobie.{Query0, Update0}
import doobie.implicits._
import doobie.implicits.javasql._

object UserCommands {

  def insertUserCommand(
      userCommandName: UserCommandName,
      body: String,
      username: ChatUserName,
      channelName: ChannelName
  ): Query0[UserCommand] =
    sql"""insert into user_commands (name, body, channel, added_by)
          values(${userCommandName.name}, $body, ${channelName.name}, ${username.name})
          returning name, body, channel, added_by, added_at""".query[UserCommand]

  def selectUserCommand(
      channelName: ChannelName,
      userCommandName: UserCommandName
  ): Query0[UserCommand] =
    sql"""select name, body, channel, added_by, added_at from user_commands
          where channel = ${channelName.name} and name = ${userCommandName.name}"""
      .query[UserCommand]

  def selectAllUserCommandsForStream(channelName: ChannelName): Query0[UserCommand] =
    sql"""select * from user_commands where channel = ${channelName.name}""".query[UserCommand]

  def editUserCommand(
      channelName: ChannelName,
      userCommandName: UserCommandName,
      body: String
  ): Query0[UserCommand] =
    sql"""update user_commands set body = $body
          where channel = ${channelName.name} and name = ${userCommandName.name}
          returning name, body, channel, added_by, added_at
          """.query[UserCommand]

  def deleteUserCommand(channelName: ChannelName, userCommandName: UserCommandName): Update0 =
    sql"""delete from user_commands
          where channel = ${channelName.name} and name = ${userCommandName.name}""".update
}
