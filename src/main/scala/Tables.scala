import TrollabotDb.quotes
import slick.dbio.Effect
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}
import slick.sql.FixedSqlAction

case class Stream(id: Int, name: String)

class Streams(tag: Tag) extends Table[Stream](tag, "streams") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  def name: Rep[String] = column[String]("name")
  def * : ProvenShape[Stream] = (id, name) <> (Stream.tupled, Stream.unapply)
}

case class Quote(id: Int, text : String, qid: Int, userId: String, channel: Int)

class Quotes(tag: Tag) extends Table[Quote](tag, "quotes") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  def text: Rep[String] = column[String]("text")
  def qid: Rep[Int] = column[Int]("qid")
  def userId: Rep[String] = column[String]("user_id")
  def channel: Rep[Int] = column[Int]("channel")
  def * : ProvenShape[Quote] = (id, text, qid, userId, channel) <> (Quote.tupled, Quote.unapply)

  // A reified foreign key relation that can be navigated to create a join
  def stream: ForeignKeyQuery[Streams, Stream] =
    foreignKey("quotes_channel_fkey", channel, TableQuery[Streams])(_.id)
}

object TrollabotDb {

  val quotes = TableQuery[Quotes]
  val streams = TableQuery[Streams]

  val schema: PostgresProfile.DDL = streams.schema ++ quotes.schema

  // TODO: these are all lacking channel name, but, its not so bad for now
  def insertQuote(t: Quote): FixedSqlAction[Int, NoStream, Effect.Write] = quotes += t
  def getQuoteByQid_(qid: Int): Query[Quotes, Quote, Seq] = quotes.filter(_.qid === qid)
  def getRandomQuote_ : Query[Quotes, Quote, Seq] =
    quotes.sortBy(_ => SimpleFunction.nullary[Double]("random")).take(1)
  def deleteQuoteByQid_(qid: Int): FixedSqlAction[Int, NoStream, Effect.Write] =
    quotes.filter(_.qid === qid).delete
  def insertStream(s: Stream): FixedSqlAction[Int, NoStream, Effect.Write] = streams += s

  def joinedStreamsAndQuotes: Query[(Streams, Quotes), (Stream, Quote), Seq] =
    streams join quotes on (_.id === _.channel)

  //class Monad m => QuotesDb m where
  //  insertQuote :: ChannelName -> ChatUserName -> Text -> m Quote
  //  getQuote :: ChannelName -> Int -> m (Maybe Quote)
  //  deleteQuote :: ChannelName -> Int -> m ()
  //  getQuotes :: ChannelName -> m [Quote]
  //  getRandomQuote :: ChannelName -> m (Maybe Quote)
}


//val innerJoin = for {
//  (c, s) <- coffees join suppliers on (_.supID === _.id)
//} yield (c.name, s.name)
//// compiles to SQL (simplified):
////   select x2."COF_NAME", x3."SUP_NAME" from "COFFEES" x2
////     inner join "SUPPLIERS" x3
////     on x2."SUP_ID" = x3."SUP_ID"

// here is how we can print out some sql if we want
//  val q = coffees.filter(_.supID === 15)
//  val action = q.delete
//  val affectedRowsCount: Future[Int] = db.run(action)
//  val sql = action.statements.head


/*
slick=> \d quotes;
                                  Table "public.quotes"
 Column  |       Type        | Collation | Nullable |              Default
---------+-------------------+-----------+----------+------------------------------------
 id      | integer           |           | not null | nextval('quotes_id_seq'::regclass)
 text    | character varying |           | not null |
 qid     | bigint            |           | not null |
 user_id | character varying |           | not null |
 channel | bigint            |           |          |
Indexes:
    "quotes_pkey" PRIMARY KEY, btree (id)
Foreign-key constraints:
    "quotes_channel_fkey" FOREIGN KEY (channel) REFERENCES streams(id)

slick=> \d streams;
                                 Table "public.streams"
 Column |       Type        | Collation | Nullable |               Default
--------+-------------------+-----------+----------+-------------------------------------
 id     | integer           |           | not null | nextval('streams_id_seq'::regclass)
 name   | character varying |           | not null |
Indexes:
    "streams_pkey" PRIMARY KEY, btree (id)
    "streams_name_key" UNIQUE CONSTRAINT, btree (name)
Referenced by:
    TABLE "quotes" CONSTRAINT "quotes_channel_fkey" FOREIGN KEY (channel) REFERENCES streams(id)
* */

//val innerJoin = for {
//  (c, s) <- coffees join suppliers on (_.supID === _.id)
//} yield (c.name, s.name)
//// compiles to SQL (simplified):
////   select x2."COF_NAME", x3."SUP_NAME" from "COFFEES" x2
////     inner join "SUPPLIERS" x3
////     on x2."SUP_ID" = x3."SUP_ID"

// here is how we can print out some sql if we want
//  val q = coffees.filter(_.supID === 15)
//  val action = q.delete
//  val affectedRowsCount: Future[Int] = db.run(action)
//  val sql = action.statements.head
