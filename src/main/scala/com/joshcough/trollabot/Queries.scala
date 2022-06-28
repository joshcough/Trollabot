package com.joshcough.trollabot

import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._

object Queries {

  val dropStreamsTable: Update0 = sql"drop table if exists streams".update

  val dropQuotesTable: Update0 = sql"drop table if exists quotes".update

  val dropCountersTable: Update0 = sql"drop table if exists counters".update

  val createStreamsTable: Update0 =
    sql"""
      CREATE TABLE streams (
        id SERIAL PRIMARY KEY,
        name character varying NOT NULL,
        joined boolean NOT NULL,
        CONSTRAINT unique_stream_name UNIQUE (name)
      )""".update

  val createQuotesTable: Update0 =
    sql"""
      CREATE TABLE quotes (
        id SERIAL PRIMARY KEY,
        qid integer NOT NULL,
        text character varying NOT NULL,
        channel int NOT NULL references streams(id),
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        deleted bool NOT NULL DEFAULT false,
        deleted_by text,
        deleted_at TIMESTAMP WITH TIME ZONE,
        CONSTRAINT unique_quote_channel_and_qid UNIQUE (channel, qid),
        CONSTRAINT unique_quote_channel_and_text UNIQUE (channel, text)
      )""".update

  val createCountersTable: Update0 =
    sql"""
      CREATE TABLE counters (
        id SERIAL PRIMARY KEY,
        name character varying NOT NULL,
        current_count int NOT NULL,
        channel integer NOT NULL,
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT unique_counter_channel UNIQUE (channel, name)
      )""".update

  def getRandomQuoteForStream(channelName: ChannelName): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(channelName) ++ fr"order by random() limit 1").query[Quote]

  def quotesJoinStreams(channelName: ChannelName): Fragment =
    fr"""
      from quotes q
      join streams s on s.id = q.channel
      where s.name = ${channelName.name}
      """

  def countersJoinStreams(counterName: CounterName, channelName: ChannelName): Fragment =
    fr"""
      from counters c
      join streams s on s.id = c.channel
      where s.name = ${channelName.name} and c.name = ${counterName.name}
      """

  val getAllQuotes: Query0[Quote] = sql"select q.* from quotes q".query[Quote]

  val countQuotes: Query0[Int] = sql"select count(*) from quotes".query[Int]
  def countQuotesInStream(channelName: ChannelName): Query0[Int] =
    (fr"select count(*)" ++ quotesJoinStreams(channelName)).query[Int]

  def getAllQuotesForStream(channelName: ChannelName): Query0[Quote] =
    selectQuotes(channelName).query[Quote]

  def searchQuotesForStream(channelName: ChannelName, like: String): doobie.Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text LIKE $like order by q.qid ASC")
      .queryWithLogHandler[Quote](LogHandler.jdkLogHandler)

  def getQuoteByQid(channelName: ChannelName, qid: Int): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.qid = $qid").query[Quote]

  def getQuoteByText(channelName: ChannelName, text: String): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text = $text").query[Quote]

  def selectQuotes(channelName: ChannelName): Fragment =
    fr"select q.*" ++ quotesJoinStreams(channelName)

  val getJoinedStreams: Query0[Stream] =
    sql"select * from streams where joined = true".query[Stream]

  val getAllStreams: Query0[Stream] =
    sql"select * from streams".query[Stream]

  def getStreamId(channelName: ChannelName): Query0[Int] =
    sql"select id from streams where name = ${channelName.name}".query[Int]

  def doesStreamExist(channelName: ChannelName): Query0[Boolean] =
    sql"select exists(select id from streams where name = ${channelName.name})".query[Boolean]

  // TODO: what if stream already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertStream(s: Stream): Update0 =
    sql"insert into streams (name, joined) values (${s.name}, ${s.joined})".update

  // TODO: what if quote already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertQuote(text: String, username: ChatUserName, channelName: ChannelName): Query0[Quote] =
    (fr"insert into quotes (qid, text, channel, added_by)" ++
      fr"select" ++
      fr"(" ++ nextQidForChannel_(channelName) ++ fr")," ++
      fr"""$text,
             s.id,
             ${username.name}
             from streams s where s.name = ${channelName.name}
             returning *""").query[Quote]

  // TODO: instead of deleting - mark as deleted, by whom and when
  def deleteQuote(channelName: ChannelName, qid: Int): Update0 =
    sql"""delete from quotes q
          using streams s
          where s.id = q.channel and s.name = ${channelName.name} and q.qid = $qid
       """.update

  // TODO: instead of deleting - mark as deleted, by whom and when
  def deleteStream(channelName: ChannelName): Update0 =
    sql"delete from streams where name=${channelName.name}".update

  // TODO: record who did this action
  def partStream(channelName: ChannelName): Update0 =
    sql"update streams set joined=false where name=${channelName.name}".update

  // TODO: record who did this action
  def joinStream(channelName: ChannelName): Update0 =
    sql"update streams set joined=true where name=${channelName.name}".update

  def nextQidForChannel_(channelName: ChannelName): Fragment =
    fr"select coalesce(max(q.qid) + 1, 0)" ++ quotesJoinStreams(channelName)

  def nextQidForChannel(channelName: ChannelName): Query0[Int] =
    nextQidForChannel_(channelName).query[Int]

  // these three are pretty much just for testing i think.

  val recreateSchema: ConnectionIO[Int] =
    (
      dropQuotesTable,
      dropCountersTable,
      dropStreamsTable,
      createStreamsTable,
      createQuotesTable,
      createCountersTable
    ) match {
      case (a, b, c, d, e, f) =>
        (a.run, b.run, c.run, d.run, e.run, f.run).mapN(_ + _ + _ + _ + _ + _)
    }

  val deleteAllQuotes: Update0 = sql"delete from quotes".update

  val deleteAllStreams: Update0 = sql"delete from streams".update

  def insertCounter(
      counterName: CounterName,
      username: ChatUserName,
      channelName: ChannelName
  ): Query0[Counter] =
    sql"""insert into counters (name, current_count, channel, added_by)
          select ${counterName.name}, 0, s.id, ${username.name}
          from streams s where s.name = ${channelName.name}
          returning *""".query[Counter]

  def counterValue(counterName: CounterName, channelName: ChannelName): Query0[Int] =
    (sql"select c.current_count" ++ countersJoinStreams(counterName, channelName)).query[Int]

  def selectAllCountersForStream(channelName: ChannelName): Query0[Counter] =
    sql"""select c.* from counters c join streams s
          on s.id = c.channel
          where s.name = ${channelName.name}""".query[Counter]

  def incrementCounter(counterName: CounterName, channelName: ChannelName): Query0[Counter] =
    sql"""update counters c
          set current_count = current_count + 1
          from streams s
          where c.channel = s.id and c.name = ${counterName.name} and s.name = ${channelName.name}
          returning *""".query[Counter]
}
