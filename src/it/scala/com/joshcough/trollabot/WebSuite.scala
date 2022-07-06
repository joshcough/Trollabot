package com.joshcough.trollabot

import cats.effect.IO
import cats.implicits._
import com.joshcough.trollabot.QuotesData.daut
import com.joshcough.trollabot.api.{CounterName, Counters, Quotes}
import com.joshcough.trollabot.web.Routes
import doobie.Transactor
import doobie.implicits._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.decode
import org.http4s.Method._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Request}

class WebSuite extends PostgresContainerSuite {

  val q2: AssertableQuote = AssertableQuote(Some(2), 1, "come to my healing spot man!", daut.name, ChatUserName("jc"), deleted = false, None)
  val q5: AssertableQuote = AssertableQuote(Some(5), 4, "close us man!", daut.name, ChatUserName("jc"), deleted = false, None)

  val c1: AssertableCounter = AssertableCounter(Some(1), CounterName("housed"), 2, dautChannel, ChatUserName("jc"))
  val c2: AssertableCounter = AssertableCounter(Some(2), CounterName("brutal"), 0, dautChannel, ChatUserName("jc"))

  test("/quote returns a quote") {
    withData { xa =>
      assertIO(QuotesRequestRunner.getQuote(xa, "daut", 1), Option(q2))
    }
  }

  test("can search for quotes") {
    withData { xa =>
      assertIO(QuotesRequestRunner.search(xa, "daut", "man"), List(q2, q5))
    }
  }

  test("can get counters") {
    withData { xa =>
      assertIO(CountersRequestRunner.getCounters(xa, "daut"), List(c1, c2))
    }
  }

  def withData[A](f: Transactor[IO] => IO[A]): IO[A] =
    withXa { xa => insertDautQuotes.transact(xa) *> insertDautCounters.transact(xa) *> f(xa) }
}

object QuotesRequestRunner {
  def apply(xa: Transactor[IO]): RequestRunner = RequestRunner(Routes.quoteRoutes(Quotes.impl[IO](xa)))

  def getQuote(xa: Transactor[IO], streamName: String, qid: Int): IO[Option[AssertableQuote]] =
    apply(xa).runRequest[Option[AssertableQuote]](GET(uri"/quotes" / streamName / qid))

  def search(xa: Transactor[IO], streamName: String, like: String): IO[List[AssertableQuote]] =
    apply(xa).runRequestStream[AssertableQuote](GET(uri"/quotes" / streamName / s"%$like%"))
}

object CountersRequestRunner {
  def apply(xa: Transactor[IO]): RequestRunner = RequestRunner(Routes.counterRoutes(Counters.impl[IO](xa)))

  def getCounters(xa: Transactor[IO], streamName: String): IO[List[AssertableCounter]] =
    apply(xa).runRequestStream[AssertableCounter](GET(uri"/counters" / streamName))
}

case class RequestRunner(routes: org.http4s.HttpRoutes[IO]) {
  val client: Client[IO] = Client.fromHttpApp(routes.orNotFound)

  def runRequest[A](req: Request[IO])(implicit decoder: EntityDecoder[IO, A]): IO[A] =
    client.expect[A](req)

  def runRequestStream[A](req: Request[IO])(implicit decoder: Decoder[A]): IO[List[A]] =
    client.stream(req)
      .flatMap(x => x.bodyText.map(decode[A](_)))
      .compile.toList.map(_.sequence)
      .map(x => x.fold(e => throw e, a => a))
}