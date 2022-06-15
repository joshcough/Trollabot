import com.joshcough.trollabot.twitch._

class CommandsSuite extends PostgresContainerSuite {

  val user = ChatUser(ChatUserName("artofthetroll"), isMod = true, subscriber = true, badges = Map())
  val channel = ChannelName("artofthetroll")

  test("commands parse") {
    val commandRunner = CommandRunner(Commands.commands)
    def f(msg: String): Action =
      commandRunner.parseFully(ChatMessage(user, channel, msg)).map(_._2) match {
        case Some(Right(a)) => a
        case Some(Left(err)) => fail(err)
        case None => fail("couldn't parse")
      }
    assertEquals(f("!join daut"), JoinAction("daut"))
    assertEquals(f("!part"), PartAction(channel))
    assertEquals(f("!addQuote hi"), AddQuoteAction(channel, user, "hi"))
    assertEquals(f("!delQuote 0"), DelQuoteAction(channel, 0))
    assertEquals(f("!quote 0"), GetExactQuoteAction(channel, 0))
    assertEquals(f("!quote"), GetRandomQuoteAction(channel))
    assertEquals(f("!printStreams"), PrintStreamsAction)
    assertEquals(f("!addCounter x"), AddCounterAction(channel, user, "x"))
    assertEquals(f("!inc x"), IncCounterAction(channel, "x"))
    assertEquals(f("!help !quote"), HelpAction("!quote"))
    assertEquals(f("!search %hell%"), SearchQuotesAction(channel, "%hell%"))
  }

  test("join command joins") {
    withDb {
      for {
        response <- CommandInterpreter.interpret(JoinAction("artofthetroll")).compile.toList
      } yield assertEquals(response, List(Join("artofthetroll"), RespondWith("Joining artofthetroll!")))
    }
  }

  test("part command parts") {
    withDb {
      for {
        response <- CommandInterpreter.interpret(PartAction(channel)).compile.toList
      } yield assertEquals(response, List(RespondWith("Goodbye cruel world!"), Part))
    }
  }

  test("add quote command adds") {
    withDb {
      for {
        response <- CommandInterpreter.interpret(AddQuoteAction(channel, user, "hello")).compile.toList
      } yield assertEquals(response, List(RespondWith("Quote #0: hello")))
    }
  }

  test("get quote command gets quote") {
    withDb {
      for {
        _ <- CommandInterpreter.addQuote(channel, user, "hello").compile.toList
        response <- CommandInterpreter.interpret(GetExactQuoteAction(channel, 0)).compile.toList
      } yield assertEquals(response, List(RespondWith("Quote #0: hello")))
    }
  }

  test("search command gets quote") {
    withDb {
      for {
        _ <- CommandInterpreter.addQuote(channel, user, "hello").compile.toList
        response1 <- CommandInterpreter.interpret(SearchQuotesAction(channel, "%hell%")).compile.toList
        response2 <- CommandInterpreter.interpret(SearchQuotesAction(channel, "%zzz%")).compile.toList
      } yield assertEquals(response1, List(RespondWith("Quote #0: hello"))) && assertEquals(response2, Nil)
    }
  }

  test("delete quote command deletes quote") {
    withDb {
      for {
        _ <- CommandInterpreter.addQuote(channel, user, "hello").compile.toList
        deleteResponse <- CommandInterpreter.interpret(DelQuoteAction(channel, 0)).compile.toList
        getResponse <- CommandInterpreter.getExactQuote(channel, 0).compile.toList
      } yield
          assertEquals(deleteResponse, List(RespondWith("Ok I deleted it."))) &&
          assertEquals(getResponse, List(RespondWith("I couldn't find quote #0, man.")))
    }
  }

  test("print streams command prints streams") {
    withDb {
      val expectedText = "Stream(Some(1),daut,false), Stream(Some(2),jonslow_,false), Stream(Some(3),artofthetroll,false)"
      for {
        response <- CommandInterpreter.interpret(PrintStreamsAction).compile.toList
      } yield assertEquals(response, List(RespondWith(expectedText)))
    }
  }

  test("add counter command adds counter") {
    withDb {
      for {
        response0 <- CommandInterpreter.interpret(AddCounterAction(channel, user, "my-counter")).compile.toList
        response1 <- CommandInterpreter.interpret(IncCounterAction(channel, "my-counter")).compile.toList
        response2 <- CommandInterpreter.interpret(IncCounterAction(channel, "my-counter")).compile.toList
        responses = response0 ++ response1 ++ response2
      } yield assertEquals(
        responses,
        List(
          RespondWith("Ok I added it. my-counter:0"),
          RespondWith("Ok I incremented it. my-counter:1"),
          RespondWith("Ok I incremented it. my-counter:2")
        )
      )
    }
  }

  test("help command helps")(
    for {
      response <- CommandInterpreter.interpret(HelpAction("!quote")).compile.toList
    } yield assertEquals(response, List(RespondWith("!quote optional(int) (permissions: Anyone)")))
  )
}
