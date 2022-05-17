package com.joshcough.trollabot

import doobie._
import doobie.implicits._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._

case class Stream(id: Option[Int], name: String, joined: Boolean)

case class Quote(id: Option[Int], qid: Int, text: String, userId: String, channel: Int) {
  def display: String = s"Quote #$qid: $text"
}

trait TrollabotQueries {

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

// I feel like there is probably a better way on all this stuff
case class TrollabotDb(xa: Transactor[IO]) extends TrollabotQueries {
  def runStream[A](s: fs2.Stream[ConnectionIO, A]): IO[Seq[A]] = transact(s.compile.toList)
  def runQuery[A](q: Query0[A]): IO[Seq[A]] = runStream(q.stream)
  def runUpdate[A](u: Update0): IO[Int] = transact(u.run)
  def transact[A](x: ConnectionIO[A]): IO[A] = x.transact(xa)
}

// This sorta represents stuff that I would still like to kill, if possible.
case class TrollabotDbIO(db: TrollabotDb) {

  // streams
  def insertStream(streamName: String): IO[Int] =
    db.runUpdate(db.insertStream(Stream(None, streamName, joined = false)))

  def partStream(streamName: String): IO[Int] = db.runUpdate(db.partStream(streamName))

  val getAllStreams: IO[Seq[Stream]] = db.runQuery(db.getAllStreams)

  val getJoinedStreams: IO[Seq[Stream]] = db.runQuery(db.getJoinedStreams)

  def joinStream(streamName: String): IO[Int] = db.runUpdate(db.joinStream(streamName))

  def doesStreamExist(streamName: String): IO[Boolean] =
    db.runQuery(db.doesStreamExist(streamName)).map(_.headOption.getOrElse(false))

  // quotes
  def getQuoteByQid(stream: String, qid: Int): IO[Option[Quote]] =
    db.runQuery(db.getQuoteByQid(stream, qid)).map(_.headOption)

  def getRandomQuoteForStream(stream: String): IO[Option[Quote]] =
    db.runQuery(db.getRandomQuoteForStream(stream)).map(_.headOption)

  val getAllQuotes: IO[Seq[Quote]] = db.runQuery(db.getAllQuotes)

  def getAllQuotesForStream(stream: String): IO[Seq[Quote]] = db.runQuery(db.getAllQuotesForStream(stream))

  def insertQuote(text: String, username: String, streamName: String): IO[Option[Quote]] =
    db.runQuery(db.insertQuote(text, username, streamName)).map(_.headOption)

  def deleteQuote(streamName: String, qid: Int): IO[Int] =
    db.runUpdate(db.deleteQuote(streamName: String, qid: Int))

  // testing
  val createSchema: IO[Int] = db.transact(db.recreateSchema)
  val deleteAllQuotes: IO[Int] = db.runUpdate(db.deleteAllQuotes)
  val deleteAllStreams: IO[Int] = db.runUpdate(db.deleteAllStreams)
}
