import cats.effect.IO
import com.joshcough.trollabot.web.{Quotes, Routes}
import doobie.Transactor
import doobie.implicits._
import org.http4s._
import org.http4s.implicits._

class TrollabotWebSuite extends PostgresContainerSuite {

  test("Quote returns status code 200") {
    withXa { xa =>
      insertDautQuotes.transact(xa) *>
      assertIO(retQuote(xa).map(_.status), Status.Ok)
    }
  }

  test("Quote returns a quote") {
    withXa { xa =>
      for {
        _ <- insertDautQuotes.transact(xa)
        expected = "{\"id\":2,\"qid\":1,\"text\":\"come to my healing spot man!\",\"userId\":\"jc\",\"channel\":1}"
        _ <- assertIO(retQuote(xa).flatMap(_.as[String]), expected)
      } yield ()
    }
  }

  // TODO: take stream, qid as arguments
  private def retQuote(xa: Transactor[IO]): IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/quote/daut/1")
    val quotes = Quotes.impl[IO](xa)
    Routes.quoteRoutes(quotes).orNotFound(getHW)
  }
}
