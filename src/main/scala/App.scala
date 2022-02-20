import com.joshcough.trollabot.{Chatbot, Configuration}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api.Database

object App {
  def main(args: Array[String]): Unit = {

    // val db = Database.forConfig("db")
//    val dataSource = null.asInstanceOf[slick.jdbc.DatabaseUrlDataSource]
//    val db = Database.forDataSource(dataSource: slick.jdbc.DatabaseUrlDataSource, None)
//    val chatbot = Chatbot(db)

    val url = Configuration.dbUrl
//    println(s"url: $url")
//    val connectionUrl = "jdbc:postgresql://ec2-3-225-79-57.compute-1.amazonaws.com:5432/d32um56ap42olc?user=rjmayszlhyzpov&password=15a1c07b77224867afdb7464ff0d5f5b7b4842162443d533b89d3c44cd597075"
    val chatbot = Chatbot(Database.forURL(url, driver = "org.postgresql.Driver"))

    Runtime.getRuntime.addShutdownHook(new Thread { override def run(): Unit = chatbot.close() })
    chatbot.run()
  }
}
