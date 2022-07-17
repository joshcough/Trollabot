ALTER TABLE quotes DROP CONSTRAINT quotes_pkey;
ALTER TABLE quotes DROP CONSTRAINT quotes_channel_fkey;
ALTER TABLE streams DROP CONSTRAINT streams_pkey;

ALTER TABLE quotes ADD COLUMN channelName text NOT NULL references streams(name) DEFAULT 'daut';
UPDATE quotes q SET channelName = s.name FROM streams s WHERE s.id = q.channel;
ALTER TABLE quotes DROP COLUMN channel;
ALTER TABLE quotes RENAME COLUMN channelName TO channel;
ALTER TABLE quotes ALTER COLUMN channel DROP DEFAULT;
ALTER TABLE quotes ALTER COLUMN channel DROP DEFAULT;

ALTER TABLE quotes DROP COLUMN id;
ALTER TABLE streams DROP COLUMN id;

ALTER TABLE streams ADD COLUMN joined boolean NOT NULL DEFAULT false;
ALTER TABLE streams ADD COLUMN added_by text NOT NULL DEFAULT 'trollabot';
ALTER TABLE streams
  ADD COLUMN added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE quotes RENAME COLUMN user_id to added_by;
ALTER TABLE quotes ADD COLUMN added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE quotes ADD COLUMN deleted bool NOT NULL DEFAULT false;
ALTER TABLE quotes ADD COLUMN deleted_by text;
ALTER TABLE quotes ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE ONLY streams ADD CONSTRAINT streams_pkey PRIMARY KEY (name);
ALTER TABLE ONLY quotes ADD CONSTRAINT quotes_pkey PRIMARY KEY (channel, qid);

select * from streams;
select * from quotes limit 5;


