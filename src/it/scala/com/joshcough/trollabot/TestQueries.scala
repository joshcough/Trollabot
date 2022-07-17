package com.joshcough.trollabot

import doobie._
import doobie.implicits._

object TestQueries {
  val deleteAllUserCommands: Update0 = sql"delete from user_commands".update
  val deleteAllCounters: Update0 = sql"delete from counters".update
  val deleteAllScores: Update0 = sql"delete from scores".update
  val deleteAllQuotes: Update0 = sql"delete from quotes".update
  val deleteAllStreams: Update0 = sql"delete from streams".update
}
