package com.joshcough.trollabot

import cats.effect.IO
import cats.implicits._
import com.comcast.ip4s.Port
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.joshcough.trollabot.LoggingImplicits.productionLogger
import com.joshcough.trollabot.api.{CounterName, CountersDb, Quote, QuotesDb, Score, ScoresDb, Stream, StreamsDb}
import com.joshcough.trollabot.db.Migrations
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import java.sql.Timestamp
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.testcontainers.utility.DockerImageName

case class QuoteException(msg: String) extends RuntimeException

object QuotesData {

  val userName: ChatUserName = ChatUserName("artofthetroll")
  val ts: Timestamp = Timestamp.valueOf("2022-07-04 06:42:00")

  val daut: Stream = Stream(ChannelName("daut"), joined = false, userName, ts)
  val jonslow: Stream = Stream(ChannelName("jonslow_"), joined = false, userName, ts)
  val artoftroll: Stream = Stream(ChannelName("artofthetroll"), joined = true, userName, ts)
  val streams: List[Stream] = List(daut, jonslow, artoftroll)

  val dautQuotes: List[String] = List(
    "I deserve to be trolled",
    "come to my healing spot man!",
    "hope u lost villager",
    "You are proper Jordan Tati",
    "close us man!"
  )

  val dautCounters: List[CounterName] = List("housed", "brutal").map(CounterName)
}

trait PostgresContainerSuite extends CatsEffectSuite with ScalaCheckEffectSuite with TestContainersForAll {

  import QuotesData._

  override type Containers = PostgreSQLContainer

  val dautChannel: ChannelName = ChannelName("daut")
  val jcName: ChatUserName = ChatUserName("jc")
  val jc: ChatUser = ChatUser(jcName, isMod = false, subscriber = false, badges = Map())

  def insertDautQuotes: ConnectionIO[List[Quote]] =
    dautQuotes.map(q => insertAndGetQuote(q, jcName, daut)).sequence

  def insertDautCounters: ConnectionIO[Unit] =
    for {
      _ <- dautCounters.map(c => CountersDb.insertCounter(dautChannel, jc, c)).sequence
      _ <- CountersDb.incrementCounter(dautChannel, CounterName("housed"))
      _ <- CountersDb.incrementCounter(dautChannel, CounterName("housed"))
    } yield ()

  def insertDautScore: ConnectionIO[Score] =
    ScoresDb.setAll(dautChannel, "daut", 4, "viper", 0)

  override def startContainers(): Containers = {
    val myImage = DockerImageName.parse("postgres:12-alpine").
      asCompatibleSubstituteFor("postgres")
    val p = PostgreSQLContainer.Def(myImage).createContainer()
    p.start()
    p
  }

  def insertTestData: ConnectionIO[Unit] = for {
    _ <- TestQueries.deleteAllUserCommands.run
    _ <- TestQueries.deleteAllCounters.run
    _ <- TestQueries.deleteAllScores.run
    _ <- TestQueries.deleteAllQuotes.run
    _ <- TestQueries.deleteAllStreams.run
    _ <- streams.map(s => StreamsDb.insertStream(s.name, joined=s.joined, userName)).sequence
  } yield ()

  def withXa[A](f: Transactor[IO] => IO[A]): IO[A] =
    withContainers { postgres =>
      def loadConfigForTesting: IO[Configuration] = {
        val port: Port =
          Port.fromInt(postgres.jdbcUrl.split(":")(3).take(5).toInt).
            getOrElse(throw new RuntimeException(s"bad port in url ${postgres.jdbcUrl}"))
        // We have to set all these variables in order to read the config without it crashing.
        java.lang.System.setProperty("TROLLABOT_TOKEN", "trollabot_token")
        java.lang.System.setProperty("DB_HOST", postgres.host)
        java.lang.System.setProperty("DB_PORT", port.toString())
        java.lang.System.setProperty("DB_USER", postgres.username)
        java.lang.System.setProperty("DB_PASSWORD", postgres.password)
        java.lang.System.setProperty("DB_NAME", postgres.databaseName)
        Configuration.read().flatMap {
          case Left(err) => IO.raiseError(new RuntimeException(s"Couldn't read configuration ${err.prettyPrint()}"))
          // NOTE: so... for some reason the config is not really being reloaded
          // which means it gets set once for the first 'postgres' variable instance, and never updates
          // but the only thing that changes is the port, so... i hack around that here.
          // its kinda BS, but ... i guess im just going to leave it like this for now.
          case Right(config) => config.copy(db=config.db.copy(port=port)).pure[IO]
        }
      }

      for {
        config <- loadConfigForTesting
        xa = config.xa[IO]
        _ <- Migrations.migrate(config)
        _ <- insertTestData.transact(xa)
        a <- f(xa)
      } yield a
    }

  def withDb[A](c: ConnectionIO[A]): IO[A] = withXa(c.transact(_))

  def insertAndGetQuote(text: String, user: ChatUserName, stream: Stream): ConnectionIO[Quote] = {
    val dbAction = for {
      newQ <- QuotesDb.insertQuote(text, user, stream.name).map(
        _.fold(q => throw QuoteException(s"quote already exists: ${q.display}"), identity)
      )
      newQO_ <- QuotesDb.getQuote(stream.name, newQ.qid)
      newQ_ = newQO_.getOrElse(fail(
        s"couldn't retrieve inserted quote: text: $text, user: $user, stream: $stream"
      ))
    } yield (newQ, newQ_)

    dbAction.map { case (newQ, newQ_) =>
      assertEquals(newQ.text, text)
      assertEquals(newQ_.text, text)
      assertEquals(newQ, newQ_)
      newQ
    }
  }
}
