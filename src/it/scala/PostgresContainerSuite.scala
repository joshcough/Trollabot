import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.joshcough.trollabot.{Stream, TrollabotDb}

import java.sql.DriverManager
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
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

trait PostgresContainerSuite extends CatsEffectSuite with ScalaCheckEffectSuite with TestContainersForAll {

  import QuotesData._

  override type Containers = PostgreSQLContainer

  def insertDautQuotes: ConnectionIO[List[Unit]] =
    dautQuotes.map(q => insertAndGetQuote(q, "jc", daut)).sequence

  override def startContainers(): Containers = {
    val myImage = DockerImageName.parse("postgres:12-alpine").
      asCompatibleSubstituteFor("postgres")
    val p = PostgreSQLContainer.Def(myImage).createContainer()
    p.start()
    p
  }

  def setupDB: ConnectionIO[Unit] = for {
    _ <- TrollabotDb.createSchema
    _ <- TrollabotDb.deleteAllQuotes
    _ <- TrollabotDb.deleteAllStreams
    _ <- streams.map(s => TrollabotDb.insertStream(s.name)).sequence
  } yield ()

  def withXa[A](f: Transactor[IO] => IO[A]): IO[A] =
    withContainers { postgres =>
      val xa = Transactor.fromConnection[IO](
        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
      )
      setupDB.transact(xa) *> f(xa)
    }

  def withDb[A](c: ConnectionIO[A]): IO[A] = withXa(c.transact(_))

  def insertAndGetQuote(text: String, user: String, stream: Stream): ConnectionIO[Unit] = {
    val dbAction = for {
      newQ <- TrollabotDb.insertQuote(text, user, stream.name)
      newQO_ <- TrollabotDb.getQuoteByQid(stream.name, newQ.qid)
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
