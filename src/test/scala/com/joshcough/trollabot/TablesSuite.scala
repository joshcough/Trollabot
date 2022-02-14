package com.joshcough.trollabot

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import slick.jdbc.PostgresProfile.api._

class TablesSuite extends funsuite.AnyFunSuite with BeforeAndAfter with ScalaFutures {

  var db: TrollabotDb = _

  val daut: Stream = Stream(None, "daut", joined = false)
  val jonslow: Stream = Stream(None, "jonslow_", joined = false)
  val artoftroll: Stream = Stream(None, "artofthetroll", joined = true)
  val streams: Seq[Stream] = List(daut, jonslow, artoftroll)

  before {
    db = TrollabotDb(Database.forConfig("db"))
    db.createSchemaIO()
    streams.foreach(db.insertStreamIO)
  }

  test("tables") {
    val tables = db.getTablesIO()
    assert(tables.size >= 2)
    List("streams", "quotes").foreach(t => assert(tables.count(_.name.name.equalsIgnoreCase(t)) == 1))
  }

  test("Streams") {
    val streams = db.getAllStreamsIO()
    assert(streams.size === 3)
  }

  test("com.joshcough.trollabot.Quotes works") {
    def insertAndGetQuote(text: String, user: String, stream: Stream): Assertion = {
      val Some(newQ) = db.insertQuoteIO(text, user, stream.name)
      assert(newQ.text === text)
      val Some(newQ_) = db.getQuoteByQidIO(stream.name, newQ.qid)
      assert(newQ_.text === text)
      assert(newQ === newQ_)
    }

    insertAndGetQuote("I deserve to be trolled", "jc", daut)
    insertAndGetQuote("come to my healing spot man!", "jc",daut)
    insertAndGetQuote("hope u lost villager", "jc",daut)
    insertAndGetQuote("You are proper Jordan Tati", "jc",daut)
    insertAndGetQuote("close us man!", "jc",daut)

    assert(db.getAllQuotesIO(daut.name).size === 5)

    val Some(rando) = db.getRandomQuoteIO(daut.name)
    db.deleteQuoteIO(daut.name, rando.qid)

    assert(db.getAllQuotesIO(daut.name).size === 4)

    insertAndGetQuote("This place sucks!", "jc", daut)
    insertAndGetQuote("idiota", "jc", jonslow)
    insertAndGetQuote("retardo", "jc",artoftroll)

    assert(db.getAllQuotesIO("jonslow_").size === 1)
    assert(db.getAllQuotesIO("artofthetroll").size === 1)
  }

  after { db.closeDbIO() }
}
