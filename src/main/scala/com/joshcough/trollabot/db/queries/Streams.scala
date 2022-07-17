package com.joshcough.trollabot.db.queries

import com.joshcough.trollabot.{ChannelName, ChatUserName}
import com.joshcough.trollabot.api.Stream
import doobie.{Query0, Update0}
import doobie.implicits._
import doobie.implicits.javasql._

object Streams {

  val getJoinedStreams: Query0[Stream] =
    sql"select name, joined, added_by, added_at from streams where joined = true".query[Stream]

  val getAllStreams: Query0[Stream] =
    sql"select name, joined, added_by, added_at from streams".query[Stream]

  def getStreamByName(channelName: ChannelName): Query0[Stream] =
    sql"select name, joined, added_by, added_at from streams where name = ${channelName.name}"
      .query[Stream]

  // TODO: what if stream already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertStream(
      channelName: ChannelName,
      joined: Boolean,
      username: ChatUserName
  ): Query0[Stream] =
    sql"""insert into streams (name, joined, added_by)
          values (${channelName.name}, ${joined}, ${username.name})
          returning name, joined, added_by, added_at""".query[Stream]

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
