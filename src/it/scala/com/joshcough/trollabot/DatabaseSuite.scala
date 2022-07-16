package com.joshcough.trollabot

import cats.implicits._
import com.joshcough.trollabot.api.{Counter, CounterName, CountersDb, Quote, QuotesDb, Score, ScoresDb, Stream, StreamsDb, UserCommand, UserCommandName, UserCommandsDb}
import doobie.ConnectionIO
import doobie.implicits._

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
      insertAndGetQuote("I deserve to be trolled", jcName, daut)
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
        _ <- insertAndGetQuote("only insert me once!", jcName, daut)
        e <- insertAndGetQuote("only insert me once!", jcName, daut).attempt
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
        _ <- insertAndGetQuote("idiota", jcName, jonslow)
        _ <- insertAndGetQuote("muy", jcName, artoftroll)

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
          AssertableQuote(1,"come to my healing spot man!",daut.name,ChatUserName("jc"), deleted = false, None),
          AssertableQuote(4,"close us man!",daut.name,ChatUserName("jc"), deleted = false, None)
        )
      } yield assertQuotes(qs, expected)
    }
  }

  test("Can create and increment counters") {
    def c(name: CounterName, count: Int) = AssertableCounter(name, count, dautChannel, ChatUserName("jc"))
    def housed(count: Int) = c(CounterName("housed"), count)
    def brutal(count: Int) = c(CounterName("brutal"), count)
    withDb {
      for {
        // create a counter called "housed" and increment it twice
        c0 <- CountersDb.insertCounter(dautChannel, jc, CounterName("housed")).map(AssertableCounter(_))
        c1 <- CountersDb.incrementCounter(dautChannel, CounterName("housed")).map(_.map(AssertableCounter(_)))
        c2 <- CountersDb.incrementCounter(dautChannel, CounterName("housed")).map(_.map(AssertableCounter(_)))

        // create another counter called "brutal" and increment it once
        _ <- CountersDb.insertCounter(dautChannel, jc, CounterName("brutal")).map(AssertableCounter(_))
        _ <- CountersDb.incrementCounter(dautChannel,CounterName("brutal")).map(_.map(AssertableCounter(_)))

        // get all the counters
        cs <- CountersDb.getCounters(ChannelName("daut")).compile.toList.map(_.map(AssertableCounter(_)))
      } yield
        assertEquals(c0, housed(0)) &&
        assertEquals(List(c1, c2), List(Some(housed(1)), Some(housed(2)))) &&
        assertEquals(cs, List(housed(2), brutal(1)))
    }
  }

  test("can set scores and players") {
    withDb {
      for {
        s0 <- ScoresDb.getScore(dautChannel)
        s1 <- ScoresDb.setPlayer1(dautChannel, "daut")
        s2 <- ScoresDb.setPlayer2(dautChannel, "mbl")
        s3 <- ScoresDb.setPlayer1Score(dautChannel, 1)
        s4 <- ScoresDb.setPlayer2Score(dautChannel, 1)
        s5 <- ScoresDb.setScore(dautChannel, 3, 2)
        s6 <- ScoresDb.setAll(dautChannel, "viper", 4, "hera", 0)
      } yield assertEquals(s0, Score(dautChannel, None, None, 0, 0)) &&
        assertEquals(s1, Score(dautChannel, Some("daut"), None, 0, 0)) &&
        assertEquals(s2, Score(dautChannel, Some("daut"), Some("mbl"), 0, 0)) &&
        assertEquals(s3, Score(dautChannel, Some("daut"), Some("mbl"), 1, 0)) &&
        assertEquals(s4, Score(dautChannel, Some("daut"), Some("mbl"), 1, 1)) &&
        assertEquals(s5, Score(dautChannel, Some("daut"), Some("mbl"), 3, 2)) &&
        assertEquals(s6, Score(dautChannel, Some("viper"), Some("hera"), 4, 0))
    }
  }

  test("can create and get user commands") {
    withDb {
      for {
        // create
        c0 <- UserCommandsDb.insertUserCommand(dautChannel, jc, UserCommandName("housed"), "Been housed 5 times!")
        c1 <- UserCommandsDb.getUserCommand(dautChannel,  UserCommandName("housed"))
        // update
        c2 <- UserCommandsDb.editUserCommand(dautChannel, UserCommandName("housed"), "Been housed 6 times!")
        c3 <- UserCommandsDb.getUserCommand(dautChannel,  UserCommandName("housed"))
        // delete
        deleted <- UserCommandsDb.deleteUserCommand(dautChannel, UserCommandName("housed"))
        c5 <- UserCommandsDb.getUserCommand(dautChannel,  UserCommandName("housed"))
      } yield {
        // getting after inserting should get the thing we just inserted
        assertEquals(Option(c0), c1) &&
        // getting after editing should get the new body
          assertEquals(c2, c3) && assert(c2.isDefined) &&
        // delete should work, and getting afterwards should yield nothing
          assert(deleted) && assertEquals(c5, None)
      }
    }
  }
}

case class AssertableStream(name: ChannelName, joined: Boolean, addedBy: ChatUserName)
case class AssertableQuote(qid: Int, text: String, channel: ChannelName,
                           addedBy: ChatUserName,  deleted: Boolean, deletedBy: Option[ChatUserName])
case class AssertableCounter(name: CounterName, count: Int, channel: ChannelName, addedBy: ChatUserName)
case class AssertableUserCommand(name: UserCommandName, body: String, channel: ChannelName, addedBy: ChatUserName)

object AssertableStream {
  def apply(s: Stream): AssertableStream = AssertableStream(s.name, s.joined, s.addedBy)
  def assertStreams(actual: List[Stream], expected: List[AssertableStream]): Unit =
    munit.Assertions.assertEquals(actual.map(AssertableStream(_)), expected)

  def assertStream(actual: Stream, expected: AssertableStream): Unit =
    munit.Assertions.assertEquals(AssertableStream(actual), expected)
}

object AssertableQuote {
  def apply(q: Quote): AssertableQuote =
    AssertableQuote(q.qid, q.text, q.channel, q.addedBy, q.deleted, q.deletedBy)
  def assertQuotes(actual: List[Quote], expected: List[AssertableQuote]): Unit =
    munit.Assertions.assertEquals(actual.map(AssertableQuote(_)), expected)

  def assertQuote(actual: Quote, expected: AssertableQuote): Unit =
    munit.Assertions.assertEquals(AssertableQuote(actual), expected)
}

object AssertableCounter {
  def apply(c: Counter): AssertableCounter =
    AssertableCounter(c.name, c.count, c.channel, c.addedBy)
  def assertCounter(actual: List[Quote], expected: List[AssertableQuote]): Unit =
    munit.Assertions.assertEquals(actual.map(AssertableQuote(_)), expected)

  def assertQuote(actual: Counter, expected: AssertableCounter): Unit =
    munit.Assertions.assertEquals(AssertableCounter(actual), expected)
}

object AssertableUserCommand {
  def apply(c: UserCommand): AssertableUserCommand =
    AssertableUserCommand(c.name, c.body, c.channel, c.addedBy)
  def assertUserCommand(actual: List[Quote], expected: List[AssertableQuote]): Unit =
    munit.Assertions.assertEquals(actual.map(AssertableQuote(_)), expected)

  def assertQuote(actual: UserCommand, expected: AssertableUserCommand): Unit =
    munit.Assertions.assertEquals(AssertableUserCommand(actual), expected)
}
