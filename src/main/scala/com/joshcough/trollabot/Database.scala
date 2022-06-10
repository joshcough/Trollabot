package com.joshcough.trollabot

import cats.effect.Concurrent
import cats.implicits._
import doobie._
import doobie.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class Stream(id: Option[Int], name: String, joined: Boolean)

case class Quote(id: Option[Int], qid: Int, text: String, userId: String, channel: Int) {
  def display: String = s"Quote #$qid: $text"
}

object Quote {
  implicit val quoteDecoder: Decoder[Quote] = deriveDecoder[Quote]
  implicit def quoteEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Quote] = jsonOf
  implicit val quoteEncoder: Encoder[Quote] = deriveEncoder[Quote]
  implicit def quoteEntityEncoder[F[_]]: EntityEncoder[F, Quote] = jsonEncoderOf
}

object Stream {
  implicit val streamDecoder: Decoder[Quote] = deriveDecoder[Quote]
  implicit def streamEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Quote] = jsonOf
  implicit val streamEncoder: Encoder[Quote] = deriveEncoder[Quote]
  implicit def streamEntityEncoder[F[_]]: EntityEncoder[F, Quote] = jsonEncoderOf
}

object TrollabotQueries {

  val dropStreamsTable: Update0 = sql"drop table if exists streams".update

  val dropQuotesTable: Update0 = sql"drop table if exists quotes".update

  val createStreamsTable: Update0 =
    sql"""
      CREATE TABLE streams (
        id SERIAL PRIMARY KEY,
        name character varying NOT NULL,
        joined boolean NOT NULL
      )""".update

  val createQuotesTable: Update0 =
    sql"""
      CREATE TABLE quotes (
        id SERIAL PRIMARY KEY,
        qid integer NOT NULL,
        text character varying NOT NULL,
        user_id character varying NOT NULL,
        channel int NOT NULL references streams(id)
      )""".update

  def getRandomQuoteForStream(streamName: String): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(streamName) ++ fr"order by random() limit 1").query[Quote]

  def quotesJoinStreams(streamName: String): Fragment =
    fr"""
      from quotes q
      join streams s on s.id = q.channel
      where s.name = $streamName
      """

  val getAllQuotes: Query0[Quote] = sql"select q.* from quotes q".query[Quote]

  def getAllQuotesForStream(streamName: String): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(streamName)).query[Quote]

  def getQuoteByQid(streamName: String, qid: Int): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(streamName) ++ fr"and q.qid = $qid").query[Quote]

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
    (fr"insert into quotes (qid, text, user_id, channel)" ++
      fr"select" ++
      fr"(" ++ nextQidForChannel_(streamName) ++ fr")," ++
      fr"""$text,
             $username,
             s.id
             from streams s where s.name = $streamName
             returning *""").query[Quote]

  def deleteQuote(streamName: String, qid: Int): Update0 =
    sql"""delete from quotes q
          using streams s
          where s.id = q.channel and s.name = $streamName and q.qid = $qid
       """.update

  def deleteStream(streamName: String): Update0 =
    sql"delete from streams where name=$streamName".update

  def partStream(streamName: String): Update0 =
    sql"update streams set joined=false where name=$streamName".update

  def joinStream(streamName: String): Update0 =
    sql"update streams set joined=true where name=$streamName".update

  def nextQidForChannel_(streamName: String): Fragment =
    fr"select coalesce(max(q.qid) + 1, 0)" ++ quotesJoinStreams(streamName)

  def nextQidForChannel(streamName: String): Query0[Int] =
    nextQidForChannel_(streamName).query[Int]

  // these three are pretty much just for testing i think.

  val recreateSchema: ConnectionIO[Int] =
    (dropQuotesTable, dropStreamsTable, createStreamsTable, createQuotesTable) match {
      case (a, b, c, d) => (a.run, b.run, c.run, d.run).mapN(_ + _ + _ + _)
    }

  val deleteAllQuotes: Update0 = sql"delete from quotes".update

  val deleteAllStreams: Update0 = sql"delete from streams".update
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

  def insertQuote(text: String, username: String, streamName: String): ConnectionIO[Quote] =
    q.insertQuote(text, username, streamName).unique

  def deleteQuote(streamName: String, qid: Int): ConnectionIO[Int] =
    q.deleteQuote(streamName: String, qid: Int).run

  // testing
  val createSchema: ConnectionIO[Int] = q.recreateSchema
  val deleteAllQuotes: ConnectionIO[Int] = q.deleteAllQuotes.run
  val deleteAllStreams: ConnectionIO[Int] = q.deleteAllStreams.run

}
