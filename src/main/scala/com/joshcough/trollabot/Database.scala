package com.joshcough.trollabot

import scala.concurrent.duration.DurationInt
import scala.concurrent.Await
import slick.dbio.Effect
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.{ForeignKeyQuery, ProvenShape}
import slick.sql.FixedSqlAction

case class Stream(id: Option[Int], name: String, joined: Boolean)

class Streams(tag: Tag) extends Table[Stream](tag, "streams") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("name", O.Unique)
  def joined: Rep[Boolean] = column[Boolean]("joined")
  def * : ProvenShape[Stream] = (id.?, name, joined) <> (Stream.tupled, Stream.unapply)
}

case class Quote(id: Option[Int], qid: Int, text : String, userId: String, channel: Int){
  def display: String = s"Quote #$qid: $text"
}

class Quotes(tag: Tag) extends Table[Quote](tag, "quotes") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def text: Rep[String] = column[String]("text")
  def qid: Rep[Int] = column[Int]("qid")
  def userId: Rep[String] = column[String]("user_id")
  def channel: Rep[Int] = column[Int]("channel")
  def * : ProvenShape[Quote] = (id.?, qid, text, userId, channel) <> (Quote.tupled, Quote.unapply)
  // A reified foreign key relation that can be navigated to create a join
  def stream: ForeignKeyQuery[Streams, Stream] =
    foreignKey("quotes_channel_fkey", channel, TableQuery[Streams])(_.id)
}

case class TrollabotDb(db: Database) {

  val quotes = TableQuery[Quotes]
  val streams = TableQuery[Streams]
  val schema: PostgresProfile.DDL = streams.schema ++ quotes.schema

  def getQuoteByQid(streamName: String, qid: Int): Query[Quotes, Quote, Seq] = for {
    sid <- getStreamId(streamName)
    q   <- quotes.filter(q => q.qid === qid && q.channel === sid)
  } yield q

  def psqlRandom: Rep[Double] = SimpleFunction.nullary[Double]("random")

  def getRandomQuoteForStream(streamName: String) : Query[Quotes, Quote, Seq] =
    (streams join quotes on ((s,q) => s.id === q.channel))
      .filter{ case (s, _) => s.name === streamName }
      .map(_._2)
      .sortBy(_ => psqlRandom)
      .take(1)

  def insertStream(s: Stream): FixedSqlAction[Int, NoStream, Effect.Write] = streams += s

  def partStream(streamName: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    streams.filter(_.name === streamName).map(_.joined).update(false)

  // select ..., max(quotes.qid)
  // from quotes join streams
  // on streams.id = quotes.channel
  // where streams.name = ${streamName};
  def nextQidForChannel(streamName: String): Rep[Option[Int]] =
    (streams join quotes on ((s,q) => s.id === q.channel))
      .filter{ case (s, _) => s.name === streamName }
      .map(_._2.qid).max + 1

  def getJoinedStreams: Query[Streams, Stream, Seq] = streams.filter(_.joined)

  def getStreamId(streamName: String): Query[Rep[Int], Int, Seq] =
    streams.filter(_.name === streamName).map(_.id)

  def getAllQuotes(streamName: String): Query[Quotes, Quote, Seq] =
    (streams join quotes on ((s,q) => s.id === q.channel))
      .filter{ case (s, _) => s.name === streamName }
      .map(_._2)

  /////
  /// a bunch of IO actions.
  /////

  // misc
  def createSchemaIO(): Unit = runDb(DBIO.seq(schema.dropIfExists, schema.createIfNotExists))
  def getTablesIO(): Seq[MTable] = runDb(MTable.getTables)
  def closeDbIO() = db.close()
  def runDb[R](act: DBIOAction[R, NoStream, Nothing]): R = Await.result(db.run(act), 1.second)

  // streams
  def insertStreamIO(t: Stream): Int = runDb(insertStream(t))
  def partStreamIO(stream: String): Unit = runDb(partStream(stream))
  def getAllStreamsIO() : Seq[Stream] = runDb(streams.result)
  def getJoinedStreamsIO(): Seq[Stream] = runDb(getJoinedStreams.result)

  // quotes
  def getQuoteByQidIO(stream: String, qid: Int): Option[Quote] =
    runDb(getQuoteByQid(stream, qid).result).headOption
  def getRandomQuoteIO(stream: String): Option[Quote] =
    runDb(getRandomQuoteForStream(stream).result).headOption
  def getAllQuotesIO(stream: String): Seq[Quote] = runDb(getAllQuotes(stream).result)

  ////
  // I don't know how to do these two as `Query`s, so they live here separate from the others
  ////

  def insertQuoteIO(text: String, username: String, streamName: String): Option[Quote] = {
    val streamIdMaybe: Option[Int] = runDb(getStreamId(streamName).result).headOption
    val qidMaybe: Int = runDb(nextQidForChannel(streamName).result).getOrElse(1)
    streamIdMaybe.map{ sid =>
      val q = Quote(None, qidMaybe, text, username, sid)
      val i = runDb(quotes returning quotes.map(_.id) += q)
      q.copy(id = Some(i))
    }
  }

  def deleteQuoteIO(streamName: String, qid: Int): Option[Unit] = {
    val streamIdOpt: Option[Int] = runDb(getStreamId(streamName).result).headOption
    streamIdOpt.map { sid => runDb(quotes.filter(q => q.qid === qid && q.channel === sid).delete) }
  }
}
