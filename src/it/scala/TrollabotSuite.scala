import doobie.Transactor
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.joshcough.trollabot.TrollabotDb
import com.joshcough.trollabot.Stream
import java.sql.DriverManager
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.testcontainers.utility.DockerImageName

class TrollabotSuite extends CatsEffectSuite with ScalaCheckEffectSuite with TestContainersForAll {

  override type Containers = PostgreSQLContainer

  val daut: Stream = Stream(None, "daut", joined = false)
  val jonslow: Stream = Stream(None, "jonslow_", joined = false)
  val artoftroll: Stream = Stream(None, "artofthetroll", joined = true)
  val streams: Seq[Stream] = List(daut, jonslow, artoftroll)

  override def startContainers(): Containers = {
    val myImage = DockerImageName.parse("postgres:12-alpine").asCompatibleSubstituteFor("postgres")
    val p = PostgreSQLContainer.Def(myImage).createContainer()
    p.start()
    p
  }

  def dbTest[A](name: String)(f: TrollabotDb => A): Unit = test(name){
    withContainers { postgres =>
      f(TrollabotDb(Transactor.fromConnection(
        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
      )))
    }
  }

  def insertAndGetQuote(db: TrollabotDb, text: String, user: String, stream: Stream): Unit = {
    val dbAction = for {
      newQ <- db.insertQuote(text, user, stream.name).stream
      newQ_ <- db.getQuoteByQid(stream.name, newQ.qid).stream
    } yield (newQ, newQ_)

    val Some((newQ, newQ_)) = db.runStream(dbAction).headOption

    assert(newQ.text == text)
    assert(newQ_.text == text)
    assert(newQ == newQ_)
  }


  dbTest("Startup") { db =>
    db.transact(db.recreateSchema)
    streams.foreach(s => db.runUpdate(db.insertStream(Stream(None, s.name, joined = false))))
  }

  dbTest("Main") { db =>
    insertAndGetQuote(db, "I deserve to be trolled", "jc", daut)
    insertAndGetQuote(db, "come to my healing spot man!", "jc", daut)
    insertAndGetQuote(db, "hope u lost villager", "jc", daut)
    insertAndGetQuote(db, "You are proper Jordan Tati", "jc", daut)
    insertAndGetQuote(db, "close us man!", "jc", daut)

    val dautStreams = db.runQuery(db.getAllQuotesForStream(daut.name))
    assert(dautStreams.size == 5)

    val Some(rando) = db.runQuery(db.getRandomQuoteForStream(daut.name)).headOption
    db.runUpdate(db.deleteQuote(daut.name, rando.qid))

    assert(db.runQuery(db.getAllQuotesForStream(daut.name)).size == 4)

    insertAndGetQuote(db, "This place sucks!", "jc", daut)
    insertAndGetQuote(db, "idiota", "jc", jonslow)
    insertAndGetQuote(db, "retardo", "jc", artoftroll)

    assert(db.runQuery(db.getAllQuotesForStream("jonslow_")).size == 1)
    assert(db.runQuery(db.getAllQuotesForStream("artofthetroll")).size == 1)
  }
}
