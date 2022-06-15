package com.joshcough.trollabot

import cats.Show
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.sql.Timestamp

object TimestampInstances {
  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] =
    new Encoder[Timestamp] with Decoder[Timestamp] {
      override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)
      override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
    }
}

case class Stream(id: Option[Int], name: String, joined: Boolean)

case class Quote(
    id: Option[Int],
    qid: Int,
    text: String,
    channel: Int,
    addedBy: String,
    addedAt: Timestamp,
    deleted: Boolean,
    deletedBy: Option[String],
    deletedAt: Option[Timestamp]
) {
  def display: String = s"Quote #$qid: $text"
}

case class Counter(id: Option[Int], name: String, count: Int, channel: Int, addedBy: String, addedAt: Timestamp)

import TimestampInstances._

object Quote {
  implicit val quoteDecoder: Decoder[Quote] = deriveDecoder[Quote]
  implicit val quoteEncoder: Encoder[Quote] = deriveEncoder[Quote]
  implicit val quoteShow: Show[Quote] = Show.fromToString
}

object Stream {
  implicit val streamDecoder: Decoder[Stream] = deriveDecoder[Stream]
  implicit val streamEncoder: Encoder[Stream] = deriveEncoder[Stream]
}

object Counter {
  implicit val counterDecoder: Decoder[Counter] = deriveDecoder[Counter]
  implicit val counterEncoder: Encoder[Counter] = deriveEncoder[Counter]
}

object TrollabotQueries {

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
        CONSTRAINT unique_quote_channel_and_qid UNIQUE (channel, qid)
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

  def getRandomQuoteForStream(streamName: String): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(streamName) ++ fr"order by random() limit 1").query[Quote]

  def quotesJoinStreams(streamName: String): Fragment =
    fr"""
      from quotes q
      join streams s on s.id = q.channel
      where s.name = $streamName
      """

  def countersJoinStreams(counterName: String, streamName: String): Fragment =
    fr"""
      from counters c
      join streams s on s.id = c.channel
      where s.name = $streamName and c.name = $counterName
      """

  val getAllQuotes: Query0[Quote] = sql"select q.* from quotes q".query[Quote]

  val countQuotes: Query0[Int] = sql"select count(*) from quotes".query[Int]
  def countQuotesInStream(streamName: String): Query0[Int] =
    (fr"select count(*)" ++ quotesJoinStreams(streamName)).query[Int]

  def getAllQuotesForStream(streamName: String): Query0[Quote] =
    selectQuotes(streamName).query[Quote]

  def searchQuotesForStream(streamName: String, like: String): doobie.Query0[Quote] =
    (selectQuotes(streamName) ++ fr"and q.text LIKE $like order by q.qid ASC")
      .queryWithLogHandler[Quote](LogHandler.jdkLogHandler)

  def getQuoteByQid(streamName: String, qid: Int): Query0[Quote] =
    (selectQuotes(streamName) ++ fr"and q.qid = $qid").query[Quote]

  def selectQuotes(streamName: String): Fragment =
    fr"select q.*" ++ quotesJoinStreams(streamName)

  val getJoinedStreams: Query0[Stream] =
    sql"select * from streams where joined = true".query[Stream]

  val getAllStreams: Query0[Stream] =
    sql"select * from streams".query[Stream]

  def getStreamId(streamName: String): Query0[Int] =
    sql"select id from streams where name = $streamName".query[Int]

  def doesStreamExist(streamName: String): Query0[Boolean] =
    sql"select exists(select id from streams where name = $streamName)".query[Boolean]

  // TODO: what if stream already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertStream(s: Stream): Update0 =
    sql"insert into streams (name, joined) values (${s.name}, ${s.joined})".update

  // TODO: what if quote already has an ID? thats bad right we need to catch that, because it shouldn't.
  def insertQuote(text: String, username: String, streamName: String): Query0[Quote] =
    (fr"insert into quotes (qid, text, channel, added_by)" ++
      fr"select" ++
      fr"(" ++ nextQidForChannel_(streamName) ++ fr")," ++
      fr"""$text,
             s.id,
             $username
             from streams s where s.name = $streamName
             returning *""").query[Quote]

  // TODO: instead of deleting - mark as deleted, by whom and when
  def deleteQuote(streamName: String, qid: Int): Update0 =
    sql"""delete from quotes q
          using streams s
          where s.id = q.channel and s.name = $streamName and q.qid = $qid
       """.update

  // TODO: instead of deleting - mark as deleted, by whom and when
  def deleteStream(streamName: String): Update0 =
    sql"delete from streams where name=$streamName".update

  // TODO: record who did this action
  def partStream(streamName: String): Update0 =
    sql"update streams set joined=false where name=$streamName".update

  // TODO: record who did this action
  def joinStream(streamName: String): Update0 =
    sql"update streams set joined=true where name=$streamName".update

  def nextQidForChannel_(streamName: String): Fragment =
    fr"select coalesce(max(q.qid) + 1, 0)" ++ quotesJoinStreams(streamName)

  def nextQidForChannel(streamName: String): Query0[Int] =
    nextQidForChannel_(streamName).query[Int]

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
      case (a, b, c, d, e, f) => (a.run, b.run, c.run, d.run, e.run, f.run).mapN(_ + _ + _ + _ + _ + _)
    }

  val deleteAllQuotes: Update0 = sql"delete from quotes".update

  val deleteAllStreams: Update0 = sql"delete from streams".update

  def insertCounter(counterName: String, username: String, streamName: String): Query0[Counter] =
    sql"""insert into counters (name, current_count, channel, added_by)
          select $counterName, 0, s.id, $username
          from streams s where s.name = $streamName
          returning *""".query[Counter]

  def counterValue(counterName: String, streamName: String): Query0[Int] =
    (sql"select c.current_count" ++ countersJoinStreams(counterName, streamName)).query[Int]

  def selectAllCountersForStream(streamName: String): Query0[Counter] =
    sql"select c.* from counters c join streams s on s.id = c.channel where s.name = $streamName".query[Counter]

  def incrementCounter(counterName: String, streamName: String): Query0[Counter] =
    sql"""update counters c
          set current_count = current_count + 1
          from streams s
          where c.channel = s.id and c.name = $counterName and s.name = $streamName
          returning *""".query[Counter]
}

