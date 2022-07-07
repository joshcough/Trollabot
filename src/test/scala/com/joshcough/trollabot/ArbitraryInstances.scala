package com.joshcough.trollabot

import com.joshcough.trollabot.twitch.commands.{Action, BuildInfoAction, HelpAction}
import com.joshcough.trollabot.twitch.commands.Counters.CounterAction
import com.joshcough.trollabot.twitch.commands.Quotes.QuoteAction
import com.joshcough.trollabot.twitch.commands.Scores.ScoreAction
import com.joshcough.trollabot.twitch.commands.Streams.StreamsAction
import com.joshcough.trollabot.twitch.commands.UserCommands.UserCommandAction
import com.mrdziuban.ScalacheckMagnolia._

import java.sql.Timestamp
import org.scalacheck.{Arbitrary, Gen}

import scala.util.Random

object ArbitraryInstances {

  implicit def arbDate: Arbitrary[Timestamp] =
    Arbitrary(new Timestamp((new Random().nextInt((10000000 - 0) + 1) + 0).toLong))

  def arbAction[A <: Action: Arbitrary]: Gen[Action] =
    implicitly[Arbitrary[A]].arbitrary.map(a => a: Action)

  implicit def arbAction: Arbitrary[Action] =
    Arbitrary(
      Gen.oneOf(
        arbAction[StreamsAction],
        arbAction[QuoteAction],
        arbAction[CounterAction],
        arbAction[ScoreAction],
        arbAction[UserCommandAction],
        arbAction[HelpAction],
        arbAction[BuildInfoAction]
      )
    )
}
