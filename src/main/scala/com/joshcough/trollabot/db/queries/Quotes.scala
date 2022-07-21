package com.joshcough.trollabot.db.queries

import com.joshcough.trollabot.{ChannelName, ChatUserName}
import com.joshcough.trollabot.api.Quote
import doobie.{Fragment, Query0, Update0}
import doobie.implicits._
import doobie.implicits.javasql._

object Quotes {

  def getRandomQuoteForStream(channelName: ChannelName): Query0[Quote] =
    fr"""select q.qid, q.text, q.channel, q.added_by, q.added_at, q.deleted, q.deleted_by, q.deleted_at
         from quotes q
         where q.channel = ${channelName.name} and deleted = false
         order by random()
         limit 1"""
      .query[Quote]

  val countQuotes: Query0[Int] = sql"select count(*) from quotes".query[Int]
  def countQuotesInStream(channelName: ChannelName): Query0[Int] =
    fr"select count(*) from quotes q where q.channel = ${channelName.name} and deleted = false"
      .query[Int]

  def getAllQuotesForStream(channelName: ChannelName): Query0[Quote] =
    selectQuotes(channelName).query[Quote]

  def searchQuotesForStream(channelName: ChannelName, like: String): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text LIKE $like order by q.qid ASC").query[Quote]
  // NOTE: to print out the sql being ran, use this:
  // .queryWithLogHandler[Quote](LogHandler.jdkLogHandler)
  // instead of .query[Quote]

  def searchQuotesForStream_Random(channelName: ChannelName, like: String): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text LIKE $like order by random() limit 1").query[Quote]

  def getQuoteByQid(channelName: ChannelName, qid: Int): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.qid = $qid").query[Quote]

  def getQuoteByText(channelName: ChannelName, text: String): Query0[Quote] =
    (selectQuotes(channelName) ++ fr"and q.text = $text").query[Quote]

  def selectQuotes(channelName: ChannelName): Fragment =
    fr"""select q.qid, q.text, q.channel, q.added_by, q.added_at, q.deleted, q.deleted_by, q.deleted_at
         from quotes q
         where q.channel = ${channelName.name} and deleted = false"""

  def deleteQuote(channelName: ChannelName, qid: Int, username: ChatUserName): Update0 =
    sql"""update quotes quotes
          set deleted = true, deleted_at = now(), deleted_by = ${username.name}
          where channel = ${channelName.name} and qid = $qid""".update

  def nextQidForChannel_(channelName: ChannelName): Fragment =
    fr"select coalesce(max(q.qid) + 1, 0) from quotes q where q.channel = ${channelName.name}"

  def nextQidForChannel(channelName: ChannelName): Query0[Int] =
    nextQidForChannel_(channelName).query[Int]

  // TODO: what if quote already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertQuote(text: String, username: ChatUserName, channelName: ChannelName): Query0[Quote] =
    (fr"insert into quotes (qid, text, channel, added_by)" ++
      fr"select" ++
      fr"(" ++ nextQidForChannel_(channelName) ++ fr")," ++
      fr"""$text, ${channelName.name}, ${username.name} 
          returning qid, text, channel, added_by, added_at, deleted, deleted_by, deleted_at""")
      .query[Quote]
}