// Paul likes more control over where transactions happen
// and i want to follow up on that.
object TrollabotDb {
  val q = TrollabotQueries

  // streams
  def insertStream(streamName: String): ConnectionIO[Int] =
    q.insertStream(Stream(None, streamName, joined = false)).run

  def partStream(streamName: String): ConnectionIO[Int] = q.partStream(streamName).run

  val getAllStreams: fs2.Stream[ConnectionIO, Stream] = q.getAllStreams.stream

  val getJoinedStreams: fs2.Stream[ConnectionIO, Stream] = q.getJoinedStreams.stream

  def joinStream(streamName: String): ConnectionIO[Int] = q.joinStream(streamName).run

  def doesStreamExist(streamName: String): ConnectionIO[Boolean] =
    q.doesStreamExist(streamName).unique

  def getQuoteByQid(stream: String, qid: Int): ConnectionIO[Option[Quote]] =
    q.getQuoteByQid(stream, qid).option

  def getRandomQuoteForStream(stream: String): ConnectionIO[Option[Quote]] =
    q.getRandomQuoteForStream(stream).option

  val getAllQuotes: fs2.Stream[ConnectionIO, Quote] = q.getAllQuotes.stream

  def getAllQuotesForStream(stream: String): fs2.Stream[ConnectionIO, Quote] =
    q.getAllQuotesForStream(stream).stream

  def searchQuotesForStream(stream: String, like: String): fs2.Stream[ConnectionIO, Quote] =
    q.searchQuotesForStream(stream, like).stream

  val countQuotes: ConnectionIO[Int] = q.countQuotes.unique
  def countQuotesInStream(streamName: String): ConnectionIO[Int] =
    q.countQuotesInStream(streamName).unique

  def insertQuote(text: String, username: String, streamName: String): ConnectionIO[Quote] =
    q.insertQuote(text, username, streamName).unique

  def deleteQuote(streamName: String, qid: Int): ConnectionIO[Int] =
    q.deleteQuote(streamName: String, qid: Int).run

  def insertCounter(counterName: String, username: String, streamName: String): ConnectionIO[Counter] =
    q.insertCounter(counterName, username, streamName).unique

  def incrementCounter(counterName: String, streamName: String): ConnectionIO[Counter] =
    q.incrementCounter(counterName, streamName).unique

  def getAllCountersForStream(streamName: String): fs2.Stream[ConnectionIO, Counter] =
    q.selectAllCountersForStream(streamName).stream

  // testing
  val createSchema: ConnectionIO[Int] = q.recreateSchema
  val deleteAllQuotes: ConnectionIO[Int] = q.deleteAllQuotes.run
  val deleteAllStreams: ConnectionIO[Int] = q.deleteAllStreams.run

}
