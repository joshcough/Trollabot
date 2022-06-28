package com.joshcough.trollabot

import cats.effect.IO
import com.joshcough.trollabot.api.Api
import com.joshcough.trollabot.twitch._
import doobie.ConnectionIO

class CommandsSuite extends PostgresContainerSuite {

  val userName: ChatUserName = ChatUserName("artofthetroll")
  val user: ChatUser = ChatUser(userName, isMod = true, subscriber = true, badges = Map())
  val channel: ChannelName = ChannelName("artofthetroll")

  test("commands parse") {
    val commandRunner = CommandRunner(Commands.commands)
    def f(msg: String): Action = {
      commandRunner.parseMessageAndFindCommand(ChatMessage(user, channel, msg)) match {
        case None => fail(s"couldn't find command for message: $msg")
        case Some((cmd, args)) => cmd.apply(channel, user, args) match {
          case Left(err) => fail(s"couldn't parse message args: $msg, $err")
          case Right(action) => action
        }
      }
    }

    assertEquals(f("!join daut"), JoinAction(ChannelName("daut")))
    assertEquals(f("!part"), PartAction(channel))
    assertEquals(f("!addQuote hi"), AddQuoteAction(channel, user, "hi"))
    assertEquals(f("!delQuote 0"), DelQuoteAction(channel, 0))
    assertEquals(f("!quote 0"), GetExactQuoteAction(channel, 0))
    assertEquals(f("!quote"), GetRandomQuoteAction(channel))
    assertEquals(f("!printStreams"), PrintStreamsAction)
    assertEquals(f("!addCounter x"), AddCounterAction(channel, user, CounterName("x")))
    assertEquals(f("!inc x"), IncCounterAction(channel, CounterName("x")))
    assertEquals(f("!help !quote"), HelpAction("!quote"))
    assertEquals(f("!search %hell%"), SearchQuotesAction(channel, "%hell%"))
    assertEquals(f("!buildInfo"), BuildInfoAction)
    assertEquals(f("!score"), ScoreAction(channel, GetScore))
    assertEquals(f("!score 2 3"), ScoreAction(channel, SetScore(2, 3)))
    assertEquals(f("!score daut 4 viper 0"), ScoreAction(channel, SetAll("daut", "viper", 4, 0)))
    assertEquals(f("!player artofthetroll"), SetPlayerAction(channel, "artofthetroll"))
    assertEquals(f("!opponent artofthetroll"), SetOpponentAction(channel, "artofthetroll"))
  }

  def withInterpreter[A](f: CommandInterpreter => ConnectionIO[A]): IO[A] =
    withDb(f(CommandInterpreter(Api.db)))

  test("join command joins") {
    withInterpreter { interp =>
      for {
        response <- interp.interpret(JoinAction(channel)).compile.toList
      } yield assertEquals(response, List(Join(channel), RespondWith("Joining artofthetroll!")))
    }
  }

  test("part command parts") {
    withInterpreter { interp =>
      for {
        response <- interp.interpret(PartAction(channel)).compile.toList
      } yield assertEquals(response, List(RespondWith("Goodbye cruel world!"), Part))
    }
  }

  test("add quote command adds") {
    withInterpreter { interp =>
      for {
        response <- interp.interpret(AddQuoteAction(channel, user, "hello")).compile.toList
      } yield assertEquals(response, List(RespondWith("Quote #0: hello")))
    }
  }

  test("can't add the same quote twice") {
    withInterpreter { interp =>
      for {
        response1 <- interp.interpret(AddQuoteAction(channel, user, "hello")).compile.toList
        response2 <- interp.interpret(AddQuoteAction(channel, user, "hello")).compile.toList
      } yield
        assertEquals(response1, List(RespondWith("Quote #0: hello"))) &&
        assertEquals(response2, List(RespondWith("That quote already exists man! It's #0")))
    }
  }

  test("get quote command gets quote") {
    withInterpreter { interp =>
      for {
        _ <- interp.addQuote(channel, user, "hello").compile.toList
        response <- interp.interpret(GetExactQuoteAction(channel, 0)).compile.toList
      } yield assertEquals(response, List(RespondWith("Quote #0: hello")))
    }
  }

  test("search command gets quote") {
    withInterpreter { interp =>
      for {
        _ <- interp.addQuote(channel, user, "hello").compile.toList
        response1 <- interp.interpret(SearchQuotesAction(channel, "%hell%")).compile.toList
        response2 <- interp.interpret(SearchQuotesAction(channel, "%zzz%")).compile.toList
      } yield assertEquals(response1, List(RespondWith("Quote #0: hello"))) &&
              assertEquals(response2, List(RespondWith("Couldn't find any quotes that match that.")))
    }
  }

  test("delete quote command deletes quote") {
    withInterpreter { interp =>
      for {
        _ <- interp.addQuote(channel, user, "hello").compile.toList
        deleteResponse <- interp.interpret(DelQuoteAction(channel, 0)).compile.toList
        getResponse <- interp.getExactQuote(channel, 0).compile.toList
      } yield
          assertEquals(deleteResponse, List(RespondWith("Ok I deleted it."))) &&
          assertEquals(getResponse, List(RespondWith("I couldn't find quote #0, man.")))
    }
  }

  test("print streams command prints streams") {
    withInterpreter { interp =>
      val expectedText =
        """{"id":1,"name":{"name":"daut"},"joined":false},
          |{"id":2,"name":{"name":"jonslow_"},"joined":false},
          |{"id":3,"name":{"name":"artofthetroll"},"joined":false}""".stripMargin.replace("\n", "")
      for {
        response <- interp.interpret(PrintStreamsAction).compile.toList
      } yield assertEquals(response, List(RespondWith(expectedText)))
    }
  }

  test("add counter command adds counter") {
    withInterpreter { interp =>
      for {
        response0 <- interp.interpret(AddCounterAction(channel, user, CounterName("my-counter"))).compile.toList
        response1 <- interp.interpret(IncCounterAction(channel, CounterName("my-counter"))).compile.toList
        response2 <- interp.interpret(IncCounterAction(channel, CounterName("my-counter"))).compile.toList
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
    withInterpreter { interp =>
      for {
        response <- interp.interpret(HelpAction("!quote")).compile.toList
      } yield assertEquals(response, List(RespondWith("!quote optional(int)")))
    }
  )

  test("can set score/player/opponent")(
    withInterpreter { interp =>
      for {
        r0 <- interp.interpret(ScoreAction(channel, GetScore)).compile.toList
        r1 <- interp.interpret(SetPlayerAction(channel, "daut")).compile.toList
        r2 <- interp.interpret(SetOpponentAction(channel, "artofthetroll")).compile.toList
        r3 <- interp.interpret(ScoreAction(channel, SetScore(0, 4))).compile.toList
        r4 <- interp.interpret(ScoreAction(channel, SetAll("viper", "hera", 4, 0))).compile.toList
      } yield
        assertEquals(r0, List(RespondWith("player 0 - 0 opponent"))) &&
          assertEquals(r1, List(RespondWith("daut 0 - 0 opponent"))) &&
          assertEquals(r2, List(RespondWith("daut 0 - 0 artofthetroll"))) &&
          assertEquals(r3, List(RespondWith("daut 0 - 4 artofthetroll"))) &&
          assertEquals(r4, List(RespondWith("viper 4 - 0 hera"))) // &&
    }
  )
}
