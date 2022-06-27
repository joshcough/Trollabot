package com.joshcough.trollabot

import cats.effect.IO
import cats.implicits._
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.joshcough.trollabot.api.{CountersDb, QuotesDb, StreamsDb}
import com.joshcough.trollabot.twitch.{ChannelName, ChatUser, ChatUserName}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.testcontainers.utility.DockerImageName

import java.sql.DriverManager

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

  val dautCounters: List[String] = List("housed", "brutal")
}

trait PostgresContainerSuite extends CatsEffectSuite with ScalaCheckEffectSuite with TestContainersForAll {

  import QuotesData._

  override type Containers = PostgreSQLContainer

  val dautChannel: ChannelName = ChannelName("daut")
  val jc: ChatUser = ChatUser(ChatUserName("jc"), isMod = false, subscriber = false, badges = Map())

  def insertDautQuotes: ConnectionIO[List[Unit]] =
    dautQuotes.map(q => insertAndGetQuote(q, "jc", daut)).sequence

  def insertDautCounters: ConnectionIO[Unit] =
    for {
      _ <- dautCounters.map(c => CountersDb.insertCounter(dautChannel, jc, c)).sequence
      _ <- CountersDb.incrementCounter(dautChannel, "housed")
      _ <- CountersDb.incrementCounter(dautChannel, "housed")
    } yield ()

  override def startContainers(): Containers = {
    val myImage = DockerImageName.parse("postgres:12-alpine").
      asCompatibleSubstituteFor("postgres")
    val p = PostgreSQLContainer.Def(myImage).createContainer()
    p.start()
    p
  }

  def setupDB: ConnectionIO[Unit] = for {
    _ <- Queries.recreateSchema
    _ <- Queries.deleteAllQuotes.run
    _ <- Queries.deleteAllStreams.run
    _ <- streams.map(s => StreamsDb.insertStream(s.name)).sequence
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
      newQ <- QuotesDb.insertQuote(text, user, stream.name)
      newQO_ <- QuotesDb.getQuote(stream.name, newQ.qid)
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
