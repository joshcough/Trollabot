import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta._

case class TrollabotDbActions(db: Database) extends ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  import TrollabotDb._

  def createSchema(): Unit =
    db.run(DBIO.seq(schema.dropIfExists, schema.createIfNotExists)).futureValue
  def insertQuote(t: Quote): Int = db.run(quotes += t).futureValue
  def getQuoteByQid(qid: Int): Option[Quote] =
    db.run(getQuoteByQid_(qid).result).futureValue.headOption
  def getRandomQuote: Seq[Quote] = db.run(getRandomQuote_.result).futureValue
  def deleteQuoteByQid(qid: Int): Int = db.run(deleteQuoteByQid_(qid)).futureValue
  def getAllQuotes: Seq[Quote] = db.run(quotes.result).futureValue
  def getTables(): Seq[MTable] = db.run(MTable.getTables).futureValue
  def insertStream(t: Stream): Int = db.run(streams += t).futureValue
  def getStreams() : Seq[Stream] = db.run(streams.result).futureValue
  def getJoinedStreamsAndQuotes(): Seq[(Stream, Quote)] =
    db.run(joinedStreamsAndQuotes.result).futureValue

  def closeDb() = db.close()
}
