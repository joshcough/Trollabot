import cats.effect.IO
import cats.implicits._
import doobie.Transactor
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.joshcough.trollabot.web.{Quotes, Routes}
import com.joshcough.trollabot.{Stream, TrollabotDb}

import java.sql.DriverManager
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.http4s._
import org.http4s.implicits._
import org.testcontainers.utility.DockerImageName

object QuotesData {
  val daut: Stream = Stream(None, "daut", joined = false)
  val jonslow: Stream = Stream(None, "jonslow_", joined = false)
  val artoftroll: Stream = Stream(None, "artofthetroll", joined = true)
  val streams: List[Stream] = List(daut, jonslow, artoftroll)

  val dautQuotes: List[String] = List(
    "I deserve to be trolled",
    "come to my healing spot man!",
    "hope u lost villager",
    "You are proper Jordan Tati",
    "close us man!"
  )
}

class TrollabotSuite extends CatsEffectSuite with ScalaCheckEffectSuite with TestContainersForAll {

  override type Containers = PostgreSQLContainer

  import QuotesData._

  test("Can get streams") {
    withDb { db =>
      db.getAllStreams.compile.toList.map(_.map(_.name)).assertEquals(streams.map(_.name))
    }
  }

  test("Can insert a single quote") {
    withDb { db =>
      insertAndGetQuote(db, "I deserve to be trolled", "jc", daut)
    }
  }

  test("Can insert many quotes") {
    withDb { db =>
      for {
        _ <- insertDautQuotes(db)
        qs <- db.getAllQuotesForStream(daut.name).compile.toList
      } yield assertEquals(qs.size, 5)
    }
  }

  test("Can delete quote") {
    withDb { db =>
      for {
        _ <- insertDautQuotes(db)
        randomQuote <- db.getRandomQuoteForStream(daut.name).compile.last
        rando = randomQuote.getOrElse(fail("couldn't get random quote from daut's stream"))
        _ <- db.deleteQuote(daut.name, rando.qid)
        qs <- db.getAllQuotesForStream(daut.name).compile.toList
      } yield assertEquals (qs.size, 4)
    }
  }

  test("Can insert quotes into many streams") {
    withDb { db =>
      def mustBeNQuotes(s: Stream, n: Int): IO[Unit] = for {
        qs <- db.getAllQuotesForStream(s.name).compile.toList
      } yield assertEquals (qs.size, n)

      for {
        _ <- insertDautQuotes(db)
        _ <- insertAndGetQuote (db, "idiota", "jc", jonslow)
        _ <- insertAndGetQuote (db, "muy", "jc", artoftroll)

        _ <- mustBeNQuotes(daut, 5)
        _ <- mustBeNQuotes(jonslow, 1)
        _ <- mustBeNQuotes(artoftroll, 1)
      } yield ()
    }
  }

  test("Quote returns status code 200") {
    withDb { db =>
      for {
        _ <- insertDautQuotes(db)
        _ <- assertIO(retQuote(db).map(_.status), Status.Ok)
      } yield ()
    }
  }

  test("Quote returns a quote") {
    withDb { db =>
      for {
        _ <- insertDautQuotes(db)
        expected = "{\"id\":2,\"qid\":1,\"text\":\"come to my healing spot man!\",\"userId\":\"jc\",\"channel\":1}"
        _ <- assertIO(retQuote(db).flatMap(_.as[String]), expected)
      } yield ()
    }
  }

  // TODO: take stream, qid as arguments
  private def retQuote(db: TrollabotDb[IO]): IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/quote/daut/1")
    val quotes = Quotes.impl[IO](db)
    Routes.quoteRoutes(quotes).orNotFound(getHW)
  }

  // helper functions below

  def insertDautQuotes(db: TrollabotDb[IO]): IO[List[Unit]] =
    dautQuotes.map(q => insertAndGetQuote(db, q, "jc", daut)).sequence

  override def startContainers(): Containers = {
    val myImage = DockerImageName.parse("postgres:12-alpine").
      asCompatibleSubstituteFor("postgres")
    val p = PostgreSQLContainer.Def(myImage).createContainer()
    p.start()
    p
  }

  def withDb[A](f: TrollabotDb[IO] => IO[A]): IO[Unit] =
    withContainers { postgres =>
      val db = TrollabotDb[IO](Transactor.fromConnection(
        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
      ))
      for {
        _ <- db.createSchema
        _ <- db.deleteAllQuotes
        _ <- db.deleteAllStreams
        _ <- streams.map(s => db.insertStream(s.name)).sequence
        _ <- f(db)
      } yield ()
    }

  def insertAndGetQuote(db: TrollabotDb[IO], text: String, user: String, stream: Stream): IO[Unit] = {
    val dbAction = for {
      newQ <- db.insertQuote(text, user, stream.name)
      newQO_ <- db.getQuoteByQid(stream.name, newQ.qid).compile.last
      newQ_ = newQO_.getOrElse(fail(
        s"couldn't retrieve inserted quote: text: $text, user: $user, stream: $stream"
      ))

    } yield (newQ, newQ_)

    dbAction.map { case (newQ, newQ_) =>
      assertEquals(newQ.text, text)
      assertEquals(newQ_.text, text)
      assertEquals(newQ, newQ_)
    }
  }
}
