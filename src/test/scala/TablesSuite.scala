import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import slick.jdbc.PostgresProfile.api._

class TablesSuite extends funsuite.AnyFunSuite with BeforeAndAfter with ScalaFutures {

  var actions: TrollabotDbActions = _

  before {
    actions = TrollabotDbActions(Database.forConfig("db"))
  }

  test("Creating the Schema works") {
    actions.createSchema()
    val tables = actions.getTables()
    assert(tables.size >= 2)
    assert(tables.count(_.name.name.equalsIgnoreCase("quotes")) == 1)
  }

  test("Quotes works") {
    actions.insertQuote(Quote(1, "I deserve to be trolled", 1, "joshcough",0))
    actions.insertQuote(Quote(2, "come to my healing spot man!", 2, "joshcough",0))
    actions.insertQuote(Quote(3, "hope u lost villager", 3, "joshcough",0))
    actions.insertQuote(Quote(4, "You are proper Jordan Tati", 4, "joshcough",0))
    actions.insertQuote(Quote(5, "close us man!", 5, "joshcough",0))

    println("all quotes:")
    actions.getAllQuotes.foreach(println)

    println("random quote:")
    actions.getRandomQuote.foreach(println)

    actions.insertQuote(Quote(2685, "This place sucks!", 2413, "joshcough",0))

    println("single quote:")
    println(actions.getQuoteByQid(3))

    println("delete:")
    println(actions.deleteQuoteByQid(2413))
  }

  test("Streams works") {
    actions.insertStream(Stream(0, "daut"))
    actions.insertStream(Stream(1, "jonslow_"))
    val results2 : Seq[Stream] = actions.getStreams()
    println("streams:")
    results2.foreach(println)
  }

  test("joins"){
    val results: Seq[(Stream, Quote)] = actions.getJoinedStreamsAndQuotes()
    print("joins")
    results.foreach(println)
  }

  after { actions.closeDb() }
}
