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
}
