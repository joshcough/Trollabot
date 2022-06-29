package com.joshcough.trollabot

import cats.implicits._
import doobie._
import doobie.implicits._

object TestQueries {

  val dropStreamsTable: Update0 = sql"drop table if exists streams".update
  val dropQuotesTable: Update0 = sql"drop table if exists quotes".update
  val dropCountersTable: Update0 = sql"drop table if exists counters".update
  val dropScoresTable: Update0 = sql"drop table if exists scores".update

  val createStreamsTable: Update0 =
    sql"""
      CREATE TABLE streams (
        id SERIAL PRIMARY KEY,
        name character varying NOT NULL,
        joined boolean NOT NULL,
        CONSTRAINT unique_stream_name UNIQUE (name)
      )""".update

  val createQuotesTable: Update0 =
    sql"""
      CREATE TABLE quotes (
        id SERIAL PRIMARY KEY,
        qid integer NOT NULL,
        text character varying NOT NULL,
        channel int NOT NULL references streams(id),
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        deleted bool NOT NULL DEFAULT false,
        deleted_by text,
        deleted_at TIMESTAMP WITH TIME ZONE,
        CONSTRAINT unique_quote_channel_and_qid UNIQUE (channel, qid),
        CONSTRAINT unique_quote_channel_and_text UNIQUE (channel, text)
      )""".update

  val createCountersTable: Update0 =
    sql"""
      CREATE TABLE counters (
        id SERIAL PRIMARY KEY,
        name character varying NOT NULL,
        current_count int NOT NULL,
        channel integer NOT NULL,
        added_by text NOT NULL,
        added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT unique_counter_channel UNIQUE (channel, name)
      )""".update

  val createScoresTable: Update0 =
    sql"""
      CREATE TABLE scores (
        id SERIAL PRIMARY KEY,
        channel integer NOT NULL,
        player1 character varying NULL,
        player2 character varying NULL,
        player1_score int NOT NULL,
        player2_score int NOT NULL,
        CONSTRAINT unique_score_channel UNIQUE (channel)
      )""".update

  val recreateSchema: ConnectionIO[Int] =
    (
      dropScoresTable,
      dropCountersTable,
      dropQuotesTable,
      dropStreamsTable,
      createStreamsTable,
      createQuotesTable,
      createCountersTable,
      createScoresTable,
    ) match {
      case (a, b, c, d, e, f, g, h) =>
        (a.run, b.run, c.run, d.run, e.run, f.run, g.run, h.run).mapN(_ + _ + _ + _ + _ + _ + _ + _)
    }

  val deleteAllQuotes: Update0 = sql"delete from quotes".update

  val deleteAllStreams: Update0 = sql"delete from streams".update
}
