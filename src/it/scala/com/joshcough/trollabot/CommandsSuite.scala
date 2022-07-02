package com.joshcough.trollabot

import com.joshcough.trollabot.api.{Api, CounterName, UserCommandName}
import com.joshcough.trollabot.twitch.commands._
import com.joshcough.trollabot.twitch.commands.Quotes._
import com.joshcough.trollabot.twitch.commands.Scores._
import com.joshcough.trollabot.twitch.commands.Streams._
import com.joshcough.trollabot.twitch.commands.Counters._
import com.joshcough.trollabot.twitch.commands.UserCommands.{AddUserCommandAction, DeleteUserCommandAction, EditUserCommandAction, GetUserCommandAction}
import doobie.ConnectionIO

class CommandsSuite extends PostgresContainerSuite {

  val userName: ChatUserName = ChatUserName("artofthetroll")
  val user: ChatUser = ChatUser(userName, isMod = true, subscriber = true, badges = Map())
  val channel: ChannelName = ChannelName("artofthetroll")
  val api = Api.db
  val commandRunner = CommandRunner(Commands.commands)

  test("commands parse") {
    assertEquals(parse("!join daut"), JoinAction(ChannelName("daut")))
    assertEquals(parse("!part"), PartAction(channel))
    assertEquals(parse("!addQuote hi"), AddQuoteAction(channel, user, "hi"))
    assertEquals(parse("!delQuote 0"), DelQuoteAction(channel, 0))
    assertEquals(parse("!quote 0"), GetExactQuoteAction(channel, 0))
    assertEquals(parse("!quote"), GetRandomQuoteAction(channel))
    assertEquals(parse("!printStreams"), PrintStreamsAction)
    assertEquals(parse("!addCounter x"), AddCounterAction(channel, user, CounterName("x")))
    assertEquals(parse("!inc x"), IncCounterAction(channel, CounterName("x")))
    assertEquals(parse("!help !quote"), HelpAction("!quote"))
    assertEquals(parse("!search %hell%"), SearchQuotesAction(channel, "%hell%"))
    assertEquals(parse("!buildInfo"), BuildInfoAction)
    assertEquals(parse("!score"), ScoreAction(channel, GetScore))
    assertEquals(parse("!score 2 3"), ScoreAction(channel, SetScore(2, 3)))
    assertEquals(parse("!score daut 4 viper 0"), ScoreAction(channel, SetAll("daut", "viper", 4, 0)))
    assertEquals(parse("!player artofthetroll"), SetPlayerAction(channel, "artofthetroll"))
    assertEquals(parse("!opponent artofthetroll"), SetOpponentAction(channel, "artofthetroll"))
    assertEquals(parse("!add hurt I'm hurt!"), AddUserCommandAction(channel, user, UserCommandName("hurt"), "I'm hurt!"))
    assertEquals(parse("!edit hurt I'm actually not!"), EditUserCommandAction(channel, UserCommandName("hurt"), "I'm actually not!"))
    assertEquals(parse("!delete hurt"), DeleteUserCommandAction(channel, UserCommandName("hurt")))
  }

  test("join command joins") {
    withDb {
      for {
        response <- run(JoinAction(channel))
      } yield assertEquals(response, List(Join(channel), RespondWith("Joining artofthetroll!")))
    }
  }

  test("part command parts") {
    withDb {
      for {
        response <- run(PartAction(channel))
      } yield assertEquals(response, List(RespondWith("Goodbye cruel world!"), Part))
    }
  }

  test("add quote command adds") {
    withDb {
      for {
        response <- run(AddQuoteAction(channel, user, "hello"))
      } yield assertEquals(response, List(RespondWith("Quote #0: hello")))
    }
  }

  test("can't add the same quote twice") {
    withDb {
      for {
        response1 <- run(AddQuoteAction(channel, user, "hello"))
        response2 <- run(AddQuoteAction(channel, user, "hello"))
      } yield
        assertEquals(response1, List(RespondWith("Quote #0: hello"))) &&
        assertEquals(response2, List(RespondWith("That quote already exists man! It's #0")))
    }
  }

  test("get quote command gets quote") {
    withDb {
      for {
        _ <- Quotes.addQuote(api)(channel, user, "hello").compile.toList
        response1 <- run(GetExactQuoteAction(channel, 0))
        response2 <- run(GetRandomQuoteAction(channel))
      } yield assertEquals(response1, List(RespondWith("Quote #0: hello"))) &&
              assertEquals(response2, List(RespondWith("Quote #0: hello")))
    }
  }

  test("search command gets quote") {
    withDb {
      for {
        _ <- Quotes.addQuote(api)(channel, user, "hello").compile.toList
        response1 <- run(SearchQuotesAction(channel, "%hell%"))
        response2 <- run(SearchQuotesAction(channel, "%zzz%"))
      } yield assertEquals(response1, List(RespondWith("Quote #0: hello"))) &&
              assertEquals(response2, List(RespondWith("Couldn't find any quotes that match that.")))
    }
  }

  test("delete quote command deletes quote") {
    withDb {
      for {
        _ <- Quotes.addQuote(api)(channel, user, "hello").compile.toList
        deleteResponse <- run(DelQuoteAction(channel, 0))
        getResponse <- Quotes.getExactQuote(api)(channel, 0).compile.toList
      } yield
          assertEquals(deleteResponse, List(RespondWith("Ok I deleted it."))) &&
          assertEquals(getResponse, List(RespondWith("I couldn't find quote #0, man.")))
    }
  }

