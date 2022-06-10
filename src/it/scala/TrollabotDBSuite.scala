import com.joshcough.trollabot.{Stream, TrollabotDb}
import doobie.ConnectionIO
import doobie.implicits._

class TrollabotDBSuite extends PostgresContainerSuite {

  import QuotesData._

  test("Can get streams") {
    withDb {
      TrollabotDb.getAllStreams.compile.toList.map(_.map(_.name))
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
        qs <- TrollabotDb.getAllQuotesForStream(daut.name).compile.toList
      } yield assertEquals(qs.size, 5)
    }
  }

  test("Can delete quote") {
    withDb {
      for {
        _ <- insertDautQuotes
        randomQuote <- TrollabotDb.getRandomQuoteForStream(daut.name)
        rando = randomQuote.getOrElse(fail("couldn't get random quote from daut's stream"))
        _ <- TrollabotDb.deleteQuote(daut.name, rando.qid)
        qs <- TrollabotDb.getAllQuotesForStream(daut.name).compile.toList
      } yield assertEquals (qs.size, 4)
    }
  }

  test("Can insert quotes into many streams") {
    withDb {
      def mustBeNQuotes(s: Stream, n: Int): ConnectionIO[Unit] = for {
        qs <- TrollabotDb.getAllQuotesForStream(s.name).compile.toList
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
}
