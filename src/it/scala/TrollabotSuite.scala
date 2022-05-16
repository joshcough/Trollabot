import doobie.Transactor
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.joshcough.trollabot.TrollabotDb
import com.joshcough.trollabot.Stream
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.testcontainers.utility.DockerImageName

import java.sql.DriverManager

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

  def makeDb(postgres: PostgreSQLContainer): TrollabotDb = {
    TrollabotDb(Transactor.fromConnection(
      DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
    ))
  }

  def withDb[A](f: TrollabotDb => A): A = withContainers { postgres => f(makeDb(postgres)) }
  def dbTest[A](name: String)(f: TrollabotDb => A): Unit = test(name){
    withContainers { postgres => f(makeDb(postgres)) }
  }

  def insertAndGetQuote(db: TrollabotDb, text: String, user: String, stream: Stream): Unit = {
    val Some(newQ) = db.insertQuoteIO(text, user, stream.name)
    assert(newQ.text == text)
    val Some(newQ_) = db.getQuoteByQidIO(stream.name, newQ.qid)
    assert(newQ_.text == text)
    assert(newQ == newQ_)
  }

  dbTest("Startup") { db =>
    db.createSchemaIO()
    streams.foreach(s => db.insertStreamIO(s.name))
  }

  dbTest("Main") { db =>
    insertAndGetQuote(db, "I deserve to be trolled", "jc", daut)
    insertAndGetQuote(db, "come to my healing spot man!", "jc", daut)
    insertAndGetQuote(db, "hope u lost villager", "jc", daut)
    insertAndGetQuote(db, "You are proper Jordan Tati", "jc", daut)
    insertAndGetQuote(db, "close us man!", "jc", daut)

    assert(db.getAllQuotesForStreamIO(daut.name).size == 5)

    val Some(rando) = db.getRandomQuoteIO(daut.name)
    db.deleteQuoteIO(daut.name, rando.qid)

    assert(db.getAllQuotesForStreamIO(daut.name).size == 4)

    insertAndGetQuote(db, "This place sucks!", "jc", daut)

    insertAndGetQuote(db, "idiota", "jc", jonslow)
    insertAndGetQuote(db, "retardo", "jc",artoftroll)

    assert(db.getAllQuotesForStreamIO("jonslow_").size == 1)
    assert(db.getAllQuotesForStreamIO("artofthetroll").size == 1)
  }
}