  test("print streams command prints streams") {
    withDb {
      val expectedText =
        """{"name":{"name":"daut"},"joined":false},
          |{"name":{"name":"jonslow_"},"joined":false},
          |{"name":{"name":"artofthetroll"},"joined":false}""".stripMargin.replace("\n", "")
      for {
        response <- run(PrintStreamsAction)
      } yield assertEquals(response, List(RespondWith(expectedText)))
    }
  }

  test("add counter command adds counter") {
    withDb {
      for {
        response0 <- run(AddCounterAction(channel, user, CounterName("my-counter")))
        response1 <- run(IncCounterAction(channel, CounterName("my-counter")))
        response2 <- run(IncCounterAction(channel, CounterName("my-counter")))
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
    withDb {
      for {
        response <- run(HelpAction("!quote"))
      } yield assertEquals(response, List(RespondWith("!quote optional(int)")))
    }
  )

  test("can set score/player/opponent")(
    withDb {
      for {
        r0 <- run(ScoreAction(channel, GetScore))
        r1 <- run(SetPlayerAction(channel, "daut"))
        r2 <- run(SetOpponentAction(channel, "artofthetroll"))
        r3 <- run(ScoreAction(channel, SetScore(0, 4)))
        r4 <- run(ScoreAction(channel, SetAll("viper", "hera", 4, 0)))
      } yield
        assertEquals(r0, List(RespondWith("player 0 - 0 opponent"))) &&
          assertEquals(r1, List(RespondWith("daut 0 - 0 opponent"))) &&
          assertEquals(r2, List(RespondWith("daut 0 - 0 artofthetroll"))) &&
          assertEquals(r3, List(RespondWith("daut 0 - 4 artofthetroll"))) &&
          assertEquals(r4, List(RespondWith("viper 4 - 0 hera"))) // &&
    }
  )

  test("user commands")(
    withDb {
      val cn = UserCommandName("housed")
      val getIt = run(GetUserCommandAction(channel, cn))
      for {
        // create
        r0 <- run(AddUserCommandAction(channel, user, cn, "Been housed 5 times!"))
        r1 <- getIt
        // update
        r2 <- run(EditUserCommandAction(channel, cn, "Been housed 6 times!"))
        r3 <- getIt
        // delete
        r4 <- run(DeleteUserCommandAction(channel, cn))
        r5 <- getIt
      } yield
        assertEquals(r0, List(RespondWith("Ok I added it."))) &&
          assertEquals(r1, List(RespondWith("Been housed 5 times!"))) &&
          assertEquals(r2, List(RespondWith("Ok I edited it."))) &&
          assertEquals(r3, List(RespondWith("Been housed 6 times!"))) &&
          assertEquals(r4, List(RespondWith("Ok I deleted it."))) &&
          assertEquals(r5, List(RespondWith("Couldn't find that command, man.")))
    }
  )
  test("user commands 2")(
    withDb {
      for {
        // create
        r0 <- runCmd("!add housed Been housed 5 times!")
        r1 <- runCmd("!housed")
        // update
        r2 <- runCmd("!edit housed Been housed 6 times!")
        r3 <- runCmd("!housed")
        // delete
        r4 <- runCmd("!delete housed")
        r5 <- runCmd("!housed")
      } yield
        assertEquals(r0, List(RespondWith("Ok I added it."))) &&
          assertEquals(r1, List(RespondWith("Been housed 5 times!"))) &&
          assertEquals(r2, List(RespondWith("Ok I edited it."))) &&
          assertEquals(r3, List(RespondWith("Been housed 6 times!"))) &&
          assertEquals(r4, List(RespondWith("Ok I deleted it."))) &&
          assertEquals(r5, List(RespondWith("Couldn't find that command, man.")))
    }
  )

  test("user commands with counters")(
    withDb {
      for {
        _ <- runCmd("!addCounter x")
        _ <- runCmd("!addCounter y")
        // create
        r0 <- runCmd("!add housed Been housed ${x} times!")
        r1 <- runCmd("!housed")
        r2 <- runCmd("!housed")
        _ <- runCmd("!add mf I said mf ${x}${y} times!")
        r3 <- runCmd("!mf")
        r4 <- runCmd("!mf")
      } yield
        assertEquals(r0, List(RespondWith("Ok I added it."))) &&
          assertEquals(r1, List(RespondWith("Been housed 1 times!"))) &&
          assertEquals(r2, List(RespondWith("Been housed 2 times!"))) &&
          assertEquals(r3, List(RespondWith("I said mf 31 times!"))) &&
          assertEquals(r4, List(RespondWith("I said mf 42 times!")))
    }
  )

  def parse(msg: String): Action = {
    commandRunner.parseMessageAndFindCommand(ChatMessage(user, channel, msg)) match {
      case None => fail(s"couldn't find command for message: $msg")
      case Some((cmd, args)) => cmd.parseAndCheckPerms(channel, user, args) match {
        case Left(err) => fail(s"couldn't parse message args: $msg, $err")
        case Right(Some(action)) => action
        case Right(None) => fail(s"insufficient permissions for message: $msg")
      }
    }
  }
  def run(a: Action): ConnectionIO[List[Response]] = a.run(api).compile.toList
  def runCmd(body: String): ConnectionIO[List[Response]] =
    commandRunner.processMessage(ChatMessage(user, channel, body), api).compile.toList

}
