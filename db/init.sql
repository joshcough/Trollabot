ALTER DATABASE trollabot OWNER TO postgres;

\connect trollabot

CREATE TABLE streams (
  name character varying NOT NULL,
  joined boolean NOT NULL,
  added_by text NOT NULL,
  added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (name)
);

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
);

CREATE TABLE counters (
  name character varying NOT NULL,
  current_count int NOT NULL,
  channel text NOT NULL references streams(name),
  added_by text NOT NULL,
  added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (channel, name)
);

CREATE TABLE scores (
  channel text NOT NULL references streams(name),
  player1 character varying NULL,
  player2 character varying NULL,
  player1_score int NOT NULL,
  player2_score int NOT NULL,
  PRIMARY KEY (channel)
);

COPY public.streams (name, joined, added_by) FROM stdin;
daut	f trollabot
jonslow_	f trollabot
artofthetroll	t trollabot
\.

SELECT pg_catalog.setval('public.streams_id_seq', 3, true);

COPY public.quotes (text, qid, channel, added_by) FROM stdin;
such a loomie fucker	585	1	carloscnsz
Hera: "I will study math and education." Daut: "Waaat, but you suck at both! I guess that's why you study..."	588	1	byelo
go back to Egypt man!	589	1	carloscnsz
Ohhhh! that's what you get you walling fuck	598	1	carloscnsz
"They are standing, but they are coming."	26	1	trollabot
"what happened man we once had beautiful army and now it's gone"	27	1	trollabot
"if I transition now, Jordi will say I told you so, can't have that man"	28	1	trollabot
I miss my secret boys	29	1	trollabot
daut who would win in a bo3 bean or smarthy? ... how the fuck should i know man?	30	1	trollabot
every girl wants to go where daut go	31	1	trollabot
"I'm not Liereyy, I'm not losing my age." Daut on his 33rd Birthday	32	1	trollabot
\.

SELECT pg_catalog.setval('public.quotes_id_seq', 2684, true);
