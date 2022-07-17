package com.joshcough.trollabot.db.queries

import com.joshcough.trollabot.{ChannelName, ChatUserName}
import com.joshcough.trollabot.api.{Counter, CounterName}
import doobie.Query0
import doobie.implicits._
import doobie.implicits.javasql._

object Counters {

  def insertCounter(
      counterName: CounterName,
      username: ChatUserName,
      channelName: ChannelName
  ): Query0[Counter] =
    sql"""insert into counters (name, current_count, channel, added_by)
          values(${counterName.name}, 0, ${channelName.name}, ${username.name})
          returning name, current_count, channel, added_by, added_at""".query[Counter]

  def counterValue(counterName: CounterName, channelName: ChannelName): Query0[Int] =
    sql"""select current_count from counters
          where channel = ${channelName.name} and name = ${counterName.name}""".query[Int]

  def selectAllCountersForStream(channelName: ChannelName): Query0[Counter] =
    sql"""select name, current_count, channel, added_by, added_at
          from counters where channel = ${channelName.name}""".query[Counter]

  def incrementCounter(counterName: CounterName, channelName: ChannelName): Query0[Counter] =
    sql"""update counters
          set current_count = current_count + 1
          where channel = ${channelName.name} and name = ${counterName.name}
          returning name, current_count, channel, added_by, added_at""".query[Counter]
}
