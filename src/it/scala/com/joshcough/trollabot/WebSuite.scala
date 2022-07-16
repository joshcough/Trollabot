package com.joshcough.trollabot

import cats.effect.IO
import cats.implicits._
import com.joshcough.trollabot.QuotesData.{artoftroll, daut, dautQuotes, jonslow}
import com.joshcough.trollabot.api.{CounterName, Counters, Quotes, Score, Scores, Stream, Streams, UserCommands}
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

  val qs@List(q1, q2, q3, q4, q5) = dautQuotes.zipWithIndex.map { case (s, i) =>
    AssertableQuote(i, s, daut.name, ChatUserName("jc"), deleted = false, None)
  }

  val c1: AssertableCounter = AssertableCounter(CounterName("housed"), 2, dautChannel, ChatUserName("jc"))
  val c2: AssertableCounter = AssertableCounter(CounterName("brutal"), 0, dautChannel, ChatUserName("jc"))

  test("/streams returns streams") {
    withData { xa =>
      assertIO(StreamsRequestRunner.getStreams(xa), List(daut, jonslow, artoftroll).map(AssertableStream(_)))
    }
  }

  test("/streams/joined returns joined streams") {
    withData { xa =>
      assertIO(StreamsRequestRunner.getJoinedStreams(xa), List(artoftroll).map(AssertableStream(_)))
    }
  }

  test("can get streams by name") {
    withData { xa =>
      def go(s: Stream): IO[Unit] =
        assertIO(StreamsRequestRunner.getStream(xa, s.name), Some(s).map(AssertableStream(_)))

      go(artoftroll) *> go(daut) *> go(jonslow)
    }
  }

  test("/quotes/stream_name/id returns a quote") {
    withData { xa =>
      assertIO(QuotesRequestRunner.getQuote(xa, daut.name, 1), Option(q2))
    }
  }

  test("/quotes/stream_name returns quotes") {
    withData { xa =>
      assertIO(QuotesRequestRunner.getQuotes(xa, daut.name), qs)
    }
  }

  test("can search for quotes") {
    withData { xa =>
      assertIO(QuotesRequestRunner.search(xa, daut.name, "man"), List(q2, q5))
    }
  }

  test("can get counters") {
    withData { xa =>
      assertIO(CountersRequestRunner.getCounters(xa, daut.name), List(c1, c2))
    }
  }

  test("can get scores") {
    withData { xa =>
      val dautScore = Score(dautChannel, Some("daut"), Some("viper"), 4, 0)
      val trollScore = Score.empty(artoftroll.name)
      for {
        _ <- assertIO(ScoresRequestRunner.getScores(xa, daut.name), List(dautScore))
        _ <- assertIO(ScoresRequestRunner.getScores(xa, artoftroll.name), List(trollScore))
      } yield ()
    }
  }

  def withData[A](f: Transactor[IO] => IO[A]): IO[A] =
    withXa { xa =>
      for {
        _ <- List(insertDautQuotes, insertDautCounters, insertDautScore).traverse(_.transact(xa))
        a <- f(xa)
      } yield a
    }
}

object StreamsRequestRunner {
  def apply(xa: Transactor[IO]): RequestRunner = RequestRunner(Routes.streamRoutes(Streams.impl[IO](xa)))

  def getStreams(xa: Transactor[IO]): IO[List[AssertableStream]] =
    apply(xa).runRequestStream[AssertableStream](GET(uri"/streams"))

  def getJoinedStreams(xa: Transactor[IO]): IO[List[AssertableStream]] =
    apply(xa).runRequestStream[AssertableStream](GET(uri"/streams" / "joined"))

  def getStream(xa: Transactor[IO], channelName: ChannelName): IO[Option[AssertableStream]] =
    apply(xa).runRequest[Option[AssertableStream]](GET(uri"/streams" / channelName.name))
}

object QuotesRequestRunner {
  def apply(xa: Transactor[IO]): RequestRunner = RequestRunner(Routes.quoteRoutes(Quotes.impl[IO](xa)))

  def getQuote(xa: Transactor[IO], channelName: ChannelName, qid: Int): IO[Option[AssertableQuote]] =
    apply(xa).runRequest[Option[AssertableQuote]](GET(uri"/quotes" / channelName.name / qid))

  def getQuotes(xa: Transactor[IO], channelName: ChannelName): IO[List[AssertableQuote]] =
    apply(xa).runRequestStream[AssertableQuote](GET(uri"/quotes" / channelName.name))

  def search(xa: Transactor[IO], channelName: ChannelName, like: String): IO[List[AssertableQuote]] =
    apply(xa).runRequestStream[AssertableQuote](GET(uri"/quotes" / channelName.name / s"%$like%"))
}

object CountersRequestRunner {
  def apply(xa: Transactor[IO]): RequestRunner = RequestRunner(Routes.counterRoutes(Counters.impl[IO](xa)))

  def getCounters(xa: Transactor[IO], channelName: ChannelName): IO[List[AssertableCounter]] =
    apply(xa).runRequestStream[AssertableCounter](GET(uri"/counters" / channelName.name))
}

object ScoresRequestRunner {
  def apply(xa: Transactor[IO]): RequestRunner = RequestRunner(Routes.scoreRoutes(Scores.impl[IO](xa)))

  def getScores(xa: Transactor[IO], channelName: ChannelName): IO[List[Score]] =
    apply(xa).runRequestStream[Score](GET(uri"/scores" / channelName.name))
}

object UserCommandsRequestRunner {
  def apply(xa: Transactor[IO]): RequestRunner = RequestRunner(Routes.userCommandRoutes(UserCommands.impl[IO](xa)))

  def getUserCommands(xa: Transactor[IO], channelName: ChannelName): IO[List[AssertableUserCommand]] =
    apply(xa).runRequestStream[AssertableUserCommand](GET(uri"/userCommands" / channelName.name))
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