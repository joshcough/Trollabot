package com.joshcough.trollabot

import cats.implicits._
import doobie._
import doobie.implicits._

object TestQueries {

  val dropStreamsTable: Update0 = sql"drop table if exists streams".update
  val dropQuotesTable: Update0 = sql"drop table if exists quotes".update
  val dropCountersTable: Update0 = sql"drop table if exists counters".update
  val dropScoresTable: Update0 = sql"drop table if exists scores".update
  val dropUserCommandsTable: Update0 = sql"drop table if exists user_commands".update

  val createStreamsTable: Update0 =
    sql"""
      CREATE TABLE streams (
        name character varying NOT NULL,
        joined boolean NOT NULL,
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (name)
      )""".update

  val createQuotesTable: Update0 =
    sql"""
      CREATE TABLE quotes (
        qid integer NOT NULL,
        text character varying NOT NULL,
        channel text NOT NULL references streams(name),
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        deleted bool NOT NULL DEFAULT false,
        deleted_by text,
        deleted_at TIMESTAMP WITH TIME ZONE,
        PRIMARY KEY (channel, qid),
        CONSTRAINT unique_quote_channel_and_text UNIQUE (channel, text)
      )""".update

  val createCountersTable: Update0 =
    sql"""
      CREATE TABLE counters (
        name character varying NOT NULL,
        current_count int NOT NULL,
        channel text NOT NULL references streams(name),
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (channel, name)
      )""".update

  val createScoresTable: Update0 =
    sql"""
      CREATE TABLE scores (
        channel text NOT NULL references streams(name),
        player1 character varying NULL,
        player2 character varying NULL,
        player1_score int NOT NULL,
        player2_score int NOT NULL,
        PRIMARY KEY (channel)
      )""".update

  val createUserCommandsTable: Update0 =
    sql"""
      CREATE TABLE user_commands (
        name character varying NOT NULL,
        body text NOT NULL,
        channel text NOT NULL references streams(name),
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (channel, name)
      )""".update

  val recreateSchema: ConnectionIO[Int] =
    (
      dropUserCommandsTable,
      dropScoresTable,
      dropCountersTable,
      dropQuotesTable,
      dropStreamsTable,
      createStreamsTable,
      createQuotesTable,
      createCountersTable,
      createScoresTable,
      createUserCommandsTable,
    ) match {
      case (a, b, c, d, e, f, g, h, i, j) =>
        (a.run, b.run, c.run, d.run, e.run, f.run, g.run, h.run, i.run, j.run)
          .mapN(_ + _ + _ + _ + _ + _ + _ + _ + _ + _)
    }

  val deleteAllQuotes: Update0 = sql"delete from quotes".update

  val deleteAllStreams: Update0 = sql"delete from streams".update
}
