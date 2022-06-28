package com.joshcough.trollabot

import cats.implicits._
import com.joshcough.trollabot.api.{CountersDb, QuotesDb, StreamsDb}
import doobie.ConnectionIO
import doobie.implicits._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

class DatabaseSuite extends PostgresContainerSuite {

  import AssertableQuote._
  import QuotesData._

  test("Can get streams") {
    withDb {
      StreamsDb.getAllStreams.compile.toList.map(_.map(_.name))
        .map(ss => assertEquals(ss, streams.map(_.name)))
    }
  }

  test("Can insert a single quote") {
    withDb {
      insertAndGetQuote("I deserve to be trolled", "jc", daut)
    }
  }

  test("Can insert many quotes") {
    withDb {
      for {
        _ <- insertDautQuotes
        qs <- QuotesDb.getQuotes(daut.name).compile.toList
      } yield assertEquals(qs.size, 5)
    }
  }

  test("Can't insert the same quote twice") {
    withDb {
      for {
        _ <- insertAndGetQuote("only insert me once!", "jc", daut)
        e <- insertAndGetQuote("only insert me once!", "jc", daut).attempt
      } yield assertEquals(e, Left(QuoteException("quote already exists: Quote #0: only insert me once!")))
    }
  }

  test("Can delete quote") {
    withDb {
      for {
        _ <- insertDautQuotes
        randomQuote <- QuotesDb.getRandomQuote(daut.name)
        rando = randomQuote.getOrElse(fail("couldn't get random quote from daut's stream"))
        _ <- QuotesDb.deleteQuote(daut.name, rando.qid)
        qs <- QuotesDb.getQuotes(daut.name).compile.toList
      } yield assertEquals (qs.size, 4)
    }
  }

  test("Can insert quotes into many streams") {
    withDb {
      def mustBeNQuotes(s: Stream, n: Int): ConnectionIO[Unit] = for {
        qs <- QuotesDb.getQuotes(s.name).compile.toList
      } yield assertEquals (qs.size, n)

      for {
        _ <- insertDautQuotes
        _ <- insertAndGetQuote("idiota", "jc", jonslow)
        _ <- insertAndGetQuote("muy", "jc", artoftroll)

        _ <- mustBeNQuotes(daut, 5)
        _ <- mustBeNQuotes(jonslow, 1)
        _ <- mustBeNQuotes(artoftroll, 1)
      } yield ()
    }
  }

  test("Can search quotes") {
    withDb {
      for {
        _ <- insertDautQuotes
        qs <- QuotesDb.searchQuotes(daut.name, "%man%").compile.toList
        expected = List(
          AssertableQuote(Some(2),1,"come to my healing spot man!",1,"jc", deleted = false, None),
          AssertableQuote(Some(5),4,"close us man!",1,"jc", deleted = false, None)
        )
      } yield assertQuotes(qs, expected)
    }
  }

  test("Can create and increment counters") {
    def c(id: Int, name: String, count: Int) = AssertableCounter(Some(id), name, count, 1, "jc")
    def housed(count: Int) = c(1, "housed", count)
    def brutal(count: Int) = c(2, "brutal", count)
    withDb {
      for {
        // create a counter called "housed" and increment it twice
        c0 <- CountersDb.insertCounter(dautChannel, jc, "housed").map(AssertableCounter(_))
        c1 <- CountersDb.incrementCounter(dautChannel, "housed").map(AssertableCounter(_))
        c2 <- CountersDb.incrementCounter(dautChannel, "housed").map(AssertableCounter(_))

        // create another counter called "brutal" and increment it once
        _ <- CountersDb.insertCounter(dautChannel, jc, "brutal").map(AssertableCounter(_))
        _ <- CountersDb.incrementCounter(dautChannel,"brutal").map(AssertableCounter(_))

        // get all the counters
        cs <- CountersDb.getCounters("daut").compile.toList.map(_.map(AssertableCounter(_)))
      } yield
        assertEquals(c0, housed(0)) &&
        assertEquals(List(c1, c2), List(housed(1), housed(2))) &&
        assertEquals(cs, List(housed(2), brutal(1)))
    }
  }
}

case class AssertableQuote(id: Option[Int], qid: Int, text: String, channel: Int,
                           addedBy: String,  deleted: Boolean, deletedBy: Option[String])
case class AssertableCounter(id: Option[Int], name: String, count: Int, channel: Int, addedBy: String)

object AssertableQuote {
  def apply(q: Quote): AssertableQuote =
    AssertableQuote(q.id, q.qid, q.text, q.channel, q.addedBy, q.deleted, q.deletedBy)
  def assertQuotes(actual: List[Quote], expected: List[AssertableQuote]): Unit =
    munit.Assertions.assertEquals(actual.map(AssertableQuote(_)), expected)

  def assertQuote(actual: Quote, expected: AssertableQuote): Unit =
    munit.Assertions.assertEquals(AssertableQuote(actual), expected)

  implicit val assQuoteDecoder: Decoder[AssertableQuote] = deriveDecoder[AssertableQuote]
  implicit val assQuoteEncoder: Encoder[AssertableQuote] = deriveEncoder[AssertableQuote]
}

object AssertableCounter {
  def apply(c: Counter): AssertableCounter =
    AssertableCounter(c.id, c.name, c.count, c.channel, c.addedBy)
  def assertCounter(actual: List[Quote], expected: List[AssertableQuote]): Unit =
    munit.Assertions.assertEquals(actual.map(AssertableQuote(_)), expected)

  def assertQuote(actual: Counter, expected: AssertableCounter): Unit =
    munit.Assertions.assertEquals(AssertableCounter(actual), expected)

  implicit val assCounterDecoder: Decoder[AssertableCounter] = deriveDecoder[AssertableCounter]
  implicit val assCounterEncoder: Encoder[AssertableCounter] = deriveEncoder[AssertableCounter]
}
