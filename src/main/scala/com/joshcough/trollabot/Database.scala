package com.joshcough.trollabot

import queries._
import doobie._
import doobie.implicits._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._

case class Stream(id: Option[Int], name: String, joined: Boolean)

case class Quote(id: Option[Int], qid: Int, text : String, userId: String, channel: Int){
  def display: String = s"Quote #$qid: $text"
}

object queries {

  def dropStreamsTable: Update0 =
    sql"drop table if exists streams".update

  def dropQuotesTable: Update0 =
    sql"drop table if exists quotes".update

  def createStreamsTable: Update0 =
    sql"""
      CREATE TABLE streams (
        id SERIAL PRIMARY KEY,
        name character varying NOT NULL,
        joined boolean NOT NULL
      )""".update

  def createQuotesTable: Update0 =
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

  def getAllQuotes: Query0[Quote] = sql"select q.* from quotes q".query[Quote]

  def getAllQuotesForStream(streamName: String): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(streamName)).query[Quote]

  def getQuoteByQid(streamName: String, qid: Int): Query0[Quote] =
    (fr"select q.*" ++ quotesJoinStreams(streamName) ++ fr"and q.qid = $qid").query[Quote]

  def getJoinedStreams: Query0[Stream] =
    sql"select * from streams where joined = true".query[Stream]

  def getAllStreams: Query0[Stream] =
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
             returning *"""
      ).query[Quote]

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
}

case class TrollabotDb(xa: Transactor[IO]) {

  // misc
  def createSchemaIO(): Unit = {
    (dropQuotesTable.run, dropStreamsTable.run).mapN(_ + _).transact(xa).unsafeRunSync()
    (createStreamsTable.run, createQuotesTable.run).mapN(_ + _).transact(xa).unsafeRunSync()
  }

  // streams
  def insertStreamIO(streamName: String): Int =
    queries.insertStream(Stream(None, streamName, joined = false)).run.transact(xa).unsafeRunSync()

  def partStreamIO(streamName: String): Unit =
    queries.partStream(streamName).run.transact(xa).unsafeRunSync()

  def getAllStreamsIO : Seq[Stream] =
    queries.getAllStreams.stream.compile.toList.transact(xa).unsafeRunSync()

  def getJoinedStreamsIO: Seq[Stream] =
    queries.getJoinedStreams.stream.compile.toList.transact(xa).unsafeRunSync()

  def joinStreamIO(streamName: String): Int =
    queries.joinStream(streamName).run.transact(xa).unsafeRunSync()

  def doesStreamExistIO(streamName: String) : Boolean =
    queries.doesStreamExist(streamName).stream.compile.toList.transact(xa).unsafeRunSync().headOption.getOrElse(false)

  // quotes
  def getQuoteByQidIO(stream: String, qid: Int): Option[Quote] =
    queries.getQuoteByQid(stream, qid).stream.compile.toList.transact(xa).unsafeRunSync().headOption

  def getRandomQuoteIO(stream: String): Option[Quote] =
    queries.getRandomQuoteForStream(stream).stream.compile.toList.transact(xa).unsafeRunSync().headOption

  def getAllQuotesIO: Seq[Quote] =
    queries.getAllQuotes.stream.compile.toList.transact(xa).unsafeRunSync()

  def getAllQuotesForStreamIO(stream: String): Seq[Quote] =
    queries.getAllQuotesForStream(stream).stream.compile.toList.transact(xa).unsafeRunSync()

  def insertQuoteIO(text: String, username: String, streamName: String): Option[Quote] =
    queries.insertQuote(text, username, streamName).stream.compile.toList.transact(xa).unsafeRunSync().headOption

  def deleteQuoteIO(streamName: String, qid: Int): Int =
    queries.deleteQuote(streamName: String, qid: Int).run.transact(xa).unsafeRunSync()
}
