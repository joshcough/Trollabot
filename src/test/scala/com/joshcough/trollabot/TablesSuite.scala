package com.joshcough.trollabot

import cats.effect.IO
import doobie.Transactor
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

class TablesSuite extends funsuite.AnyFunSuite with BeforeAndAfter with ScalaFutures {

  var db: TrollabotDb = _

  val daut: Stream = Stream(None, "daut", joined = false)
  val jonslow: Stream = Stream(None, "jonslow_", joined = false)
  val artoftroll: Stream = Stream(None, "artofthetroll", joined = true)
  val streams: Seq[Stream] = List(daut, jonslow, artoftroll)

  before {
    val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/slick?user=slick&password=testes"
    )
    db = TrollabotDb(xa)
    db.createSchemaIO()
    streams.foreach(s => db.insertStreamIO(s.name))
  }

  test("Streams") {
    val streams = db.getAllStreamsIO
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

    assert(db.getAllQuotesForStreamIO(daut.name).size === 5)

    val Some(rando) = db.getRandomQuoteIO(daut.name)
    db.deleteQuoteIO(daut.name, rando.qid)

    assert(db.getAllQuotesForStreamIO(daut.name).size === 4)

    insertAndGetQuote("This place sucks!", "jc", daut)

    insertAndGetQuote("idiota", "jc", jonslow)
    insertAndGetQuote("retardo", "jc",artoftroll)

    assert(db.getAllQuotesForStreamIO("jonslow_").size === 1)
    assert(db.getAllQuotesForStreamIO("artofthetroll").size === 1)
  }

//  after { db.closeDbIO() }
}

/*
object doobsMain {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/slick?user=slick&password=testes"
  )

  def go(streamName:String): Unit = {
    (dropQuotesTable.run, dropStreamsTable.run).mapN(_ + _).transact(xa).unsafeRunSync()
    (createStreamsTable.run, createQuotesTable.run).mapN(_ + _).transact(xa).unsafeRunSync()

    println("inserting stream")
    insertStream(Stream(None, streamName, joined = false)).run.transact(xa).unsafeRunSync()

    println("inserting some quotes")
    insertQuote("hello", "troll", streamName).compile.toList.transact(xa).unsafeRunSync()
    insertQuote("goodbye", "zeke", streamName).compile.toList.transact(xa).unsafeRunSync()
    insertQuote("11", "viper", streamName).compile.toList.transact(xa).unsafeRunSync()

    println("random quote:")
    getRandomQuoteForStream(streamName).compile.toList.transact(xa).unsafeRunSync().foreach(println)
    println("all quotes:")
    getAllQuotesForStream(streamName).compile.toList.transact(xa).unsafeRunSync().foreach(println)
    println("all streams:")
    getAllStreams.compile.toList.transact(xa).unsafeRunSync().foreach(println)
    println("joined streams:")
    getJoinedStreams.compile.toList.transact(xa).unsafeRunSync().foreach(println)
    println("stream id:")
    getStreamId(streamName).compile.toList.transact(xa).unsafeRunSync().foreach(println)

    println("next qid:")
    nextQidForChannel(streamName).compile.toList.transact(xa).unsafeRunSync().foreach(println)

    println("inserting stream")
    val newStreamName = "test-delete-me"
    val x = insertStream(Stream(None, newStreamName, joined = false)).run.transact(xa).unsafeRunSync()
    println(s"x: $x")

    println("stream id of inserted stream:")
    getStreamId(newStreamName).compile.toList.transact(xa).unsafeRunSync().foreach(println)

    println("deleting that stream")
    val y = deleteStream(newStreamName).run.transact(xa).unsafeRunSync()
    println(s"x: $y")

    println("stream id of inserted stream:")
    getStreamId(newStreamName).compile.toList.transact(xa).unsafeRunSync().foreach(println)
  }
}
 */