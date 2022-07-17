
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

CREATE TABLE user_commands (
  name character varying NOT NULL,
  body text NOT NULL,
  channel text NOT NULL references streams(name),
  added_by text NOT NULL,
  added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (channel, name)
);


select * from counters;
select * from scores;
select * from user_commands;
