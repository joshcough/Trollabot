ALTER DATABASE trollabot OWNER TO postgres;

\connect trollabot


CREATE TABLE public.streams (
    id SERIAL PRIMARY KEY,
    name character varying NOT NULL,
    joined boolean NOT NULL,
    CONSTRAINT unique_stream_name UNIQUE (name)
);

CREATE TABLE public.quotes (
    id SERIAL PRIMARY KEY,
    qid integer NOT NULL,
    text character varying NOT NULL,
    channel int NOT NULL references streams(id),
    added_by text NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    deleted_by text,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT unique_quote_channel_and_qid UNIQUE (channel, qid)
);

CREATE TABLE public.counters (
    id SERIAL PRIMARY KEY,
    name character varying NOT NULL,
    current_count int NOT NULL,
    channel integer NOT NULL references streams(id),
    CONSTRAINT unique_counter_channel UNIQUE (channel, name)
);

COPY public.streams (id, name, joined) FROM stdin;
1	daut	f
2	jonslow_	f
3	artofthetroll	t
\.

SELECT pg_catalog.setval('public.streams_id_seq', 3, true);

COPY public.quotes (id, text, qid, channel, added_by) FROM stdin;
618	such a loomie fucker	585	1	carloscnsz
621	Hera: "I will study math and education." Daut: "Waaat, but you suck at both! I guess that's why you study..."	588	1	byelo
622	go back to Egypt man!	589	1	carloscnsz
631	Ohhhh! that's what you get you walling fuck	598	1	carloscnsz
26	"They are standing, but they are coming."	26	1	trollabot
27	"what happened man we once had beautiful army and now it's gone"	27	1	trollabot
28	"if I transition now, Jordi will say I told you so, can't have that man"	28	1	trollabot
29	I miss my secret boys	29	1	trollabot
30	daut who would win in a bo3 bean or smarthy? ... how the fuck should i know man?	30	1	trollabot
31	every girl wants to go where daut go	31	1	trollabot
32	"I'm not Liereyy, I'm not losing my age." Daut on his 33rd Birthday	32	1	trollabot
33	you're going down little man!	33	1	trollabot
34	I kinda promised girlfriend I would spend the night with her, screw that, bushnite man! dautBush	34	1	trollabot
35	i need to spend time with my girlfriend, she still thinks im playing tournament man! dautWat	35	1	trollabot
36	i'm proper Lara Croft now dautWat	36	1	trollabot
37	"Long time no see my son" DauT coz he is sleeping all day	37	1	trollabot
38	Usually when i travel i tend to come back with much less bags	38	1	trollabot
39	I deserve to be trolled	39	1	trollabot
40	come to my healing spot man!	40	1	trollabot
41	hope u lost villager	41	1	trollabot
42	You are proper Jordan Tati	42	1	trollabot
43	close us man!	43	1	trollabot
44	"Don't let my castle be DauT!"	44	1	trollabot
45	They place me above you. So they place me correct	45	1	trollabot
118	ok guys! keep making fun of viper while i go eat	118	1	trollabot
46	It's hard to play Viper cuz he always make stupid moves, so you think he's stupid. But sometimes he have a plan	46	1	trollabot
47	"if only AoC was turn based like chess"	47	1	trollabot
48	do i think viper is gay? well that's now a proper question	48	1	trollabot
49	I know shisha is bad for health, but i dont plan to live forever.	49	1	trollabot
50	"if your girlfriend is cheating on you go for man at arms"	50	1	trollabot
51	talk with viper for 24hs that's a dream	51	1	trollabot
52	Tomorrow! tomorrow i will practice 1v1s all day	52	1	trollabot
53	I'm a fast fucker	53	1	trollabot
54	tell me viper, how does it feels to cast my games every weekend?	54	1	trollabot
55	Ppl with more than 1 kid desirve so much respect	55	1	trollabot
56	56 doesnt deserve a quote	56	1	trollabot
57	Art of troll is really good at what he's doing	57	1	trollabot
58	shit goes wrong when i'm not there	58	1	trollabot
59	Who can hate me?	59	1	trollabot
60	my luring skills are good, i should be fine	60	1	trollabot
61	Viper is full gossip girl of aoc	61	1	trollabot
62	U live in snow viper. only thing u can do is sit and play	62	1	trollabot
63	when u have lead you fantasy play	63	1	trollabot
64	xD	64	1	trollabot
65	Samurai counter Cav Archers that makes no sense man!	65	1	trollabot
66	If u get a boy in the first try. why keep trying?	66	1	trollabot
67	Time goes faster when u sleep all day	67	1	trollabot
68	BAM! ... BAAAM!! ... come on i said BAM! there u go	68	1	trollabot
69	"It's a rainbow day here! KappaPride It's like I'm playing against 10 players here"	69	1	trollabot
70	"I'm not 2k anymore, man!" - DauT 2018	70	1	trollabot
71	ok ok i'm dedicated to kill this deer	71	1	trollabot
72	i will still find a way to lose this villager	72	1	trollabot
73	never be nice man never be nice.	73	1	trollabot
74	every gold in the map is my gold	74	1	trollabot
75	sacrifice nicov is like main strategy for every player	75	1	trollabot
76	The wolf is outmicroing me	76	1	trollabot
77	"You are such a nicov" DauT to Tati cuz he fast gg after one attack	77	1	trollabot
78	"I show up for events... sooner or later" - DauT NAC1 2018	78	1	trollabot
79	NIli said "u guys can drink all this beer that i bought?" and i was like FUCK YEAH!!!	79	1	trollabot
80	if u wanna learn how to micro watch my stream. if u wanna learn macro watch Edie	80	1	trollabot
81	imagine they using our strategy and getting destroyed in ecl	81	1	trollabot
82	You are jealous of people with hair viper	82	1	trollabot
83	Ok ok. just sit there and don't die	83	1	trollabot
84	ok Memb i will do what you are saying because your are older than me and i respect you	84	1	trollabot
85	Tati u are the king of the migration and the king of the sling	85	1	trollabot
86	my girl give me food man yours not	86	1	trollabot
635	I am bored, I go die	602	1	goldeneye_
641	you can put the sheep inside of the cow	608	1	carloscnsz
794	he is converting my kills	759	1	carloscnsz
852	every team with me as a pocket is a strong team	786	1	carloscnsz
619	i was bussy complaining and crying instead of thinking	586	1	carloscnsz
623	Viper you are the worst team waller	590	1	carloscnsz
632	Man you are worse than Jordan	599	1	carloscnsz
87	Attack ground should be removed from this game	87	1	trollabot
88	Tati is nice guy.. Only I can make him mad dautKing	88	1	trollabot
89	i should go take my family. but my gf is not answering so... Let's play one more!	89	1	trollabot
90	I can't be new Viper! i was Viper before Viper	90	1	trollabot
91	That moment when you realize Viper is the longest sub you got. You ask to yourself what the fuck are you doing with your life	91	1	trollabot
92	more mods i have, less stuff i have to do	92	1	trollabot
93	I like to mod people dautWat	93	1	trollabot
94	"I don't like to micro to be honest"	94	1	trollabot
95	I'll go inside next time man	95	1	trollabot
96	This hill I like... I take!	96	1	trollabot
97	get converted fucker!	97	1	trollabot
98	i always try hard	98	1	trollabot
99	I only killed spearmen man!	99	1	trollabot
100	Can you come to my tc?	100	1	trollabot
101	Can you tell me what you're doing, cause I don't have scout	101	1	trollabot
102	it's like I like to be stupid man!	102	1	trollabot
103	oh fuck sake, you and your fricking timing man.	103	1	trollabot
104	you have a problem, just make a gate.	104	1	trollabot
105	"What tati? u can't go sleep! we are doing bushnite after this! grassDaut " DauT to Tatoh during bo37	105	1	trollabot
106	time to reboom	106	1	trollabot
107	What are you eating? Thats cheating	107	1	trollabot
108	screw u fire. now i want bact to qualify	108	1	trollabot
109	fuck this! i'm throwing now!	109	1	trollabot
110	i'm super professional compared to fire	110	1	trollabot
111	I love my sleep schedule when its fucked	111	1	trollabot
112	"hope u don't qualify to kotd" Daut to mbl after losing 2 vills in dark age to mbl scout	112	1	trollabot
113	I will make nicov drunk and he will do secret things	113	1	trollabot
114	who boosted me to 2k? 20 years of experience man	114	1	trollabot
115	When your main army is 5 spearmen, you know you are fucked	115	1	trollabot
116	Report? report who? i report u now dautWat	116	1	trollabot
117	"how is his hill bigger than mine!"	117	1	trollabot
119	Viper: "Daut? why u stream so early?" / Daut: "Because i'm fresh. I'm always fresh"	119	1	trollabot
120	“I love F1re, he is my favorite player”	120	1	trollabot
121	if fire is coming i will come as well. I need to troll him	121	1	trollabot
122	And me... I'm looking promising as well	122	1	trollabot
123	"Oh I have vils! What is the purpose of villagers?" - DauT 2018	123	1	trollabot
124	I will make Tati quit aoc again	124	1	trollabot
125	All i want for christmas is extra gold dautWat	125	1	trollabot
126	Oh i'm host. was wondering why the game wasn't starting	126	1	trollabot
127	That kinda fucks my aggression	127	1	trollabot
128	Hello miguel, happy birthday, u have your best birthday present! Fire losing	128	1	trollabot
129	"If i was Fire i will kill my self" daut casting Bact Vs Fire	129	1	trollabot
130	We need to gather money for an elevator for nili's building	130	1	trollabot
131	mbl playstyle can create serial killers	131	1	trollabot
132	Viper must be laughing at us like "Ha ha ha kiss me honey"	132	1	trollabot
133	oh man i royally fucked up	133	1	trollabot
134	Don't call me MbL man.	134	1	trollabot
135	T90 only knows how to play Forest Nothing and he even sucks at that!	135	1	trollabot
136	fuck that's cheating, not even I can micro that man!	136	1	trollabot
137	why is everybody buying food? I wanna buy food. Food is my friend man!	137	1	trollabot
138	it's such a fast game actually, my APM can't handle this! dautWat	138	1	trollabot
139	NOOOO don't resign yet! i want to kill u!	139	1	trollabot
140	I have camel archers now, you can't resign. That's the rules man!	140	1	trollabot
141	I miss the old me... I was a cool guy	141	1	trollabot
142	That's lazy play. I do it often	142	1	trollabot
143	i'm hard to catch	143	1	trollabot
144	Redemption is the counter to Redemption! New meta confirmed!	144	1	trollabot
145	the best way to sellout is beg for the sub dautSellout	145	1	trollabot
146	dautWat I am topscore? I did not expect to be top score! dautKiss	146	1	trollabot
147	Vaat? You're using that gold without asking? He did not even ask me! dautWat	147	1	trollabot
148	Vaaat? Comeback was possible. Never give up man, don't do a Nicov!	148	1	trollabot
149	look at my tg rating man... i'm such a jordan	149	1	trollabot
150	"look at mbl, picking yellow. We are bald we are from norway we are yellow!"	150	1	trollabot
636	look at tatoh man and his beautiful indian TCs	603	1	carloscnsz
767	I really have a thing for castles. dont know why. it's like hyun with camels!	732	1	hyunaop
795	now i see your base but i don't see your base	760	1	carloscnsz
620	Villager production? I don't need that	587	1	carloscnsz
624	Of course we are fine man! We have goth and khmer man, those civs are op	591	1	carloscnsz
633	Poor ACCM he was destroying all game and now he is dead	600	1	carloscnsz
151	oh it's a hole-a? fuck my life! losing all eco!	151	1	trollabot
152	why do I make castle? it's hard to resist not to make castle!	152	1	trollabot
153	Stop killing my villagers let me build economy!	153	1	trollabot
154	Vaat? Everybody can get to 2.3k, what's wrong these days?	154	1	trollabot
155	apparently in germany viper is funny	155	1	trollabot
156	he is just dying man! he doesn't care	156	1	trollabot
157	Nobody can plan kid, if it happens it happens man!	157	1	trollabot
158	"When someone holds your hand, you hold it back"	158	1	trollabot
159	wall me in man. make me a tower.	159	1	trollabot
160	I will go out and lost all my army	160	1	trollabot
161	"this fast imp will not work" DauT trying to go drush into condos	161	1	trollabot
162	Who is trolling now!!!	162	1	trollabot
163	i thought it will work cuz fire man! he's not the smartest guy	163	1	trollabot
164	"vaat? only 2 points... well u don't worth more than this" DauT to MBL after beating him in chess	164	1	trollabot
165	"OH! STOP PLAYING LIKE MBL!!!!" Daut to Mbl	165	1	trollabot
166	"oh viper stop making mining camps in the woodlines"	166	1	trollabot
167	"maybe I should have pocket.. maybe you should go fuck yourself" DauT to MbL	167	1	trollabot
168	"look at this micro viper! oh? what? u don't have carto? then why i'm taking this fight?" DauT going into micro war with xbos vs mangos	168	1	trollabot
169	"how is red score? oh is lower than mine" and daut procede with the fantasy play	169	1	trollabot
170	should i tell my teammate that all his trade carts stoped... mmmm no, he's top score	170	1	trollabot
171	i'll take grey color to counter Bact invisible color	171	1	trollabot
172	when u think u fuck up	172	1	trollabot
173	this si looking so much BAM now dautBam	173	1	trollabot
174	You are learning the ways of prostagma viper	174	1	trollabot
175	Bolt your ass man!	175	1	trollabot
176	Go little useless fucks!	176	1	trollabot
177	i will outlast everybody	177	1	trollabot
178	I don't want to be in discord with you man	178	1	trollabot
179	I guess i will mangonel your TC	179	1	trollabot
180	one day I will wall in my towers	180	1	trollabot
181	"don't fuck around with arambai man I will fucking kill you"	181	1	trollabot
182	Dance is cheating	182	1	trollabot
183	"Burmese siege onagers? I'm not st4rk I can't make those man"	183	1	trollabot
184	"I need a 10 minute break man, I'm an old fuck"	184	1	trollabot
185	i could sell stone and then kill myself	185	1	trollabot
186	"my son prefers keyboard so he will be a macro player" DauT logic cuz his son likes to smash keyboard instead of mouse LUL	186	1	trollabot
257	This will be named Jordan villager	257	1	trollabot
187	I wish to get sponsor of sisha so i get one sisha girl preparing sisha for me all day	187	1	trollabot
188	i want to hit you	188	1	trollabot
189	i'm humble facka man	189	1	trollabot
190	everybody who knows me in person is a lucky person	190	1	trollabot
191	"This is the type of map i like. just relax and smoke sisha" daut while playing extreme michi	191	1	trollabot
192	Forest nothing is actually so tense at the beginning	192	1	trollabot
193	sorry tati wasn't listen to you. i was spinning on chair	193	1	trollabot
194	There is no such thing as too many monks	194	1	trollabot
195	oh yes we need to ban malay for them... but they probably didn't prepare so let's ban a meso civ	195	1	trollabot
196	you're on a boat? a motherfucking boat!	196	1	trollabot
197	"I'm not supposed to talk about that, or is it announced?"	197	1	trollabot
198	T90 we could have been the winners... well i could be the winner and you my sidekick	198	1	trollabot
199	"I do not have Snapchat I am over 20 years old and I am also human and male"	199	1	trollabot
200	that was a dumb fuck	200	1	trollabot
201	Time for plan B!	201	1	trollabot
202	"200 quotes? time to write a book"	202	1	trollabot
203	"He knows the way man, better get my own cutters"	203	1	trollabot
204	he betray me before i betray him	204	1	trollabot
205	if i was fully myself on stream twitch would ban me	205	1	trollabot
206	ey, you are not allowed to have that as an army!	206	1	trollabot
207	Is that really a question? Of course New Eagles man, look how pretty they look!	207	1	trollabot
208	Spit on it fucking sake	208	1	trollabot
209	he will never see it coming! oh man he saw it	209	1	trollabot
210	I feel special dautLove	210	1	trollabot
637	send more man i'm bored vs only 2 players	604	1	carloscnsz
643	"i hate the quickwalls man. if you are caught, you should die."	609	1	artofthetroll
646	How did I end up being this good? I don't know. Nobody does.	611	1	artofthetroll
647	"i'm the best player in the world man! i'm sick!"	612	1	carloscnsz
625	oh oh!! look at the meat	592	1	carloscnsz
248	time to join heresy	248	1	trollabot
211	Do u think i will read whatsapp? i only read twitch chat... and only in between games Kappa	211	1	trollabot
212	"First we steal your game, now we steal your viewers! Who is the biggest lamer now!" daut after mbl host him	212	1	trollabot
213	Have you met my friend Stu? Stu Pidasso? -ArtOfTheTroll	213	1	trollabot
214	I can't quick wall everything I'm not that good	214	1	trollabot
215	Let's end the game with this little pretty units fighting	215	1	trollabot
216	"fly little guys! fly!!! I BELIEVE U CAN FLY!!!" Daut trying to garrison vills one tile away from tower	216	1	trollabot
217	U never have enough forward villagers	217	1	trollabot
218	"I hope hico is not watching this stream" Daut 15min after getting 10k dono	218	1	trollabot
219	You eat banana	219	1	trollabot
220	Stop microing or i will get ballistic!	220	1	trollabot
221	everything that mbl says is correct	221	1	trollabot
222	"will totally not make fun of tati if he lose" DauT watching Tatoh Vs St4rk	222	1	trollabot
223	i'm going to be a good little husband and take my gf to doctor dautKing	223	1	trollabot
224	"you are not attractive you are fat fuck" Daut to F1re	224	1	trollabot
225	You are not a good player cuz you can't find your sheep	225	1	trollabot
226	F1re: "where are you taking gold?" DauT: "Wherever the fuck i want man"	226	1	trollabot
227	"I am a little bitch"	227	1	trollabot
228	are u going party with your grandmother?	228	1	trollabot
229	everything turn from a little bit wrong into DISASTER!	229	1	trollabot
230	I'm not training viper i'm giving him false hopes	230	1	trollabot
231	oh man they fuck me in the ass... was not pleasant	231	1	trollabot
232	I'm not medic man. I'm bush	232	1	trollabot
233	don't land where u r going to get shooted	233	1	trollabot
234	When u live in an island everything is in the way to manchester	234	1	trollabot
235	I will rush this lovely fucker	235	1	trollabot
236	Best way to fix this is shit tons of TCs	236	1	trollabot
237	every poem is a valid one	237	1	trollabot
238	Top score! Top fucking score!!!	238	1	trollabot
239	"it's a safe castle, not a daut castle" dautCastle	239	1	trollabot
240	thats why hand cannoneers suck you need to babysit them all the time	240	1	trollabot
241	Worst thing would be if i finish my 5tc boom and all my allies are dead and have to resign	241	1	trollabot
242	"DauT is out" is funny because it rhymes	242	1	trollabot
243	"Is a showmatch! IS A SHOWMATCH YOU FUCK!!! .... ok ok he will pay for that" daut after fire lamed his vill	243	1	trollabot
244	Maybe i can trap them all... or die trying	244	1	trollabot
245	that's a jordan bam	245	1	trollabot
246	to be in the same team with fire again is my dream	246	1	trollabot
247	i will go maa yolo or some shit to end this	247	1	trollabot
249	No Jordan can beat me	249	1	trollabot
250	I can't wait to see you again mbl	250	1	trollabot
251	Help? Where? Oh no that's too far away from me	251	1	trollabot
252	Oh full wall... this should put an end to my tower rush... That's what he will like to think	252	1	trollabot
253	oh come on! you are spanish and you are villagers	253	1	trollabot
254	I didn't plan to do that. But I like it	254	1	trollabot
255	"get armour man, knights are shit without it'	255	1	trollabot
256	"well stop microing if you wanna fight with honour"	256	1	trollabot
259	"hopefully I go to his economy before he comes to mine, heh heh nice spider senses"	259	1	trollabot
260	"I feel like I am playing this game like MbL"	260	1	trollabot
261	hit you fuckers AHHH!	261	1	trollabot
262	what? I am top score?	262	1	trollabot
263	If there is a nac3 and fire and me qualify i will pay individual room	263	1	trollabot
264	If fire was vipers roomate he wouldn't have won nac finals	264	1	trollabot
265	when I am with Jordan, it's hard not to laugh!	265	1	trollabot
266	I know how to handle my night life	266	1	trollabot
267	I'm not your content! I'm my own content	267	1	trollabot
268	"We are not splitting. That bit is whole mine" Daut about to play a showmatch with 1bit as prizepool	268	1	trollabot
269	towers are our future, not kids	269	1	trollabot
270	How i'm top score if i had nothing?	270	1	trollabot
271	How do u tower rush someone who has no economy man?!	271	1	trollabot
272	he wants to kamehame my ass man!	272	1	trollabot
273	Tatoh can't go to sleep he is too excited watching me microing	273	1	trollabot
275	don't micro the ram man! let it die!	275	1	trollabot
276	every time i lame something die inside me	276	1	trollabot
277	mbl should pay a fee for those sheeps	277	1	trollabot
278	"Luring is for tryhards"	278	1	trollabot
279	Not a single kill? that was supposed to be a double kill!	279	1	trollabot
638	"it's full madness here"	605	1	artofthetroll
648	Tomorrow i will start streaming 8am!	613	1	carloscnsz
626	I almost think going for elephants was a mistake	593	1	carloscnsz
280	When mbl is the responsible guy from your team. You are doomed	280	1	trollabot
281	"I read people minds! do u see that!"	281	1	trollabot
282	"Where is my team? my team is disappearing!!!" Daut playing tg with memb and britney	282	1	trollabot
283	Go for the rattan fuckers!!	283	1	trollabot
284	This game is full of Daut castles	284	1	trollabot
285	When u r playing with Memb u never look at the score!	285	1	trollabot
286	Why are we on teamspeak? to make the loss funnier	286	1	trollabot
287	no no no no... it was good... good for them i mean	287	1	trollabot
288	I don't want to be the guy making monks to counter elephants man... feel dirty now	288	1	trollabot
289	I fixed my sleep schedule! I'm european now	289	1	trollabot
290	If we win it's working	290	1	trollabot
291	I'm top score man! If u are top score is cuz u r doing something right	291	1	trollabot
292	We win this game in exactly 5 minutes	292	1	trollabot
293	Fuckers wall these days	293	1	trollabot
294	I do care about your opinion tatoh	294	1	trollabot
295	Our team composition is pikeman man 1111	295	1	trollabot
296	Noooo! no my relics! relics give me a hope for a better tomorrow	296	1	trollabot
297	sometimes i'm a little bit late and viper is a little bit mad	297	1	trollabot
298	I want to stay on hill you dumbfuck	298	1	trollabot
299	we re not allowed to praise viper here	299	1	trollabot
300	people don't like when I'm myself. I don't even like me when I'm myself	300	1	trollabot
301	Lierey is my adopted kid. I will make a man out of him	301	1	trollabot
302	being in TS with aM is too many information at the same time and all the information is useless	302	1	trollabot
303	can we start feudal? dark age too long man!	303	1	trollabot
304	no no no... Scorps are viper thing. Ballista eles is my thing	304	1	trollabot
305	damn right, it's all about being unique	305	1	trollabot
306	double nothing is nothing man, easy maths!	306	1	trollabot
307	i'm in secret to win games and to piss off viper	307	1	trollabot
308	Best thing is to piss off viper and troll around and boom and then win the game	308	1	trollabot
309	who needs boar when u can wall	309	1	trollabot
310	if i'm fuck then we are all fuck	310	1	trollabot
311	Portuguese and vietnamese now this is mbl dream. He can lame whoever he wants	311	1	trollabot
312	I never get fucked	312	1	trollabot
313	Kids in the game, kids outside the game, what the fuck is wrong with my life?	313	1	trollabot
314	"enjoy point of view, i'm freaking playing here" DauT to nili cuz nili started to sling him in post imp game	314	1	trollabot
315	oh fuck i made scouts instead of paladins. oh well it's ok they will think i'm out of gold	315	1	trollabot
316	"let's host the girl. probably viper is playing with the girl"	316	1	trollabot
317	I pray for the new age2 DE get smarter deers	317	1	trollabot
318	what is the difference between a 2k and a 2k5? well u can go watch nili stream and then come back to mine and see the diference	318	1	trollabot
319	everyday i fight those deers man! is so annoying	319	1	trollabot
320	ModeratorVerifiedNightbot: Yes i'm trying to be the streamer cuz t90 is trying to be the player	320	1	trollabot
321	i wish hera was here to share this amazing moment of casting t90	321	1	trollabot
322	The good caster will need more sisha	322	1	trollabot
323	When eagles kick in the problems begin	323	1	trollabot
324	Why are we top score? probably because of me	324	1	trollabot
325	"He doesn't read about civs" daut implying that enemy doesn't know about teutons bonus	325	1	trollabot
326	ohhh now i have to quick wall everything.. let's better resign	326	1	trollabot
327	we can go 2v2 and whoever gets nili is screwed	327	1	trollabot
328	only nili knows what he is doing	328	1	trollabot
329	i almost want to close the stream and go to hico's house to make fun of him	329	1	trollabot
330	i should pm him to resing... i mean look at his score	330	1	trollabot
331	to be fair deers are quite easy to push	331	1	trollabot
332	lame teammates is vipers and mine speciallity	332	1	trollabot
333	this was stupid... but it's ok, i'm stupid	333	1	trollabot
334	come on spearmen, what are you doing? are you drunk or something?	334	1	trollabot
335	what is he doing? OH WHAT I AM DOING???	335	1	trollabot
336	"I don't want to deal with this shit anymore" proceeds to drop a fwd castle	336	1	trollabot
337	cmon die you little girl	337	1	trollabot
338	"maybe this is a bit bit agressive castle" Kappa dautCastle	338	1	trollabot
339	Farms is the imperial age unit to make	339	1	trollabot
340	i don't think even i could throw this game.. but never say never	340	1	trollabot
341	"Fine.. die! See if i care" daut talking to a deer	341	1	trollabot
343	"this is our homemap" few mins later "we did not practice this enough man" DauT casting Secret vs Suomi	343	1	trollabot
639	ohh oh nili you can't play. is 2k3 room	606	1	carloscnsz
645	I am made of micro	610	1	artofthetroll
627	I want vivi man! he is the best teammate i ever had	594	1	carloscnsz
258	Freaking luck wasted in practice	258	1	trollabot
344	There is no pride in laming in tournaments	344	1	trollabot
345	Come on Tati get fletching and do something	345	1	trollabot
346	They don't know how to play so they won	346	1	trollabot
347	"i don't know man. I'm here just to cheer for Frantic" DauT kinda ignoring nili questions while casting Heresy vs Frantic	347	1	trollabot
348	Indian camels? Frank camels seems to be the way!	348	1	trollabot
349	come on monks, convert faster lads.	349	1	trollabot
350	He forced me to do stupid moves	350	1	trollabot
351	"I thought I was Jordan as well!"	351	1	trollabot
352	did i lost villager? mm no? idk what happen	352	1	trollabot
353	Fun fact! i didn't ask slam if he wanted to play. i just sign up both of us	353	1	trollabot
354	"what friend? u have no friend tatoh! i'm your only friend"	354	1	trollabot
355	Sansa Stark is like Jordan in Secret	355	1	trollabot
356	aM plebs always trying to be Secret	356	1	trollabot
357	"You lost tatoh! i won!" daut talking about a game where tatoh was his teammate	357	1	trollabot
358	"MVP stands for Most important player" dautWat	358	1	trollabot
359	I'm always romantic	359	1	trollabot
360	You can do whatever you want. I will do the same	360	1	trollabot
361	if I wanted to work I would get a job	361	1	trollabot
362	ModeratorVerifiedNightbot: he thinks Khmer elephants are strong? wait.. just wait.	362	1	trollabot
363	how do I quickwall out of this problem?	363	1	trollabot
364	I cant get through the shield of the meat man!	364	1	trollabot
365	they tickle shit out of me man	365	1	trollabot
366	he only got monks hes not cool person (daut going full elephants into monks)	366	1	trollabot
367	faith is useless I'm still getting converted	367	1	trollabot
368	"Now i will go to watch your stream and watch you lose every game" DauT to viper at the end of the stream	368	1	trollabot
369	It's one's personal opinion if u want to stay young or not	369	1	trollabot
370	Age of Empires keeps you young	370	1	trollabot
371	Is not trolling if it works	371	1	trollabot
372	Deleting your TC in team game is disrespectful to your allies. You only do that in meaningless 1v1 tournaments	372	1	trollabot
373	I have powers everywhere i go	373	1	trollabot
374	Nili and the random 1500 needs to split... i prefer him	374	1	trollabot
375	Why do u want me to play with no mistakes man? then we win and we start a new game. let's enjoy this one	375	1	trollabot
376	Me good micro	376	1	trollabot
377	Oh hera... you are a caster now! u should ask nili for caster coaching	377	1	trollabot
378	they are trebing my ass man	378	1	trollabot
379	The fuckers are fucking me man! i can't handle this!!!	379	1	trollabot
380	Is not yours if it's under my TC	380	1	trollabot
381	Don't "good luck have fun" me now	381	1	trollabot
382	when somebody ask me to micro for them you know shit is really bad	382	1	trollabot
383	yeah... people loves to smurf on my stream	383	1	trollabot
384	"u need to forget everything that hera told you" DauT coaching Tek	384	1	trollabot
385	Columbus reach The Americas faster than Viper Serbia	385	1	trollabot
386	Viper lives in north pole. He knows Santa	386	1	trollabot
387	Whatever i said I'm right man cuz i'm the older one here	387	1	trollabot
388	I'm not a stubborn old man	388	1	trollabot
389	I will go full mbl now on this guy... I need to take this out of my system	389	1	trollabot
390	Man it's so laggy to micro like a beast	390	1	trollabot
391	Someone is not doing anything and it's not me	391	1	trollabot
392	We don't trade man! We kill!	392	1	trollabot
393	I would kick you as well Tati	393	1	trollabot
394	oh i spent like 1k wood on quickwalls! can't even build barrack now.. was worth it 11	394	1	trollabot
395	oh i have counter to his units. Is called TC	395	1	trollabot
396	there is a certain amount of games that u can play a proper army composition, after that unique units are too cool to resist	396	1	trollabot
398	"We can't lose while we're winning"	398	1	trollabot
399	I don't even know what i'm doing! Will i go castle age? or feudal?	399	1	trollabot
400	Look at my top score and i don't even have a scout	400	1	trollabot
401	Don't let that winter come to me Tati!	401	1	trollabot
402	let me finish paladin upgrade first and then we can go and die	402	1	trollabot
403	If u are a kid and u own the ball u have guaranteed a place in the game	403	1	trollabot
404	If i had to replace viper with other player i would chose Jordan dautJordi	404	1	trollabot
405	Camels are smarter than knights	405	1	trollabot
406	It wasn't a prepared strategy. Just looked smart by accident	406	1	trollabot
407	"no no! don't tell the enemies we hate water! let them think we love to micro galleys and shit" Daut discussing strategies with slam	407	1	trollabot
640	now i look like a stupid and an idiot	607	1	carloscnsz
871	The masterpiz backfired	805	1	carloscnsz
628	You died like a pig man	595	1	carloscnsz
408	PLaying safe in a tournament? what year is this?	408	1	trollabot
409	We got 3 wins and we are badasses	409	1	trollabot
410	Where is fire? oh he is playing team game alone	410	1	trollabot
411	why can we just patrol each others like normal humans ?	411	1	trollabot
412	hope the scouts will be gentle on me	412	1	trollabot
413	You can't raid me, I don't have anything	413	1	trollabot
414	Quite a daut lumbercamp	414	1	trollabot
415	i have a few demos... how many is a few? less than one	415	1	trollabot
416	If artofthetroll was in my wedding, people will confusing him with my son	416	1	trollabot
417	Viper: "U didn't invite MBL to your wedding cuz he lames you?" DauT: "I should invite him and kick his ass"	417	1	trollabot
418	You can basically lick tati before tequila. cuz he is so salty	418	1	trollabot
419	I'm memb man, I'm a caster man. what next? BibleThump	419	1	trollabot
420	Good luck guys, hope you lose	420	1	trollabot
421	Did hera give you coach and you are back to 1600 again?	421	1	trollabot
422	wtf man! slam is top15 player man! and no! he doesn't come up with strategies! but he listen and he performs	422	1	trollabot
423	Nothing is more important for me that your love viper	423	1	trollabot
424	who cares about micro when enemy is losing villagers	424	1	trollabot
425	oh man what's my civ? .. Elephantos man!!!	425	1	trollabot
426	I lost all my economy to lions	426	1	trollabot
427	I'm not dead, he is dead!	427	1	trollabot
428	tatoh! go with jordan man! fuck viper	428	1	trollabot
429	Hera is like mini mbl man	429	1	trollabot
430	daut: "oh we can teamwall tati! lets teamwall" viper: "yeah we can teamwall" daut: "nobody ask you"	430	1	trollabot
431	we make units and kill	431	1	trollabot
432	bad luck in 4 games is not bad luck	432	1	trollabot
433	So much potential to make fun of the people i know	433	1	trollabot
434	Did you burn your house?	434	1	trollabot
435	When you believe hard enough your tower may hit something	435	1	trollabot
436	How can you steal T90 content now that you are playing aoe1 viper?	436	1	trollabot
437	Just plug off the cable! there is always a way to fake a restart	437	1	trollabot
438	This is regicide, we always win	438	1	trollabot
439	Screw the money, making fun of viper is all that matters	439	1	trollabot
440	I will win aoe1 tournament and never play it again. I will say then "i'm the best aoe1 player, i'm retired now"	440	1	trollabot
441	oh!!!! look at his villagers!! 11 welcome to the defeated land	441	1	trollabot
442	Everything more than 2 clicks is hard work	442	1	trollabot
443	Win this and brag in tatoh's face is the best prize i can get	443	1	trollabot
444	I need to wake up at least at 2pm to don't miss my son's birthday	444	1	trollabot
445	I need my beauty sleep	445	1	trollabot
446	It's really colorful in my side of the map	446	1	trollabot
447	"At least I have one relic" DauT after losing control of all his golds	447	1	trollabot
448	We destroy one guy and then we wall	448	1	trollabot
449	Well sorry for being that good	449	1	trollabot
450	I want castle here. I get what i want	450	1	trollabot
451	I was touched man WutFace	451	1	trollabot
452	(DauT to SY in a TG Nomad) compared to these guys mbl is more fun to play against!!	452	1	trollabot
453	his walls got walls man	453	1	trollabot
454	I'm gonna treb his ass to the freaking dark age	454	1	trollabot
455	Worst case scenario I will end this game with an amazing DauT castle	455	1	trollabot
456	Freaking tati! stop drinking and smoking! Is bad for your health	456	1	trollabot
457	There is always a fail	457	1	trollabot
458	If there is a wall there is a fail	458	1	trollabot
459	I win to the deer but i lost to the boar	459	1	trollabot
460	I'm a little bit mbl these days	460	1	trollabot
461	"I would have won this game earlier" Daut casting Max vs Viper	461	1	trollabot
462	Shit is getting trickier everyday man	462	1	trollabot
463	"I have no clue what i'm doing with my life"	463	1	trollabot
464	I have no clue why u guys are watching this shit	464	1	trollabot
465	This is family friendly stream man! I have family and i am friendly	465	1	trollabot
467	I don't trashtalk fire man, i tell the true. Is not trashtalk if its true	467	1	trollabot
468	don’t fuck, man...girl!	468	1	trollabot
469	zombie game is something mbl would enjoy. just wall up and make towers!	469	1	trollabot
470	nah i won't be greedy... i think i have been making to many daut castles in my life	470	1	trollabot
471	But what you must understand, I am a lazy fuck.	471	1	trollabot
472	He doesn't give a single fuck man	472	1	trollabot
473	Man in old days i prefered to type instead of talking but nowadays i'm too lazy to type	473	1	trollabot
474	I know u guys wanted a daut castle but i can't	474	1	trollabot
475	I spent all day microing farms man	475	1	trollabot
875	I fail as much as ai could	809	1	carloscnsz
629	You need to get baby viper! Dogs are easy	596	1	carloscnsz
1	"my quick wall is quick fail"	1	1	trollabot
2	"this zebra is asshole"	2	1	trollabot
3	"Build faster you little fuckers."	3	1	trollabot
4	"time for what we've all been waiting for. time for a dautCastle"	4	1	trollabot
5	"Everything is invisible to me"	5	1	trollabot
6	"You know the best thing, if I get divorced I probably have to pay nothing because I'm unemployed"	6	1	trollabot
7	"I will bench you so hard that you will never stand up."	7	1	trollabot
8	I don't need to micro. I have hill	8	1	trollabot
9	I don't need people to die man!	9	1	trollabot
10	"Look at this Daut mill. Best mill in the game!"	10	1	trollabot
11	"Maybe I should shave before the stream. Then again, who gives a shit!"	11	1	trollabot
12	"You see gold, you make mining camp..."	12	1	trollabot
13	"He challenged me in Vodka war. He will fail."	13	1	trollabot
14	"Even wolf is being an asshole"	14	1	trollabot
15	"... and you let JORDAAAAN play?!"	15	1	trollabot
16	Forest Nothing is like freaking game of thrones man!	16	1	trollabot
17	"They are hand, and they are cannons"	17	1	trollabot
18	Nothing type of maps are quite fun	18	1	trollabot
19	Kiss me you beautiful son of a bitch!	19	1	trollabot
20	I will send friend request to mbl	20	1	trollabot
21	ok, ok, lets patrol this army and never look at them again...	21	1	trollabot
22	I need to balance troll	22	1	trollabot
23	"I'm a professional." - DauT 2018	23	1	trollabot
24	"i'm going around here fuck off of me" DauT to Viper during Nomad 2v2 Tourney	24	1	trollabot
25	i send 1k wood. i'm a good teammate	25	1	trollabot
649	He knows man at arms opening is coming and I will fucking give him man at arms opening	614	1	carloscnsz
768	I'm going paladin upgrade... Because i can!	733	1	carloscnsz
779	I'm streaming at all but i will wait instead of playing just to tilt mbl more	744	1	carloscnsz
796	"yes viper we are enemies... and tomorrow as well" they had showmatch vs aM next day	761	1	carloscnsz
853	I shift clicked it man.... i shift failed	787	1	carloscnsz
872	You wanna fuck up with elephants man? You can not	806	1	carloscnsz
876	Nothin is dying for him man!!!	810	1	carloscnsz
878	"Fire you were closing to lose your iphone and your life" daut talking about this clip https://www.twitch.tv/f1reaoe/clip/ReliableSmilingDuckDerp	812	1	carloscnsz
886	I’m like the worst shift clicker in the fucking game	820	1	zekleinhammer
895	“Playing against inc makes me feel like Lierry”	829	1	carloscnsz
903	let’s not go too all in - DauT with 0 food in the feudal age	837	1	zekleinhammer
905	choose the big one, it's the safest bet man!	839	1	hyunaop
906	we are chilling. I sound like a Hera now! but we are definitely chilling. meanwhile we can enjoy shisha	840	1	hyunaop
907	when they introduce auto micro, then we will talk	841	1	zekleinhammer
908	gg... He didn't let me enjoy...	842	1	carloscnsz
938	how do I go YOLO when I don't know where enemy is?	872	1	hyunaop
968	OHHH HE IS MAKING ELEPHANTOS!!! I want to lose now man... He deserves the win	902	1	carloscnsz
1003	i Over boomed but i was expecting to lose more villagers than this	937	1	carloscnsz
1021	Looking at nili defense is like watching AI playing	955	1	carloscnsz
1071	Yeah he got gold at the back... A bit lucky but a bit not enough	1005	1	carloscnsz
1085	He is playing full army without full army	1019	1	carloscnsz
1096	Pocket doesn't matters, unless is daut pocket, then it matters a lot	1030	1	carloscnsz
1104	oh man those castles are FULL of arrows	1038	1	zekleinhammer
1111	"Pfffft, I don't go Skirms in a team game, I am not MBL!"	1045	1	byelo
1166	I will never troll viper. You don't know me	1068	1	carloscnsz
1179	Tatoh man! he is raiding me!! I have a daut castle every single game!!!	1081	1	carloscnsz
1187	First I take those sheep that are probably yours	1089	1	carloscnsz
1195	He is sending everything into the nothing	1097	1	carloscnsz
1206	micro and me had fight	1108	1	hyunaop
1223	I could teach you guys my disaster build orders but they are expensive	1125	1	carloscnsz
1230	now I have to scout for 1 cow? I'm not doing that. they can give me that 1 cow next patch!	1132	1	hyunaop
1238	I do want to be nice and sell out but I don't want to be t90	1140	1	zekleinhammer
1245	What do we said about the farms? Not today!!	1147	1	carloscnsz
1255	"Dropping a siege workshop would be a smart move; dropping a castle would be a cool move."	1157	1	fatrhyme
1261	He must be really scared right now... of me hitting nothing	1163	1	carloscnsz
1272	Let's go with an offensive castle first	1174	1	carloscnsz
1276	well elephants are there to... Look cool?	1178	1	carloscnsz
1283	Why is my economy so good?	1185	1	carloscnsz
1292	Now when I fail I take my whole team down with me	1194	1	carloscnsz
1295	hide the low hp, because that boar is microing	1197	1	zekleinhammer
1297	let's go flaming fuckers! bam! bam!	1199	1	zekleinhammer
650	I was semi-smart at least	615	1	carloscnsz
656	"I can be polite as fuck man!"	621	1	goldeneye_
657	You are a brainless little fuck	622	1	carloscnsz
660	Ok ok too much vipering too much vipering	625	1	carloscnsz
662	I went tryhard but full disaster	627	1	carloscnsz
663	Now this is a good boar... now is an asshole... now is good again	628	1	carloscnsz
666	Look at my beastly score	631	1	carloscnsz
669	Baby is crying and i'm not there... that's cute and annoying	634	1	carloscnsz
670	There is no way he can hit me... my micro is just that good	635	1	carloscnsz
672	if the girl is Roxy I don't want it, man	637	1	zekleinhammer
675	I like saying nice things about myself.	640	1	artofthetroll
676	Not paying attention is my transition	641	1	carloscnsz
681	"this time i won't drink" daut refering to NAC3	646	1	carloscnsz
684	"micro nerd that! young fuck!"	649	1	hyunaop
693	I'm nice but I still got that asshole bit inside of me!	658	1	hyunaop
696	I read your mind man! YOu can't beat the mind reader	661	1	carloscnsz
698	I wish i was responsable	663	1	carloscnsz
704	i stonewall the shit out of him	669	1	carloscnsz
710	i'm scouting tatoh heavily	675	1	carloscnsz
720	I need myself here	685	1	carloscnsz
723	Still smells like a fast castle	688	1	carloscnsz
724	"how slow are you man?" "just like my APM"	689	1	hyunaop
725	Let's just push man i'm tired of following	690	1	carloscnsz
727	I'm bored someone sling me	692	1	carloscnsz
728	But no! it was like Fuck!	693	1	carloscnsz
730	if i masterpiz viper i will be so happy! .. OHH OHHH You are my bitch now	695	1	carloscnsz
731	"Grid mod is not working. But then again, grid mod is for tryhards" Daut being to lazy to reinstall grid mod	696	1	carloscnsz
732	there is no honor on spamming galleys for one hour	697	1	carloscnsz
733	My body is sick but i'm fine	698	1	carloscnsz
735	oh man! he is really into the micro	700	1	carloscnsz
736	yeah.. there was a hole... There is a hole in my fucking brain as well	701	1	carloscnsz
738	"Ok guys food is ready. Give me 4min and then we continue to 1k7" daut with an 1k8 elo	703	1	carloscnsz
743	Nah he wasn't salty. Although i actually didn't read his message	708	1	carloscnsz
746	Jaguars are destroying my ass	711	1	artofthetroll
747	Most of the time in nac i spent it sleeping	712	1	carloscnsz
748	man you saw that game! You lost it but it was so fucking fun	713	1	carloscnsz
751	"Nice scouting by him. He knows everything - so he knows he's going to die"	716	1	artofthetroll
752	why pause so amazing game?	717	1	carloscnsz
758	MBL! You were the reason i started streaming	723	1	carloscnsz
769	I help and he wants to fight. kids these days!	734	1	hyunaop
780	"Just boom man, who gives a shit?"	745	1	artofthetroll
781	Maybe best thing i could do is just resign and stop wasting time ... I like wasting time	746	1	carloscnsz
785	I'm so fail man	750	1	carloscnsz
797	I'm a pasta guy	762	1	carloscnsz
798	Run away from my HP man!	763	1	carloscnsz
799	"I'm lierey, la la la!"	764	1	artofthetroll
801	i think i don't have handcart. But it doesn't matters, i'm playing nicov, so he doesn't have it either	766	1	carloscnsz
809	I don't even know when i'm sleeping anymore	774	1	carloscnsz
843	Lierey is like Hamster. he just collect everything	777	1	carloscnsz
854	¨Pressure is high because i made fun of mbl for losing here.... I cannot lose now	788	1	carloscnsz
855	Why are you guys so stupid?	789	1	carloscnsz
862	Yeah i'm married.. even had kids and shit	796	1	carloscnsz
873	Oh Fuck! is 10am!	807	1	carloscnsz
877	Do i look like i ahve any job at all?	811	1	carloscnsz
879	idon't give flowers to my wife cuz she will think i fucked up, cheating or sometihing	813	1	carloscnsz
887	the best plan is always an easy win	821	1	zekleinhammer
896	Should i cancel my hoang rush... I cancel nothing man!	830	1	carloscnsz
904	"I'm not Hera I don't ask my parents' permission"	838	1	artofthetroll
909	Tell my best joke? ... Nili is a good player	843	1	carloscnsz
911	We were supposed to chill and boom man! now you are breaking the rules	845	1	carloscnsz
915	"In HC I will do outposts and pretend I'm an idiot"	849	1	carloscnsz
916	"you see there are battles here. the shades of the colors are changing"	850	1	artofthetroll
917	Thanks for 2k dono matt, i hope you fail	851	1	carloscnsz
919	At times you fail and it still works out!	853	1	byelo
920	I think kamikaze tower rush is the only choice	854	1	carloscnsz
926	you should always trust robo	860	1	zekleinhammer
928	went imp and still making castle age units? Good job, me!	862	1	zekleinhammer
929	Fuck my life! this kids are getting better	863	1	carloscnsz
939	I wouldn't like to be under mbl's command	873	1	zekleinhammer
942	I need a lot of things, and I need them fast!	876	1	zekleinhammer
949	That's my game plan here... believing...	883	1	carloscnsz
954	achievements? I have no time for achievements man!	888	1	hyunaop
651	ohh... he is now retreating and shit	616	1	carloscnsz
658	"Eventhough I have a wife and 2 kids, I will still have sex with you"	623	1	walterekurt
661	If I cannot read Nili’s mind, what the fuck am I doing here?	626	1	zekleinhammer
664	"he mastapieced shit out of me there"	629	1	artofthetroll
667	This is a typical memb tower man in the middle of nowhere and protecting nothing	632	1	carloscnsz
671	No no no don't come to Belgrade	636	1	carloscnsz
673	"If i go elephants that would screw me over" .. clicks elite war elephant tech on castle	638	1	carloscnsz
677	Kreepost are so small now dautLove they are so cute! they are cuteposts now	642	1	carloscnsz
682	Edie is my french fairy	647	1	carloscnsz
685	Luring deers is for people that care.	650	1	artofthetroll
694	"I believe in you, snowballs!"	659	1	byelo
697	converting is faster than delete button	662	1	carloscnsz
699	I'm Youtuber aswell	664	1	carloscnsz
705	"Nicov! i want your puntos not your viewers!!" Daut after getting hosted by nicov	670	1	artofthetroll
711	"Why should i make army when everybody is walled" Daut while viper was getting destroyed 2v1	676	1	carloscnsz
721	"I'm happy for you but I'm not happy for myself"	686	1	hyunaop
726	ohh viper?? i would like an ally!	691	1	carloscnsz
729	To be fair deers were too close so i needed to make it harder	694	1	carloscnsz
734	"Finally Tati is useful" Daut after Tatoh agreed with him	699	1	carloscnsz
737	if a made some micro that no one ever saw...	702	1	carloscnsz
739	"Yeah.. ok .. Fuck You!" Daut to one of his deers	704	1	carloscnsz
744	Will this villager war ever end? i hope not!	709	1	carloscnsz
749	I will risk it man! we all love those castles	714	1	carloscnsz
753	An then we will see nicov! who is food and.. who is top player	718	1	carloscnsz
759	oh hello there! You want to expand and shit ?	724	1	carloscnsz
770	at least when I'm stupid. I am stupid with style!	735	1	hyunaop
782	I don't have a castle man! Nor many villagers.. Nor chances of winning this game.... But i like playing	747	1	carloscnsz
786	He got all the monk	751	1	carloscnsz
800	camaaan you are cavalry archer man! you archer the horse!	765	1	carloscnsz
802	the mistakes like this, only i made	767	1	carloscnsz
810	my kid is waking me up and being an asshole	775	1	carloscnsz
844	This is my job man! I'm telling my family i'm working	778	1	carloscnsz
856	Hera.. is not all about educational game you fucking nerd	790	1	carloscnsz
863	yeahh.. i just made stable to be there looking good	797	1	carloscnsz
874	i still have one fucker outside	808	1	carloscnsz
880	Maybe is time to start the fail lure dautLure	814	1	carloscnsz
888	it was two militia now it’s three? They are multiplying	822	1	zekleinhammer
897	if he is going forward i call him stream cheater and resign	831	1	carloscnsz
910	So much difference when you have luck in your game	844	1	carloscnsz
912	"I've got balls, I am made of balls!" Daut before luring two boars at the same time.	846	1	byelo
918	I guess this is too greedy.... But i'm greedy boy	852	1	carloscnsz
921	Let's do a Daut castle and then end this with style.	855	1	byelo
927	oh are you trapped inside little eagle? oh, I don't have loom	861	1	zekleinhammer
930	look at the micro man, I don't know why I am doing all that!	864	1	hyunaop
940	me, mbl and megarandom. best love story.	874	1	hyunaop
943	they kill my future when they outmicro ballistics, now my future is gone.	877	1	hyunaop
950	Change of plans... I'm going to towering him like if there was no tomorrow	884	1	carloscnsz
955	I choose to play like a man: daut adding two barracks in dark age at 25 minutes	889	1	zekleinhammer
960	Next time i will coach you... You will still lose but i will get some money	894	1	carloscnsz
969	I have 7 villagers and I don't even know where they are	903	1	zekleinhammer
1004	I tend to fail a lot	938	1	artofthetroll
1022	I will wall and I will guide ad I will wall into wall and into guide wall	956	1	carloscnsz
1072	Memb is playing? Well i will have to wait for 20min queue to find him	1006	1	carloscnsz
1086	Ok Tatoh you practice with computer	1020	1	carloscnsz
1097	He is a team mate that makes outposts	1031	1	carloscnsz
1105	looks like the fucker is walled in	1039	1	carloscnsz
1112	First we became friends then you wish that never happend	1046	1	carloscnsz
1167	Oh fire was in stark's channel? But is he on stark's bat? that's the real question	1069	1	carloscnsz
1180	"I'm not losing with this many TCs" daut after 12TC boom + fishboom	1082	1	carloscnsz
1188	There is a limit to my micro tatoh man! And i'm above that limit!	1090	1	carloscnsz
1196	is not just a trade cart man! is MY TRADE CART!!!	1098	1	carloscnsz
1207	I was laughing at the demos man, who's laughing now? BibleThump	1109	1	hyunaop
1231	Get fuck right there!!!	1133	1	carloscnsz
1239	Ok, fuck that deer	1141	1	carloscnsz
1246	You can't do anything but you are a cool looking fucker	1148	1	carloscnsz
1256	Does he have a secret economy that he didn't report to me ?	1158	1	carloscnsz
1262	The mastapiz.... The mastaWhoCaresAboutItpiz	1164	1	carloscnsz
1284	I like my spot but i need more TCs	1186	1	carloscnsz
652	you get housed when you don't make houses	617	1	zekleinhammer
659	Look at this guys... WHAAAT!!! ok you guys can guess my point anyway	624	1	carloscnsz
665	I had a solid fast reaction and also have a solid asshole	630	1	carloscnsz
668	No i don't use Tinder... I'm already fucked	633	1	carloscnsz
674	"die you steroid units" ... daut sending elephants into elite kipchaks	639	1	hyunaop
678	"Who is screwing around with my pickle?" dautPickle	643	1	byelo
683	"he had a good life" Daut after losing scout under enemy TC	648	1	carloscnsz
686	i wish to under stand you hyuna. but then again you are a weird fuck... so thanks for the cheers	651	1	carloscnsz
695	To be fair my micro was really bad	660	1	carloscnsz
700	Troll, you sick son of a bitch, you know stuff.	665	1	artofthetroll
706	I'm microing my ass out here and i'm losing	671	1	carloscnsz
712	i'm only making kts so you don't cry	677	1	carloscnsz
722	"I'm sleepy, I'm hungry, I need to pick up my family. I'm so sad. I can't wait to go to Hamburg and relax for 10 days"	687	1	hyunaop
740	"Oh, it's a fail... Oooh, it's not a fail, it's a jebait" Daut saving a scorpion.	705	1	byelo
745	All my villagers are like one hit away from disaster	710	1	carloscnsz
750	fight like a man, die like a clown!	715	1	zekleinhammer
754	Ohh Double turtle! Double the pleasure	719	1	carloscnsz
760	DauT reading chat.. "TATOH: DauT!! stop ignoring me on discord fucker" .. DAUT: No!	725	1	carloscnsz
771	screw art of war someone should make a deer lure tutorial	736	1	hyunaop
783	good luck finding new opponent? that's the beauty of matchmaking. you can't escape me!	748	1	hyunaop
787	What? He think he can outmicro me? He doesn't know who i am or what?	752	1	carloscnsz
803	Run to your little TC	768	1	carloscnsz
811	I need a office where i can play and sleep	776	1	carloscnsz
845	I'm hot, funny and rich	779	1	carloscnsz
857	Ty for the gift subs man! Spread that christmas spirit! dautSanta	791	1	carloscnsz
864	And then they said is a hard game man... just wall and boom!	798	1	carloscnsz
881	Nooo! Don't be walled when i have so much food...	815	1	carloscnsz
889	my economy is four farmers	823	1	zekleinhammer
898	"Come on, not more wolves! Hoang, how do you always sneak those villagers?!"	832	1	byelo
913	I'm sick player, I don't even care if I lose anymore, already won in my heart!	847	1	hyunaop
922	It seems like it's a mbl game	856	1	carloscnsz
931	My towers are more expensive that his castle	865	1	carloscnsz
941	Maybe he is thinking that i have a defense... PLEB!	875	1	carloscnsz
944	i'm going to make a lot of happy towers	878	1	carloscnsz
951	Don't fuck with me kid!	885	1	carloscnsz
956	its so stupid it was funny man. such a t90 man!	890	1	hyunaop
961	the scout can only watch woman die there	895	1	hyunaop
970	I believe in my micro	904	1	zekleinhammer
971	death match is so easy, we need more death match tournament	905	1	hyunaop
974	It seems like I'm clown now dautArena	908	1	artofthetroll
975	Slam! thx for the sub! now turn on the game for some action!	909	1	carloscnsz
976	"you don't even know if it's day or night lately" daut talking about his sleep schedule	910	1	carloscnsz
978	I want all the pokemons	912	1	batbeetch
981	I will go for archers and my archers will be on elephants	915	1	carloscnsz
982	And we all know that cool plays win the games	916	1	carloscnsz
988	Micro Like a God! Die Like a Hero	922	1	carloscnsz
994	Seems like those flowers are never missing!	928	1	carloscnsz
1005	"Why are you all so low HP?" daut sending vills forward dautCastle	939	1	carloscnsz
1013	"I don't quick wall my villager... I beat him with my hands!"	947	1	carloscnsz
1014	I just needed fletching instead of lag	948	1	carloscnsz
1023	We won 2 out of one tournaments	957	1	carloscnsz
1024	Luckily tatoh will sling me stone. If not i will mine his stone	958	1	carloscnsz
1027	Suomi play stupids map good	961	1	carloscnsz
1033	how do I fix my economy at this point? *builds a castle with idles	967	1	zekleinhammer
1037	i will buy some hera coaching for Viper. Like a hera coaching gift card	971	1	carloscnsz
1041	we are extreme sports now, I live a dangerous life	975	1	zekleinhammer
1042	I feel like aM are my kids	976	1	zekleinhammer
1046	you don't need to leave my stream to hit 0. Twitch is doing that for you!	980	1	hyunaop
1051	even my amazing micro cant deal with that	985	1	hyunaop
1054	I will show him who is best pocket. I am getting outmicroed by zebra	988	1	batbeetch
1055	pick celts pocket and go fast castle mangonels. Is a bullet proof strategy man	989	1	carloscnsz
1056	I'm hosting and disrespecting at the same time.	990	1	goldeneye_
1057	I wear black underwear, I feel sexy like that	991	1	zekleinhammer
1058	how is it always arena against arena players? they can only ban four maps!	992	1	hyunaop
1060	"Everywhere I look I have a dead villager. This is why they invited shisha!"	994	1	byelo
1064	stop raiding me! it's not how we play this game	998	1	zekleinhammer
1073	I would need a forward castle and that's too risky	1007	1	carloscnsz
1077	I don't want to kill buildings, I want to kill people	1011	1	zekleinhammer
653	if you ever want to know how to micro just go to the VOD and watch it again	618	1	carloscnsz
679	paladins with +7 looks nice... but my mangudais look nicer	644	1	carloscnsz
687	grandpa got some moves, man	652	1	zekleinhammer
701	I'm currently number 9 in the ladder but fire is above me so i'm actually number 8	666	1	carloscnsz
707	Apparently lions are beasts.	672	1	artofthetroll
713	You make carto instantly just to know what i'm doing???	678	1	carloscnsz
741	Ah? wha? nah? wha? .... Why i'm an stupid person?	706	1	carloscnsz
742	I'm stupid and i'm proud of that	707	1	carloscnsz
755	that went well	720	1	zekleinhammer
761	In serbia womens say "screw that man! he doesn't weight more than 100Kg"	726	1	carloscnsz
772	heavy plow or TC? I say TC!	737	1	hyunaop
784	"Coaching from JonSlow? You sure you want to do that to yourself?"	749	1	byelo
788	everybody is fucking with deers early... bunch of fuckers	753	1	carloscnsz
804	That hole man.. that hole fucked me	769	1	carloscnsz
846	my life style.. i wouldn't recommend it	780	1	carloscnsz
858	Oh... well you can always do a daut castle	792	1	carloscnsz
865	i'm housed and i have no wood for house... kill me one villager man!	799	1	carloscnsz
882	"We were talking! you weren't supposed to see that!" daut when lierey evaded his mangonel shot while they were chatting	816	1	carloscnsz
890	When you lose galleys to a relic, you know you're fucked.	824	1	artofthetroll
899	"Houses are an overinvestment... cuz they cost wood" daut going for the super late game in team islands 1v1	833	1	carloscnsz
914	Do I really need conqs... Fuck yeah I need conqs!	848	1	carloscnsz
923	oh wait! We have turtles! he is dead! he is completely dead!	857	1	carloscnsz
932	Bodkin arrow is good when all your army is towers	866	1	carloscnsz
945	imagine age of empires university	879	1	zekleinhammer
952	"even if i delete all my walls i would fail here" daut luring deers on hideout	886	1	carloscnsz
957	he started it and he dot me man, don't dot me man!	891	1	hyunaop
962	i'm the princess of the black forest right now	896	1	carloscnsz
972	Every time i remember to do it it's really good	906	1	carloscnsz
977	i don't like booming girls	911	1	carloscnsz
979	Look at this old school camels they're not even on fire	913	1	batbeetch
983	i lost more villagers to boars than he lost to my push	917	1	carloscnsz
989	Stone is my best friend! I'm mongols!	923	1	carloscnsz
995	I'm training slam everyday so we don't need to use nili... I need extra prizepool for this	929	1	carloscnsz
1006	what's fire's weakest map? .. everything that is not islands	940	1	carloscnsz
1015	i would never be so mean with someone i don't consider a friend	949	1	carloscnsz
1025	you spit facts? I spit on your facts!	959	1	hyunaop
1028	this little bridge will be full of blood	962	1	carloscnsz
1034	What time it is? oh is time to call it	968	1	carloscnsz
1038	I'm just happy that that wasn't my hole	972	1	carloscnsz
1043	aM is not streaming but not cuz they are tryharding but to avoid the shame of losing like this on stream	977	1	carloscnsz
1047	you see a bunch of women running with knives and you hide in houses?	981	1	hyunaop
1052	I wanna join SY	986	1	hyunaop
1059	I saved 6 food. I'm happy!	993	1	hyunaop
1061	"So this is what it feels like to be Nili," said while dying in a TG	995	1	byelo
1065	other parents in parents in: how does your father have no job. DauT: because I do nothing man! dautKotd	999	1	hyunaop
1074	this is my last game... i have been streaming for 6hs already and if i wanted to work 8hs a day i will just get a proper job	1008	1	carloscnsz
1078	Viper: "Why is my range so short?" DauT: "Poor Debbie..."	1012	1	byelo
1087	This is so tilting man... Now i know how people feel when they cast my games	1021	1	carloscnsz
1098	Look at your score! No wonder why you didn't qualify	1032	1	carloscnsz
1113	I would prefer to beat viper myself but it would be more humiliating if fire beats him	1047	1	carloscnsz
1168	I have half HP villager without loom walling	1070	1	carloscnsz
1181	Man.. The moment when you start missing viper is the bad one	1083	1	carloscnsz
1189	nice help tatoh... WITH THE GENERIC STARTING UNIT MAN! Thanks you for the help	1091	1	carloscnsz
1197	I deleted hera in one samurai move	1099	1	carloscnsz
1208	that's kinda sleazy...that's what I do against F1re	1110	1	zekleinhammer
1209	I don't care man, wait actually I do!	1111	1	hyunaop
1213	it's a dautcaste! I know one when I see them!	1115	1	deagle2511
1215	i'm not switching to skirms like some pleb would do	1117	1	carloscnsz
1217	Oh no! i'm vs towers and my pocket is mister 0-4	1119	1	carloscnsz
1218	You are all problems	1120	1	carloscnsz
1232	You live, you learn... you wall..	1134	1	carloscnsz
1233	apparently Knights are slightly better than Scouts...	1135	1	hyunaop
1240	My school failed me, I only know cursing	1142	1	carloscnsz
1247	Everything is mistake here	1149	1	carloscnsz
1250	"I will be the person I hate, I don't want to hate myself" - DauT May 2020 BibleThump	1152	1	hyunaop
1251	I hate when other people think as well	1153	1	goldeneye_
274	my patrol is broken	274	1	trollabot
654	Should i trade all my maa for one villager? ... YEAH!!! Let's trade all my maa for one villager!!	619	1	carloscnsz
680	My knights are killing me but i'm happy	645	1	carloscnsz
688	it can be sick but it also can't be sick	653	1	carloscnsz
702	Plan is take all the relics and then chill until imp	667	1	carloscnsz
708	In hill we believe	673	1	carloscnsz
714	Where is the resign buttton? I dono man, never found that one.	679	1	artofthetroll
756	So much stone but no time to spend it	721	1	carloscnsz
762	When i was young i was going to the gym and shit. Now that i grow up i know that is more about money than looks	727	1	carloscnsz
773	So guys? What do you think about my micro? Was sick right?! i'm so proud	738	1	carloscnsz
789	Nili can't spy aM he doesn't know the game to be a good spy	754	1	carloscnsz
805	when i say maybe it means i will be greedy	770	1	carloscnsz
847	I'm playing players that are at T90 level.. and i'm losing man	781	1	carloscnsz
859	time for the surprise madafaka!	793	1	carloscnsz
866	Come to the castle! come and kiss that castle	800	1	carloscnsz
883	Deagle if you sub to me you will suck less	817	1	carloscnsz
891	A daut taunt pack? ... It' probably won't be allowed cuz i curse a lot	825	1	carloscnsz
900	We go hoang but with market	834	1	carloscnsz
924	Oh he is not even playing! come on! go out and play with me!	858	1	carloscnsz
933	when somebody is cool he is just born that way	867	1	carloscnsz
946	i want 3rd bombard cannon so i kill really fast	880	1	carloscnsz
953	I will host memb... Need to support the guy with a family in this dark times	887	1	carloscnsz
958	I'm so safe not even corona can enter my base	892	1	zuviss
963	Monks are friends with the fast imp	897	1	carloscnsz
973	DauT: Okay my favourite unit is elephant, I change my mind (nili: what was it before?) DauT: Mangudai	907	1	hyunaop
980	I'm a weird person man	914	1	carloscnsz
984	"You are a brave boy but stay at fucking home" daut after a vill went outside his base chasing enemy army	918	1	carloscnsz
990	Punishment is coming back to our city	924	1	carloscnsz
996	There is no rush for punishment	930	1	carloscnsz
1007	I don't need stone man! Stone is for losers that boom	941	1	carloscnsz
1016	you're Viper of course you're fine	950	1	batbeetch
1026	this is my team man.. always working against me	960	1	carloscnsz
1029	You can see that aM is playing serious cuz the benched nicov	963	1	carloscnsz
1035	I don't want to play the arabia game.. Oh tatoh is not playing? then i want to play it. I don't want to play with tatoh yelling at me	969	1	carloscnsz
1039	Tatoh!! I'm going for the forward castle man! You want to support that cause	973	1	carloscnsz
1044	tatoh is pushing enemy deer to TC. That is like disrespect outpost rushing!	978	1	hyunaop
1048	ho ho, Hera snipe: DauT attacking a full hp Incan villager	982	1	zekleinhammer
1053	Comodo Dragons on Arabia? Makes no sense.	987	1	hyunaop
1062	I need to hit it twice to make it personal: DauT talking about laming boars	996	1	zekleinhammer
1066	"Look at him trying to fast castle like if its arena" Daut playing arena against dracont	1000	1	carloscnsz
1075	Do i ever dream about viper? Everyday man	1009	1	carloscnsz
1076	daut kids vs viper kids?? I'm not even sure if he knows how to make one	1010	1	carloscnsz
1079	just because they cannot make cool elephants they are converting mine	1013	1	zekleinhammer
1080	PLan? ok i guess i go afk for the first 5min to make the game even	1014	1	carloscnsz
1088	Nili i didn't troll you that heavily but i let that for TGs later on the week	1022	1	carloscnsz
1089	This guy is an idiot	1023	1	carloscnsz
1091	Look at him. Asking for practice and shit	1025	1	carloscnsz
1099	I have a better game! Found where is daniboy on the map!	1033	1	carloscnsz
1106	We lost enough units, we can go back now	1040	1	carloscnsz
1114	He just wants to quickwall the whole map	1048	1	carloscnsz
1151	Tati man! don't disrespect me! Is not like i lost to Yo 4-0!	1053	1	carloscnsz
1156	you scared my deer away with that sub!	1058	1	zekleinhammer
1158	is it time to do this? patrol and enjoy!	1060	1	hyunaop
1159	I'll go to the hill... And micro... And die!	1061	1	carloscnsz
1161	ohh i took hera spot.. AMAZING!!!	1063	1	carloscnsz
1169	I'm not drunk i'm just stupid	1071	1	carloscnsz
1174	I wish my dad was doing drugs... Also kids don't do drugs	1076	1	carloscnsz
1182	any other thing i can do for you, my losing team mate?	1084	1	carloscnsz
1190	For the new people it will look like i'm losing to the enemy team man, but no.. Is only tatoh's fault	1092	1	carloscnsz
1198	(DauT sniping a dock vill gathering shorefish) "I was just jealous that you remembered the new balance changes and I did not"	1100	1	hyunaop
1199	I know this game! I do this for a living	1101	1	deagle2511
1200	"I can't stop watching Nili's stream chat. Imagine if his hair never grows back. That was all he had."	1102	1	byelo
1210	(daut to classicpro) "I have an ally in this game man, and it's a good one"	1112	1	hyunaop
1214	Once you share the bed with a guy, he is your's forever	1116	1	carloscnsz
1216	Always cheat! Is not cheating if they don't catch you	1118	1	carloscnsz
342	fuck me if i know	342	1	trollabot
655	This is DE and in DE i'm a micro god	620	1	carloscnsz
689	"Today is my sloppy day."	654	1	byelo
703	"You can hit this one" - Daut talking to towers	668	1	carloscnsz
709	what's a good counter ballista elephants? everything, even villager kills those	674	1	carloscnsz
715	oh no! i need to sling you fast before they resign	680	1	carloscnsz
757	Tower won't live a happy life	722	1	carloscnsz
763	Hello Mr. Tower!	728	1	carloscnsz
775	he is trying to up without loom... You have to punish that shit	740	1	carloscnsz
790	I think corona virus started from the NAC	755	1	zuviss
806	the meaning of life is to die and become a zombie	771	1	carloscnsz
848	how can i be a top player and make all those stupid calls all the time	782	1	carloscnsz
860	i have to go destroy him! like a man!	794	1	carloscnsz
867	Oh no... my elo is so low that i'm playing slam	801	1	carloscnsz
884	Ohh You are a good galley! you are a good galley!!! dautLove	818	1	carloscnsz
892	I'm your hero? You have low standards man	826	1	carloscnsz
901	yeah yeah.. baby crying yeah... I'm crying as well	835	1	carloscnsz
925	why i'm still playing? ok let's wait for imp and resing	859	1	carloscnsz
934	when you're unique unit is dying to the villagers. that's all I have to say about the unique unit.	868	1	hyunaop
947	hera! were you scared? imagine if i wasn't stupid!	881	1	carloscnsz
959	If I played checkers I would have a daut Castle	893	1	zekleinhammer
964	I am not freaking uber to you man	898	1	zekleinhammer
985	He is microing really good! or maybe is me being stupid	919	1	carloscnsz
991	Oh fuck it! Let's go tower rush and wall at home like a little pleb	925	1	carloscnsz
997	I remember a game against viper. I fully wall and he send all his chat to my stream to spam "walls walls no balls" but the point is that i won that game	931	1	carloscnsz
998	We are playing no balls this game	932	1	carloscnsz
1008	At least we have Nili in our team so Excuse is ready	942	1	carloscnsz
1017	"Is that your castle? Do I need to go there now, haha?" -DauT to dautBaldy	951	1	batbeetch
1030	Don't think Nili! that's your first mistake	964	1	carloscnsz
1036	Let's play Adventure man! imagine Youtube title! Secret team goes on adventure to China!!!	970	1	carloscnsz
1040	The Vivi got spider sense for my daut castles	974	1	carloscnsz
1045	SPANISH??!!! Hahahaha... they don't know shit!	979	1	hyunaop
1049	You should go for elephant archers qhen you already won the game and you want to troll	983	1	carloscnsz
1063	"Who doesn't like rainbows, come on?" DauT to Viper while doing a 5 TC boom.	997	1	byelo
1067	ohh he doesn't have loom! .. I wish i had fletching	1001	1	carloscnsz
1081	can I quick wall? I fight with villagers I don’t need quickwall	1015	1	zekleinhammer
1090	if i was admin i will give AW myself here	1024	1	carloscnsz
1092	i don't really need help but i like when you are around	1026	1	carloscnsz
1100	I'm the better team game palyer but also the better person	1034	1	carloscnsz
1107	Hey MBL do you want to buy plans from RB? i have some to sell you	1041	1	carloscnsz
1115	i don't even know where to start with the disrespecting here	1049	1	carloscnsz
1152	Man! He is playing like viper man! He should know how did that end for viper!	1054	1	carloscnsz
1157	When you hear that sheep sound you know that your scout did a bad thing	1059	1	carloscnsz
1160	You are not on stone young man?	1062	1	carloscnsz
1162	You know you hit low bottom when even daniboy is making fun of you viper	1064	1	carloscnsz
1170	I can afford to be a bit aggressive, and a bit aggressive means right into his face	1072	1	carloscnsz
1175	I prefer to work less than have more money	1077	1	carloscnsz
1183	We are playing alone and i have 200vills and you have 2 feitorias	1085	1	carloscnsz
1191	How does it feels to play a team game with a team?	1093	1	carloscnsz
1201	how do you fast imp without tc?	1103	1	zekleinhammer
1211	"look at my score, look at my ally score, look at my opponent score. Why are we looking at score?" dautKotd	1113	1	hyunaop
1219	man shift clicking is made for old people	1121	1	hyunaop
1234	aww and you wanted to raid me now? Should have messaged me earlier!	1136	1	hyunaop
1241	Overchoping will happen. I know it, you guys know it... Hera knows it	1143	1	carloscnsz
1248	I didn't kill a single one. Not my idea of having fun.	1150	1	fatrhyme
1252	It's all jokes untill you get defeated by the elephants	1154	1	carloscnsz
1257	I couldn't find the sheep then chain disaster after that	1159	1	carloscnsz
1263	Now let's not play like Fire	1165	1	carloscnsz
1273	Ohh this is a cheap way to win the way.. I'll take it!!! i'm a cheap fuck!	1175	1	carloscnsz
1277	Let's play capoch style. Like you have nothing but you have economy for everything	1179	1	carloscnsz
1285	does the loss against aM have any consequences? yes I am divorcing because of that Kappa	1187	1	zekleinhammer
1288	"You wanna get walled? You wanna get walled? Don't fuck with me."	1190	1	artofthetroll
1289	Let's go turtle boys!	1191	1	hyunaop
1293	"Make your own villagers!" DauT responding to MbL converting his trush vills.	1195	1	byelo
1296	I was counting on the prize pool to feed my children, man	1198	1	zekleinhammer
397	i'm that type of team mate	397	1	trollabot
690	Hera will soon have more games than rating	655	1	carloscnsz
716	"It worked, man, it worked!" Daut after making Tatoh and Yo resign with 105	681	1	byelo
764	Did i lose my demo to something stupid ?	729	1	carloscnsz
776	Nicov, you fucker, you jebaited me, why did I listen to you	741	1	zekleinhammer
791	"Is not that i'm tryharding, is because i have the worst flank" daut pocket nili flank	756	1	carloscnsz
807	he cannot work when he is dead, that's how this game works	772	1	carloscnsz
849	MBL and me will hunt fire down, and smurf him	783	1	carloscnsz
861	Yeah i think my map is not the best... But i believe and he doesn't !	795	1	carloscnsz
868	freaking slam! i want to scout him but if he trap my scout i will look like an idiot... i won't scout him!	802	1	carloscnsz
885	I just want axemans cuz they are unique unit	819	1	carloscnsz
893	I don't know if he is smart or stupid	827	1	carloscnsz
902	Debbies is streaming a lot... I guess he is jobless as well	836	1	carloscnsz
935	When shit starts, i have no time!	869	1	carloscnsz
948	I don't know why his economy was that shit. guess I am just much better!	882	1	hyunaop
965	time to kill his villagers for sport	899	1	zekleinhammer
986	come oooooooon!! don't take my camel away from me	920	1	carloscnsz
992	i'm planish to get a tattoo of a daut castle in my shoulder... And never finish that tattoo	926	1	carloscnsz
999	I'm so good at reading games and getting lucky	933	1	carloscnsz
1009	"Good boy, he did not want to take the shot, so now we lame his boar. He needs to pay the price for being nice!"	943	1	byelo
1018	Nili i don't want to put pressure on you but I'm casting and you are my main target	952	1	carloscnsz
1031	look at me quickwalling like hera. When i grew up i will be like hera	965	1	carloscnsz
1050	Everytime i promised something i failed... So i won't promise anything	984	1	carloscnsz
1068	Demo goes Boom! man	1002	1	zekleinhammer
1082	OHH!!! This is failing tatoh!!! He got more elephants than me... we need new plan tatoh	1016	1	carloscnsz
1093	"Am I going fast imp?? AM I GOING FAST IMP??? Pfff no.. i'm playing proper game" (daut being sarcastic going fast ip skirms in TG)	1027	1	carloscnsz
1101	You have only 4 mangudais man!!! And dany only has 4 camels man... Let me make 4 villagers and then we resign	1035	1	carloscnsz
1108	i go die	1042	1	carloscnsz
1116	i already met art of troll and reaper irl. So i kinda don't want to know more viewers	1050	1	carloscnsz
1153	Im to mean to viper? Well someone has to be	1055	1	carloscnsz
1163	if only you could use market to buy houses	1065	1	zekleinhammer
1171	Suddenly sending all those forwards vills makes little sense	1073	1	carloscnsz
1176	I Love F1re man. We even shared bed together... Wasn't a pleasant experience	1078	1	carloscnsz
1184	"Usually i don't fail those things... They just happend" Daut talking about daut castles	1086	1	carloscnsz
1192	"I will go to check if there is food at the kitchen" * 1min later * "I just order pizza"	1094	1	carloscnsz
1202	not impossible but not possible either	1104	1	zekleinhammer
1212	"I do something... I do nothing"	1114	1	hyunaop
1220	I am the best team player you ever saw	1122	1	carloscnsz
1224	"You were not born when I was microing!" while trying to wall Dobbs in on Arabia	1126	1	byelo
1227	The microphone name is like letters and numbers	1129	1	carloscnsz
1228	Moo! Moo! Thats why I got new microphone	1130	1	hyunaop
1235	there is no more cursing here, hyuna, so shut the fuck up	1137	1	zekleinhammer
1236	There is no more cursing here Hyuna so shut the fuck out!	1138	1	carloscnsz
1242	well.. you didn't get a daut castle so unsub Kappa	1144	1	carloscnsz
1249	Don't get it wrong guys! I'm dead, completely dead... Just want to be annoying before resign	1151	1	carloscnsz
1253	dropping a Siege workshop will be a smart move... But dropping a castle would be a cool move!!!	1155	1	carloscnsz
1258	this is the biggest bullshit in the game!	1160	1	zekleinhammer
1259	I'm not trying to pretend to be a nice guy	1161	1	carloscnsz
1264	"I play low population or purpose, because with more units it's harder avoid the shots"	1166	1	byelo
1265	I am not allowed to speak if we played or not. We did not play it	1167	1	zekleinhammer
1268	green dot is good dot	1170	1	zekleinhammer
1269	I see the green dot! green dot is good dot	1171	1	carloscnsz
1270	This is the perfect place for you to ask something serious	1172	1	carloscnsz
1274	There are freaking tits all over the map man! i can't focus!!!	1176	1	carloscnsz
1278	Score says that it's over... But fuck the score man!	1180	1	carloscnsz
1279	I need to order coffe from the wooman	1181	1	carloscnsz
1280	I can't read the chat now but i bet you guys are all like "Nooo!! make hussars and you win!! Noo!!!" ... That's not the point! i have to win with jannisary	1182	1	carloscnsz
1281	look at them Xing, I will be part of the team I X as well (DauT Xes random spot)	1183	1	zekleinhammer
1286	I was fuck enough before this	1188	1	carloscnsz
1290	"Look at him trying to micro ships like that! He is cute"	1192	1	hyunaop
1294	I don't mind playing boom war	1196	1	carloscnsz
1298	How do i communicate when you don't communicate	1200	1	carloscnsz
1300	How there is scout inside when scouts are outside	1202	1	carloscnsz
466	I like lucky	466	1	trollabot
691	I'm good at being evil	656	1	carloscnsz
717	hopefully he stopped queueing villagers	682	1	carloscnsz
765	haha hera snipe man. without even looking.	730	1	hyunaop
777	You move, I kill!	742	1	carloscnsz
792	You are still an idiot	757	1	carloscnsz
808	I have the hill i have all the shit i need	773	1	carloscnsz
850	better be walled cuz revenge is coming	784	1	carloscnsz
869	Vanja is like fourteen daddy fourteen!	803	1	carloscnsz
894	Seems like it pays off to worke more	828	1	carloscnsz
936	not now! I have my own problems man	870	1	zekleinhammer
966	I work for microsoft now because we found a bug	900	1	hyunaop
987	Those flower fuckers are holding	921	1	carloscnsz
993	vaaat 11 his army melted! 11 oh boy oh boy we have an epic game	927	1	carloscnsz
1000	I will show him barracks and market so he has no clue, because I have absolutely no clue what I am doing either	934	1	zekleinhammer
1001	i will show him market and barrack so he has no clue of what i'm doing... Cuz not even I know what i'm doing	935	1	carloscnsz
1010	"What the fuck am I doing with those walls? I have no idea what my map looks like!"	944	1	byelo
1011	They way i wall man.. disgusting... But i'm a disgusting human being so let's keep walling	945	1	carloscnsz
1019	Oh nili nili nili... You can't produce for 2 range with 20 idle dead villagers	953	1	carloscnsz
1032	Do you think? or do you believe ?	966	1	carloscnsz
1069	"I will beat him with his weapons!" Daut about to go monks against dracont on arena	1003	1	carloscnsz
1083	"I hope there is no water" daut luring deers instead of exploring the map in megarandom	1017	1	carloscnsz
1094	you spit on those militia and they die	1028	1	zekleinhammer
1102	i didn't noticed he was castle age cuz i was making fun of you	1036	1	carloscnsz
1109	He is making the castle in my castle man	1043	1	carloscnsz
1117	I destroy him with weaker civ man! i show class	1051	1	carloscnsz
1154	oh look look look!!! Evacuation party!	1056	1	carloscnsz
1164	OH FUCK!!! Those are tatar xbow on hill man!! they are like mameluks man! they are melting my knights!	1066	1	carloscnsz
1172	Can i magically afford eagles? ... Seems not.. What magic unit can i afford?	1074	1	carloscnsz
1177	Glokken I notice you every single time... I just ignore you	1079	1	carloscnsz
1185	Freaking tati man, he is at restaurant while we are playing	1087	1	carloscnsz
1193	Let's destroy my wonderful team mates	1095	1	carloscnsz
1203	your stick is so big	1105	1	zekleinhammer
1221	I'm too fast man... Too fast, too stupid	1123	1	carloscnsz
1225	"This one HP villager is a hero, it's all we nee..." *castle kills Daut's army*	1127	1	byelo
1229	I feel like the balance team are a bunch of trolls	1131	1	hyunaop
1237	How do people talk when they don't curse?	1139	1	carloscnsz
1243	Guard Tower? Skirms? How do you sleep at night.	1145	1	hyunaop
1244	Guard towers? Skirms??? How do you sleep at nights ?	1146	1	carloscnsz
1254	"I would love a castle there, I get the thing I love the most"	1156	1	hyunaop
1260	That was the Jbait of the Jbaits	1162	1	carloscnsz
1266	I'm going for the elephant fuckers	1168	1	carloscnsz
1271	Don't you hate when you are thinking a lot about enemy strategy and they are just doing nothin	1173	1	carloscnsz
1275	i see your logic and it's a false logic man! elephat archers are terrible!	1177	1	carloscnsz
1282	Yeah pity subs.... WE TAKE THOSE!!!	1184	1	carloscnsz
1287	I'm pretending to be a good team mate	1189	1	carloscnsz
1291	NOO NOO!! DON'T GG BEFORE I FINISHED MY PLAN!!!!	1193	1	carloscnsz
1299	My APM is actually good for typing	1201	1	carloscnsz
1301	I'm not your debbie man!	1203	1	carloscnsz
1302	Why i'm trying to save you if you don't give a shit?	1204	1	carloscnsz
1303	I will smoke sisha and watch	1205	1	carloscnsz
1304	*daut checking punishment coins requests* "asssjam request, we will ignore that one"	1206	1	carloscnsz
1305	He wanted to wall me in because I’m slow and old	1207	1	batbeetch
1306	Don't call my rams fat!	1208	1	carloscnsz
1307	I have trash units but also i have a trash ally	1209	1	carloscnsz
1308	You weren't mine anyway... But I still want you!	1210	1	carloscnsz
1309	there are 8 players I don’t read all this. If I wanted to read I would have finished high school	1211	1	zekleinhammer
1310	I miss being higher on the ladder	1212	1	carloscnsz
476	No i don't know what a daut castle is. Never made one of those. But sounds like an amazing thing	476	1	trollabot
477	I will put baby on webcam after i beat viper in memb's tournament	477	1	trollabot
478	you are full of advices viper.. while booming at home	478	1	trollabot
479	"what the bell man? what the fricking bell"	479	1	trollabot
480	if you have your own private island and cant find your sheep, then something is wrong with you.	480	1	trollabot
481	man, it's not even my fault making daut mining camps, game works against me	481	1	trollabot
529	"The oldest trick in the book of how to beat arina clowns" DauT while trushing terror on arena	529	1	trollabot
1311	"I give you the chance! I give you the chance!!" Daut fails the split "He took the chance man"	1213	1	carloscnsz
1312	nothing is ever my fault	1214	1	zekleinhammer
482	tati man, let's play crazy settings so mbl is forced to cast it.	482	1	trollabot
483	My daughter is only learning how to eat and poop man. but she will learn aoe soon	483	1	trollabot
484	2k players don't wall man! they believe!	484	1	trollabot
485	Yes viper... I'm comparing you to jordan... I'm mean both live in germany now	485	1	trollabot
486	I sent viper back to norway to collect some back from his skill	486	1	trollabot
487	"My favourite food? Snake meat, haha!"	487	1	trollabot
488	What if i surprise him going scouts... Sounds like a suicide to me	488	1	trollabot
489	I can outmicro 10year olds	489	1	trollabot
490	he is getting to that rate when u play mbl daily	490	1	trollabot
491	What unit composition should i go for... I know! TCs!!!	491	1	trollabot
492	People always find the way to fuck good things	492	1	trollabot
493	”I Will never get this deer, this deer is asshole”	493	1	trollabot
494	"little he knows that I don't micro. I just spam"	494	1	trollabot
495	"Time for my famous Man-at-arms micro!"	495	1	trollabot
496	Two farm economy is not enough for villager production, just saying	496	1	trollabot
497	do these games ever end? asking for a friend - daut playing BF	497	1	trollabot
498	Such a defensive trapping fucker man!	498	1	trollabot
499	Actually let's give viper host. If I'm host game will never start	499	1	trollabot
500	MABAO MAN!! MABAO	500	1	trollabot
501	We are all turning into MBLs	501	1	trollabot
502	and BAM! i defeated them	502	1	trollabot
503	Mabao? Mabao my ass man! i go BAMbao man! bam bam! First BoomBao then BAMmao	503	1	trollabot
504	When does my playstyle makes sense?	504	1	trollabot
505	When you get berbers you must play like MBL	505	1	trollabot
506	untouched, my ass!	506	1	trollabot
507	you give me host, i put an add then end the stream	507	1	trollabot
508	oh he picked his color. He is not fucking around	508	1	trollabot
509	I prefer streaming that been babysitting all night	509	1	trollabot
510	My sleep schedule is megarandom again	510	1	trollabot
511	asssjam question: "daut when u get a haircut what do you ask for?" I show them a picture of slam and they do their best	511	1	trollabot
512	Yes! you got scout! you are amazing! Fuck off now!!	512	1	trollabot
513	I think i will buy myself a one little castle...maybe a one little daut castle	513	1	trollabot
514	Let's ban that forest pond thing	514	1	trollabot
515	We can go for goldrush is kinda regicide	515	1	trollabot
516	I hate this man i don't have regicide and i don't have mayans	516	1	trollabot
517	Oh we can give them koreans and when they try to trush we BAM it	517	1	trollabot
518	Is not bam is is BAM IT! like destroy them	518	1	trollabot
519	"You may experience some noise in the background" DauT when his kids start to cry	519	1	trollabot
520	Oh man! dog! baby! BacT color!! what else do i have to deal with	520	1	trollabot
521	"I ban when i wanna ban!" daut to the admin of two pools tournament	521	1	trollabot
522	"TC is overrated" daut to slam when slam was losing his only tc to push and daut raiding instead of help him	522	1	trollabot
523	"This is going to be the best comeback you guys ever saw" daut before dropping a random daut castle dautCastle	523	1	trollabot
524	He is trying to honor me with daut castles	524	1	trollabot
525	"Let's fast forward dark age because i don't want to feel depressed watching my viewers" Daut while casting DCL	525	1	trollabot
526	ModeratorVerifiedNightbot: i will lure deers first. if you guys want my scout wait for 10mins	526	1	trollabot
527	"they didnt know i lost 4vills in dark age" daut during interview after winning ecl	527	1	trollabot
528	i will prismata his ass	528	1	trollabot
530	Respect my tower rush man! don't counter tower	530	1	trollabot
531	Vaaats wrong with this game today man! everybody is laming something... and that something is me	531	1	trollabot
532	At my prime? my prime never ends man	532	1	trollabot
533	ok ok i know the map, i have the plan, you are dead	533	1	trollabot
534	What are you doing? are you booming like a chicken???	534	1	trollabot
535	It's all about lying man. It gets easier every time	535	1	trollabot
536	"I do nothing and I have time for nothing"	536	1	trollabot
537	Delete yourself	537	1	trollabot
538	who is this player, the fishing ship is my unit, not archer, not mangonel, i play only with fishing ship from now on https://clips.twitch.tv/PolishedJazzyYakCharlieBitMe	538	1	trollabot
539	Me and JorDan best of 21. loser joins aM! LUL	539	1	trollabot
540	"I believe in you light fuckers" Daut sending light cavs to kill a group of xbos on his base	540	1	trollabot
541	(daut doing dm vs tati) oh, sick I got goths. what civ is tati? oh fuck. it's mirror.	541	1	trollabot
542	of course its towerfest man. it's like Oktoberfest but with towers.	542	1	trollabot
543	5 is the magic number man. if you have 5 of something man you can fight.	543	1	trollabot
544	hera you're welcome in my tc man. this is not aM!	544	1	trollabot
545	I actually have economy, problem is I have no ally!	545	1	trollabot
546	I don't click man	546	1	trollabot
547	about quickwalling: I wouldn't do it even if I could	547	1	trollabot
548	so that's what a daut castle looks like, kind of good when it doesn't happen to me!	548	1	trollabot
549	"No no You do T90 and i do Memb" Daut casting with Hera	549	1	trollabot
550	Do you know halberdiers used to no exist	550	1	trollabot
551	only thing worse than scheduling with fire is been in same room with fire	551	1	trollabot
552	Good luck with that man! that's your guy man! i cleaned my guy man, i'm chilling and booming now	552	1	trollabot
553	"what did we learn last game guys?"	553	1	trollabot
554	Feed my kids with blood of my enemies? i'm not raising mosquitoes here man	554	1	trollabot
555	I have style man	555	1	trollabot
556	I don't need to make tower! i'm beast	556	1	trollabot
557	guard it man! guard it with your life!	557	1	trollabot
558	"How did guy entered to my game? he is not supposed to be in my game" Daut being raided by the other pocket	558	1	trollabot
559	Probably boars are hosting their own talk show to make fun of us	559	1	trollabot
560	"why is he talking like Yoda man, berbers is he.. OH FUCK WHAT?" Daut talking random stuff when suddenly boar kill his villager	560	1	trollabot
561	Maybe i should start respecting people	561	1	trollabot
563	this is disaster man	562	1	trollabot
564	Hera is young slam, not invited	563	1	trollabot
565	"there are better ways to balance than to just remove" also daut, "remove bbts"	564	1	trollabot
566	let's prepare some shit for today for tomorrow	565	1	trollabot
567	let's go genitours man, oh fuck he's going knights. he is going everything I don't want him to go!	566	1	trollabot
568	I believe in you boys, let's fight! why aren't you fighting man? we are gonna have a talk after this game!	567	1	trollabot
569	making jordan die on tg is always something special	568	1	trollabot
570	not losing a single vill to boar. that's what i call a good start	569	1	trollabot
571	If i only had ballista elephants	570	1	trollabot
572	fuck it man, I'm not booming. 5 relics is my boom!	571	1	trollabot
573	5 relics is my boom	572	1	trollabot
574	I go unit that doesn't require micro	573	1	trollabot
575	I hear baby crying but I dont think it's my baby!	574	1	trollabot
577	i know my baby screams man they are louder	576	1	trollabot
579	I like sasha grey cuz the name reminds me the sisha man! sasha, sisha	578	1	trollabot
580	I think Hyuna is the biggest pervert in my chat	579	1	trollabot
613	"First he joins aM, but if he is a normal person, he will join Secret" Daut talking of Mini-Daut.	580	1	trollabot
614	nomad is the best fricking game in the map	581	1	trollabot
615	"I can't lose, I micro!"	582	1	trollabot
616	"Two things you don't do: You don't lame Finns."	583	1	trollabot
617	do i need to mangonel his ass?	584	1	trollabot
630	Maybe if you were a dog i would kick you out Viper	597	1	carloscnsz
692	I will be sleeping around	657	1	byelo
718	fishing’s ships cannot go in tower!	683	1	zekleinhammer
719	idk where is my scout. must be scouting	684	1	carloscnsz
766	BOOM! Nothing sexier than that, not even your camels	731	1	zekleinhammer
778	Mbl? did you really fuck yourself at the gym?	743	1	carloscnsz
793	I don't want to share that information	758	1	carloscnsz
851	ok ok for that now you deserve another castle in your face	785	1	carloscnsz
870	My gameplan is he not looking... Is a really good gameplan	804	1	carloscnsz
937	i'm full fighting against my own dark age and he comes and do this....	871	1	carloscnsz
967	i hope 2TC start maps never become meta	901	1	carloscnsz
1002	Now that my friend mangonel is here! not more fucking around!!	936	1	carloscnsz
1012	ok fire, We as Team Secret can make a sacrifice for you and give you nili for BoA2	946	1	carloscnsz
1020	Maybe i go to hard on nili. But then again now he will invite me to NAC4	954	1	carloscnsz
1070	Who needs food.. I got imp	1004	1	carloscnsz
1084	And people will play for 2 weeks just to give you feedback nili... yeah .. sure	1018	1	carloscnsz
1095	You didn't have to help me, you wanted to help me	1029	1	carloscnsz
1103	"if we both are pockets viper, and daniel is flank, we win!" daut after losing a few 3v3s with daniel pocket	1037	1	carloscnsz
1110	"if only i had an ally... or two..." Daut playing 3v3 with aM	1044	1	carloscnsz
1118	i didn't made a single farm and that's usually a sign of bad economy	1052	1	carloscnsz
1155	You can not block without fishing ships	1057	1	carloscnsz
1165	Patrol, TC, farm	1067	1	carloscnsz
1173	I wish I could shift click my kids to get them to do what I tell them to do	1075	1	zekleinhammer
1178	And now the battle of bad lumbercamps and shity woodlines	1080	1	carloscnsz
1186	Those ar cap freaking rams man!!!!!!!!	1088	1	carloscnsz
1194	what? That's one nothing man, you are fine!	1096	1	carloscnsz
1204	I should be forbidden to micro	1106	1	hyunaop
1205	never allow me to micro again	1107	1	hyunaop
1222	I will get her, I alway get the woman	1124	1	carloscnsz
1226	"My best hope is him misclicking. Misclick something, man!"	1128	1	byelo
1267	Hah! We stomp on that camel	1169	1	zekleinhammer
1313	Those guys are like trebs that always miss	1215	1	carloscnsz
1314	I am a boomer man! I am a boomer in my heart! Once boomer, forever boomer	1216	1	carloscnsz
1315	He does nothing man hahaha... Kill him!!!	1217	1	carloscnsz
1316	oh there is a fake daut account on twitter? I hope he is at least posting when i go live	1218	1	carloscnsz
1317	go away! Komodo beast	1219	1	zekleinhammer
1318	I have zero food income right now, usually not good	1220	1	zekleinhammer
1319	my population is out... and we don't want daut to be out	1221	1	zekleinhammer
1320	deploy there you fucks!	1222	1	zekleinhammer
1321	I am a simple person. I run over the rocket guy, and I laugh	1223	1	zekleinhammer
1322	Daut showing his daughter on stream* "Well time for her to leave and for sisha to enter"	1224	1	carloscnsz
1323	let's all pretend we hear what carlos is saying	1225	1	zekleinhammer
1324	at the start we have 3 villagers and that was his peak for him. the best he did in the game	1226	1	carloscnsz
1325	"we all had 3 vils at the start, and that was his highlight. it only wend donwhill form there"	1227	1	carloscnsz
1326	zuviss not a single fuck given 1111111111	1228	1	carloscnsz
1327	I'm not late. i have 3minutes	1229	1	carloscnsz
1328	zekleinhammer has a better economy than me	1230	1	zekleinhammer
1329	I feel dead all over again, it's like nobody cares about me	1231	1	hyunaop
1330	"Roxy is more manly than you" Daut to Hera	1232	1	carloscnsz
1331	I lost all my pride when i put my family on stream.. that ship is sailed man!	1233	1	carloscnsz
1332	You got outboomed bitch!	1234	1	carloscnsz
1333	"I'm a simple guy: I see Slavs, I make hourses, man!"	1235	1	carloscnsz
1334	Sheep are okay, boars are friendly but deers are assholes	1236	1	carloscnsz
1335	Nothing is ban when i'm losing	1237	1	carloscnsz
1336	We go casual	1238	1	carloscnsz
1337	if I lose to dracont i will lose my life	1239	1	carloscnsz
1338	I'm king of nothing man	1240	1	carloscnsz
1339	draft is too complicated even for the programers that make it	1241	1	carloscnsz
1340	Full archers agains skirms... what can go wrong? .... EXACTLY!!	1242	1	carloscnsz
1373	hyuna you know stuff, how can you be such a pleb in the game!	1243	1	zekleinhammer
1374	This is fucking Asterix & Obelix	1244	1	carloscnsz
1375	first it was the Ha Ha! and now it's not the Ha Ha! Now it's a Hehe!	1245	1	hyunaop
1376	I hate reading	1246	1	zekleinhammer
1377	I go for the 5 tc boom, he goes for the 5 tc boom, let the better boomer win	1247	1	zekleinhammer
1378	This is it! He goes for 5tc boom, I go for 5tc boom! and let the better boomer win	1248	1	carloscnsz
1379	"not sure if that's a good strategy"Daut when chat asked him to go persian douche on arena	1249	1	carloscnsz
1380	the problem with skirms is you need a lot of them to be efficient, and they're still not efficient	1250	1	zekleinhammer
1381	"say what you want about my micro but that was not my fault! (DauT when he tried to quickwall on arena with DE lagspike)	1251	1	hyunaop
1382	"This is coaching stream and i'm getting schooled''	1252	1	fatrhyme
1383	I'm still recovering from that shot in my face	1253	1	carloscnsz
1384	"When i play sub war vs tatoh i get deagle man! but now that i want to do something with my life i get freaking professionals!" daut playing 1v3 with viewers	1254	1	carloscnsz
1385	"It's not cheating if it works"	1255	1	fatrhyme
1386	"This is helpful man" Daut playing art of war	1256	1	carloscnsz
1387	I never listen to what I am told I do an easier way	1257	1	zekleinhammer
1388	Look at tatoh man! Alway looking for an unfair lead!	1258	1	carloscnsz
1389	we need art of war for pushing deers as well	1259	1	zekleinhammer
1390	Too many people in my head	1260	1	carloscnsz
1391	everybody matters, thank you for exisiting!	1261	1	hyunaop
1392	I wanted to do horse collar but didn't have the res so fuck it! i went towers and castles	1262	1	carloscnsz
1393	the score says that he is pushing deers or maybe i'm just much better player	1263	1	carloscnsz
1394	No quickwalls but quick deaths	1264	1	carloscnsz
1395	"No quickwalls but quick deaths!"	1265	1	fatrhyme
1396	I think there are 2 cows somewhere on the map laughing at me	1266	1	carloscnsz
1397	"somewhere in the map there is two cows laughing at me right now"	1267	1	hyunaop
1398	"Look at me. I'm liereyy, I'm 12 years old and I micro"	1268	1	hyunaop
1399	I am housed man, kill something!	1269	1	zekleinhammer
1400	I'm moving out now, fuck this!	1270	1	carloscnsz
1401	The range of those things, they are hitting me from another game!	1271	1	batbeetch
1402	Cya slamm. Ggs. Im going to fucking sleep	1272	1	carloscnsz
1403	"I used to have trebs, I used to have everything!"	1273	1	byelo
1404	my chat makes perfect sense. Always!	1274	1	zekleinhammer
1405	Everything went wrong for you. vills dying, archers attacking walls, we take those	1275	1	carloscnsz
1406	This civ is good against everything. Or maybe i am good against everything	1276	1	carloscnsz
1407	oh cmon, I don't have loom, danny boy you asshole	1277	1	zekleinhammer
1408	I will be Viper now	1278	1	carloscnsz
1409	he stole my boar I steal his units	1279	1	zekleinhammer
1410	"He is so wall in and so scared!"	1280	1	carloscnsz
1411	it looks look i'm trolling, but i'm not, i'm just dying	1281	1	zekleinhammer
1412	outposts are so nice! you make them, then you know you are dead	1282	1	deagle2511
1413	he obviously has the slight lead here, which is quite big	1283	1	zekleinhammer
1414	what do you do when you're housed as Chinese? I guess build a house.	1284	1	hyunaop
1415	look at Hyuna man. Getting rejected by imaginary girlfriend. At least you have a realistic imagination.	1285	1	hyunaop
1416	Now i don't have hole when i need a hole!	1286	1	carloscnsz
1417	Daut castles in every single game man! If i play tetris I will get a daut castle!	1287	1	carloscnsz
1418	don't treat me like jordan you assholes	1288	1	zekleinhammer
1419	4v4 DM is actually fun. you don't know what's happening! .. I guess RM is fun for nili too	1289	1	carloscnsz
1420	This makes no sense man! i make units he make units but my units die!	1290	1	carloscnsz
1421	My aim here is worst than in fortnite	1291	1	carloscnsz
1422	inquisition my ass!	1292	1	zekleinhammer
1423	I would fuse with liereyy man. I would be younger. I can micro. If I fuse with F1Re I'll be like Fat Goku.	1293	1	hyunaop
1424	Okay we both die! ... But i die more	1294	1	carloscnsz
1425	I'm mastering the new technique here... i called being mbl	1295	1	carloscnsz
1426	I am aggressive guy... I am STUPID guy!	1296	1	zekleinhammer
1427	he knows he needs to boom, and he will freaking boom. I respect that	1297	1	zekleinhammer
1428	I'm like Jon Snow now.... I know nothing	1298	1	carloscnsz
1429	Just fight and lose so we continue playing!	1299	1	carloscnsz
1430	I'm just lazy... But i will fix that... When i get less lazy	1300	1	carloscnsz
1431	Dark age is really stressful for me	1301	1	carloscnsz
1432	I need castle age ram i mean imperial castle age	1302	1	carloscnsz
1433	do you think im shitting stone here or what?	1303	1	artofthetroll
1434	I was repairing with my life, and I dont have life anymore.	1304	1	artofthetroll
1435	"are you dead?" Daut cuz tatoh was talking nonsense	1305	1	carloscnsz
1436	the chat knows man... everybody is trolling me	1306	1	carloscnsz
1437	Spliting like a beast, dying like a hero	1307	1	carloscnsz
1438	Where i am building the castle? Where??? where i shouldn't build a castle! there is where i'm building a castle	1308	1	carloscnsz
1439	"Well Thanks you chrazini! for the little fish on new migration!" (chrazini has nothing to do with the balance of standard maps)	1309	1	carloscnsz
1440	Trading points is not cheating if it is to remove mbl from rank1	1310	1	carloscnsz
1441	construyer the tower in your face	1311	1	zekleinhammer
1442	He can switch into monks now... Or maybe his favourite unit: The walls	1312	1	carloscnsz
1443	Hera: what are you goals? DauT: five more years of doing nothing and then retire	1313	1	zekleinhammer
1444	Drafts are still happening cuz is 1h of free content for the casters	1314	1	carloscnsz
1445	wanna be my seedie buddy?	1315	1	zekleinhammer
1446	So MBL is like tic tac tic tac	1316	1	carloscnsz
1447	Nili is not improving man, you coaching was useless! i didn't see any type of improvement	1317	1	carloscnsz
1448	daut to hera: your coaching is oh you lose three villagers thats bad	1318	1	zekleinhammer
1449	You can do 3 tc or 1 tc or delete your tc. i don't mind	1319	1	carloscnsz
1450	Now that everybody is in post imperial age you are acting like the cool guy? the cool train is gone	1320	1	carloscnsz
1451	I delete this, i delete TC, I delete both of them	1321	1	carloscnsz
1452	"Well, another stupid game again" daut after winning with civ advantage	1322	1	carloscnsz
1453	If I see Jordan above me I will go back to hospital	1323	1	zekleinhammer
1454	if I see JorDan above me. I'll go back to hospital	1324	1	hyunaop
1455	I took one game from the hera, viper couldn't even do that and he plays everyday	1325	1	zekleinhammer
1456	I am happy I am not dead as well	1326	1	zekleinhammer
1457	"did the doctors ever PETTHEDAUT" ? "No. But nurses did" dautPrincess	1327	1	hyunaop
1458	thought those monks were petards	1328	1	dfear
1459	why they make sound when you are not touching them?	1329	1	zekleinhammer
1460	you would think its not possible to be that stupid	1330	1	zekleinhammer
1461	on socotra there are no nice guys	1331	1	zekleinhammer
1462	I see your economy, i don't like your economy	1332	1	carloscnsz
1463	Tatoh, thank you for corona!	1333	1	carloscnsz
1464	He got hole I just get in from the inside	1334	1	carloscnsz
1465	I can be 10 years old as well	1335	1	carloscnsz
1466	it doesn't get as fatter and lower than this	1336	1	hyunaop
1467	Man idk MBL probably even makes the sex boring	1337	1	carloscnsz
1468	What's the range of this magical tower?	1338	1	carloscnsz
1469	Yeah tower, mining camp, dominate! We have the plan	1339	1	carloscnsz
1470	I don’t need spearman...and I can’t afford it	1340	1	zekleinhammer
1471	Everything i want fails...	1341	1	carloscnsz
1472	Time to show chris what i learn from blocking!	1342	1	carloscnsz
1473	I feel that my economy will be like nili's soon enough	1343	1	carloscnsz
1474	MBL is practicing with me and I got Nicov in first round... That's a solid team mate	1344	1	carloscnsz
1475	how much is sponsor paying us? NOT ENOUGH MAN!!	1345	1	carloscnsz
1476	"I was not planning to use those anyway" - DuaT after many xbow are killed by a mangonel	1346	1	zekleinhammer
1477	There goes my pride... Hope i'm exchanging it for the win	1347	1	carloscnsz
1478	I'm running out of gold but still can deliver the castles	1348	1	carloscnsz
1479	I won't add more TCs... that would be cheating	1349	1	carloscnsz
1480	I delete but I did not shoot	1350	1	hyunaop
1481	game gives me a free deer and I fail to take it	1351	1	hyunaop
1483	He may think i will go mangudai... But he sees I'm not in stone... But still he is not the smartest player	1352	1	carloscnsz
1484	I'm keeping it real man... Losing every tournament I play... NO!! EVEN LOSING THE SCOUT NOW! NOOO!!	1353	1	carloscnsz
1485	tala this one, man!	1354	1	zekleinhammer
1486	I got sponsorship but I refuse it	1355	1	carloscnsz
1487	She knows only what I tell her	1356	1	carloscnsz
1488	It would be good if you have won the RB viper and have the money to put in that wallet	1357	1	carloscnsz
1489	I signed up the contract but didn't read it, so I just hope I don't get scammed	1358	1	carloscnsz
1490	You have the extra armor on your women	1359	1	carloscnsz
1491	What?! Why you move out man! You are behind you should stay home! Ohh that hurts... It's hard to play against players who doesn't understand the game man	1360	1	carloscnsz
1492	"you want to be be new MBL when you grow up ?"	1361	1	carloscnsz
1493	I did micro enough for today	1362	1	carloscnsz
1494	"I Absolutely don't care about your problems" daut playing 2v2s with viper	1363	1	carloscnsz
1495	It can be bad but It can be really bad	1364	1	carloscnsz
1496	yeah defensive castle was an option but i like it on enemys face	1365	1	carloscnsz
1497	Freaking Tati man... I have one girl at home, i don't need two	1366	1	carloscnsz
1498	That's why you wanted to be a ballista elephant when you grow up	1367	1	carloscnsz
1499	"This is like TTF Racing man" Daut playing Fall Guys	1368	1	carloscnsz
1500	I'm Like Flying	1369	1	carloscnsz
1501	The problem is that others don't understand the game	1370	1	carloscnsz
1502	Ok Jordan you queue up for age of empires. i will beat you there and this game at the same time	1371	1	carloscnsz
1503	Once you start failing it never stops	1372	1	carloscnsz
1504	call me a zero conversions DauT	1373	1	hyunaop
1505	1 for 1 is fine... That's the maximum of my micro	1374	1	carloscnsz
1506	houses are for people to live, not for being part of a wall	1375	1	zekleinhammer
1507	People is always rushing man, rushing here, rushing there... Why we can't just chill	1376	1	carloscnsz
1508	I'm back! Playing stupid and win!	1377	1	carloscnsz
1509	the plan is to die, and the game is going according to plan right now	1378	1	zekleinhammer
1510	everything that's cool is now gone. even soon I will be gone too.	1379	1	hyunaop
1511	I rather lose than don't respect myself	1380	1	carloscnsz
1512	Yeah Fire is high rank for some reason. I don't know from where he gets the points	1381	1	carloscnsz
1513	when they wait, it's never good	1382	1	zekleinhammer
1514	I cannot even manage myself	1383	1	carloscnsz
1515	Viper does a lot of things! Talks to people and ask us then we never reply	1384	1	carloscnsz
1516	If he counter attack i will hate him a lot	1385	1	carloscnsz
1518	Tower is gonna fall and my castle will rise again	1386	1	carloscnsz
1519	That's tha speed! of tha beast!!!	1387	1	carloscnsz
1520	He is not microing that well... compared to me	1388	1	carloscnsz
1521	I counter nothing with my counter unit	1389	1	zekleinhammer
1522	"I don't have loom, But i believe! I know you guys believe in me and I believe in me so let's do it!" Daut going for the forward castle in arena	1390	1	carloscnsz
1523	Axes? why the fuck not. they throw things	1391	1	carloscnsz
1524	Is so much easier when you don't fail	1392	1	carloscnsz
1525	Well you were bald at 25 years old	1393	1	carloscnsz
1526	Will I go yolo? The answer is... Hell yeah!	1394	1	zekleinhammer
1527	Against Rubenstock you can't have a proper game. He is like better hoang	1395	1	carloscnsz
1528	"HA HA HAAAA!!! I love when that doesn't happens to me" Daut accidentally killing enemy mangonel while attacking TC	1396	1	carloscnsz
1529	We need a lag tournament!	1397	1	carloscnsz
1530	ohh baby is back, they were out... I guess i will have to stream until the got to sleep	1398	1	carloscnsz
1531	And this wolf probably killed 5 of my units so far	1399	1	carloscnsz
1532	When i was number one lierey wasn't even planned	1400	1	carloscnsz
1533	I will lock up my son in a room with only water food and aoe4	1401	1	carloscnsz
1535	I hope my son calls lierey grandpa and makes fun of him like "oh grandpa grandpa, you can't even quickwall"	1402	1	carloscnsz
1536	"This is the style of a winner!"	1403	1	artofthetroll
1537	that castle will put him back to the Kindergarten	1404	1	hyunaop
1538	This castle will put him back to the kindergarden	1405	1	carloscnsz
1539	Do you guys saw Kotd map? Bad things can happen... Hopefully not to me	1406	1	carloscnsz
1540	There is space for you fat fuck!	1407	1	carloscnsz
1541	Bam! and one more! and get the fuck out!!!!!	1408	1	carloscnsz
1542	That's a double fuck for him	1409	1	carloscnsz
1543	guys shoot you fucks. you think you're liereyy or something?	1410	1	hyunaop
1544	hyuna I'm not gonna ignore you next meetup. I'm gonna hug you. kiss you and give you my corona. then we can be even!	1411	1	hyunaop
1545	ooh snapchat? full edie style, when we hit 1 million subs we hit full sellout style! we have the plan!	1412	1	hyunaop
1546	Tatoh is all about timings and pressure... i want to boom man!	1413	1	carloscnsz
1547	ohh that'sa a hoang! that's a hoang in my face man!	1414	1	carloscnsz
1548	"You know me guys, there is no plan B, tha castle is going up!" daut sending 20 more vills to finnish a daut castle	1415	1	carloscnsz
1549	I like to stream naked	1416	1	hyunaop
1550	Why no cam on? I Like to stream naked	1417	1	carloscnsz
1551	if organ guns cannot counter goths than just remove them from the game.	1418	1	hyunaop
1552	one gold, one FRICKING gold	1419	1	hyunaop
1553	What should i do now? fast imp? Believe really hard? Probably believing really hard is the only choice	1420	1	carloscnsz
1554	"slam go host drafting man!" daut avoiding responsibilities on the 2v2 tournament	1421	1	carloscnsz
1556	!addQuote Fuck this! I'm picking koreans! they are fun!	1422	1	carloscnsz
1557	Amazing noises you put out of your mouth!	1423	1	carloscnsz
1558	If I open the door... Can I lose the game? ... Let's see	1424	1	carloscnsz
1559	Call me surprised, but I... I'm surprised.	1425	1	artofthetroll
1560	patrol is never enough these days	1426	1	carloscnsz
1561	I want viper to win but I want him to struggle! like winning but 3-2 or something	1427	1	carloscnsz
1562	Is all about the business here!	1428	1	carloscnsz
1563	And again teleportation man!	1429	1	carloscnsz
1564	it looks like a disrespect for the tourney because it is	1430	1	carloscnsz
1565	"Man. they are wasteing our time!" daut being the only one ready in the gameroom	1431	1	carloscnsz
1566	Man! why they dont start! i want to wall already	1432	1	carloscnsz
1567	"dont think dont think! just play" daut to slam on 2v2 tournament	1433	1	carloscnsz
1568	May be i should use my twitter to retwit vipers twits	1434	1	carloscnsz
1569	Don't worry i will lose all the archers before imp	1435	1	carloscnsz
1570	Arabia with snow? I guess global warming did hit age of empires 2	1436	1	carloscnsz
1571	I have villagers. When you have villagers you can't lose the game	1437	1	carloscnsz
1572	Some people should not be allowed to sleep at night	1438	1	carloscnsz
1573	Either I'm beastly or slam is not	1439	1	carloscnsz
1574	"Come on... You have a boomy civ.. Do what your civ told you"	1440	1	carloscnsz
1575	"What's a game without a DauT castle?"	1441	1	byelo
1576	Did he found the magical gold?	1442	1	carloscnsz
1577	I can never stop a man from dancing. Not that kind of person.	1443	1	artofthetroll
1579	Actually is already 3am. May be it's my bed time	1444	1	carloscnsz
1580	I'm not even lazy this week. Day is too short	1445	1	carloscnsz
1581	I don't care that heavily that i won't even do loom	1446	1	carloscnsz
1582	I'm not a good influence for the young people	1447	1	carloscnsz
1583	I'm not stupid if I am doing it on purpose	1448	1	hyunaop
1584	Score is close. I hope that it's because we are good friends	1449	1	carloscnsz
1585	I'm old and stupid let's not forget about that	1450	1	hyunaop
1586	I need to have an intervention about my castles	1451	1	artofthetroll
1587	daut to hyuna: go watch viper. i forgive you for being traitor	1452	1	hyunaop
1588	do you like those farms better. is it pretty enough for you?	1453	1	hyunaop
1589	muss man! we need a new mascot. jordan is playing good now so we need a new one	1454	1	carloscnsz
1590	"What you need to do here is click patrol, click here and you win the game" Daut coaching a sub	1455	1	carloscnsz
1591	Next subwars I will need new viewers	1456	1	carloscnsz
1592	that ostrich scared me	1457	1	zekleinhammer
1593	don't call lamers filthy. they are worse than filthy	1458	1	hyunaop
1594	he thinks my micro is a meme or something	1459	1	hyunaop
1595	"He's just repairing man. He's such a repairing machine."	1460	1	artofthetroll
1596	shift click that shit out of my game!	1461	1	zekleinhammer
1597	I want to be dominated	1462	1	artofthetroll
1598	villagers are useless unit	1463	1	zekleinhammer
1599	4 tc boom? why not make it 5?	1464	1	zekleinhammer
1600	don't compare me to mbl but I agree	1465	1	hyunaop
1601	do I look scary to you? Do I?	1466	1	hyunaop
1602	I will forward you to riut, you can obviously check mbl, he is amazing player, but riut is better	1467	1	zekleinhammer
1603	"4 villagers on farms and we are going Champions!"	1468	1	artofthetroll
1604	you are a traitor and I am a woman	1469	1	hyunaop
1605	that mining camp is invisible	1470	1	hyunaop
1606	"should I boom or should I DauT castle him?"	1471	1	hyunaop
1607	my wall deserves to be tickled man	1472	1	hyunaop
1608	flies are like f1re they are annoying	1473	1	hyunaop
1609	Why did the bulgarian happy music stop there? ... I liked it!	1474	1	carloscnsz
1610	"ahhgg villeeese.... You used to be cool... Now you are just like everyone else..." Daut cuz villese denied his dautKrepost	1475	1	carloscnsz
1611	He is probably thinking "oh daut, don't you know you don't have crossbow with this civ?" I Know i have no crossbow but i have other things	1476	1	carloscnsz
1612	When you go for a daut castle you enjoy, you laugh and you go for a new game	1477	1	carloscnsz
1613	Oh he is franks and I'm indians, he might think my civ hard counter his civ... Little does he know that I'm going elephant archers	1478	1	carloscnsz
1614	Time for the double, triple... 4 tiles of palisade walls and enjoy the life	1479	1	carloscnsz
1615	Ohhh You are playing a little bit of arena yourself, Mr I hate arena	1480	1	carloscnsz
1616	"you think you're cool because you can micro? i can't... and i think i'm cool"	1481	1	artofthetroll
1617	When you have gold, you have everything... That's how this game works	1482	1	carloscnsz
1618	Worst case scenario actually is all my units getting converted	1483	1	carloscnsz
1619	Feitorias are your friend. If you don't have any friend, just make feitoria	1484	1	carloscnsz
1620	We are so close that forward thingies are his thingies	1485	1	carloscnsz
1621	"This is my cow now? Ofc it is! I found it!" Daut stealing enemy cow	1486	1	carloscnsz
1622	he doesn't know about this castle, we are both friends here!	1487	1	hyunaop
1623	he is building the castle with 1 villager! I respect that!	1488	1	hyunaop
1624	jsut been there looking the fight is enough micro	1489	1	carloscnsz
1625	I'm going blind hoang style... I don't even know where to attack.. I just want to attack	1490	1	carloscnsz
1626	oh.. raiding fuck! How did you get here?	1491	1	carloscnsz
1627	Any game with a daut castle is an amazing game	1492	1	carloscnsz
1628	I am not the best at quickwalling, believe it or not	1493	1	zekleinhammer
1629	I'm too nice for my own good	1494	1	carloscnsz
1630	I'm a hard working man all over the place	1495	1	carloscnsz
1631	I think you should hit everything without ballistics and with ballistics even more	1496	1	carloscnsz
1632	It has to be me failing for you to be smart	1497	1	carloscnsz
1633	My Knights make weird dying noises	1498	1	carloscnsz
1634	Counter attack me??? Is that the way you want your kids to remember you??? COUNTER ATTACKS???	1499	1	carloscnsz
1635	I would say they clean this shit up... But I don't want my kids to remember me like that, so I will just say I won this fight	1500	1	carloscnsz
1636	"I feel its a fake nice but we take those"	1501	1	hyunaop
1637	the best way to sum it up is oh fuck	1502	1	carloscnsz
1638	I dont want to chat with ghosts im scared of the ghosts!	1503	1	hyunaop
1639	Sorry for my language	1504	1	carloscnsz
1640	At least I cannot die when I'm dead	1505	1	carloscnsz
1641	I don't trust girls	1506	1	carloscnsz
1642	Screw the tasks! I'm alive! (Daut playing Among Us)	1507	1	carloscnsz
1643	I will promise you and it will never happen. Got to stay consistent	1508	1	deagle2511
1644	Vat? Viper was in chat? well I was busy dying!	1509	1	carloscnsz
1645	So many voices in my head	1510	1	carloscnsz
1646	I'm not afk, I'm always afk	1511	1	carloscnsz
1647	VIPER!!! I overrate your brain! you stupid fuck!!!	1512	1	carloscnsz
1648	I will pretend to be a snowman	1513	1	zekleinhammer
1649	farms are units	1514	1	zekleinhammer
1650	What do I think of viper civ picks? That he is an idiot	1515	1	carloscnsz
1651	"Or he shit his pants when he saw he was against me. That makes sense too."	1516	1	byelo
1652	He paused now to go to the bathroom? Probably he saw he is facing me and shitted his pants	1517	1	carloscnsz
1653	I fail. I fail with micro. Believe it or not	1518	1	deagle2511
1654	I'm too fast for this game!	1519	1	carloscnsz
1655	I didn't want to delete scout! I wanted to delete wall!!! dautPickle	1520	1	deagle2511
1656	How did he smell it! He got a strong sense of smell there	1521	1	carloscnsz
1657	On the bright side there is no bright side	1522	1	carloscnsz
1658	Double mining camp, zero tower aggression... That's every man's dream	1523	1	carloscnsz
1659	poke something! you poking fucks	1524	1	zekleinhammer
1660	shut up and take my points	1525	1	carloscnsz
1661	Derp man! Thanks for the 39months resub... still waiting for your game review?	1526	1	carloscnsz
1662	I tend to be a lazy player	1527	1	carloscnsz
1663	"im here to be bought"	1528	1	carloscnsz
1664	I will show him a micro that he will never forget	1529	1	carloscnsz
1665	If I wasn't such a fast player I would be in troubles right now	1530	1	carloscnsz
1666	Oh man I did all that and didn't delete a single villager! (Daut after deleting quickwalls on his woodline)	1531	1	carloscnsz
1667	Zebra is even blocking my micro	1532	1	carloscnsz
1668	that zebra is bigger traitor than hyuna	1533	1	zekleinhammer
1669	Man this was embarrassing... I will have to delete VOD after this	1534	1	carloscnsz
1670	Is hard to keep the energy of lying all the time	1535	1	carloscnsz
1671	I'm like un-gate-able	1536	1	carloscnsz
1672	Let's see if we can hoang out of this	1537	1	carloscnsz
1673	This strategy is actually shit	1538	1	carloscnsz
1674	What's best advice I can give to a 2k player? Never get married	1539	1	carloscnsz
1675	Why is my rank going down so fast? well... i basically don't give a shit	1540	1	carloscnsz
1676	Well rank is good for tournament seeding but I always get to play Nicov anyway	1541	1	carloscnsz
1677	We found game fast so must be a really good player *game starts* oh is not a really good player, is mbl	1542	1	carloscnsz
1678	Why karma hits me now?	1543	1	carloscnsz
1679	I wish to have students to paly with	1544	1	carloscnsz
1680	And what do you know? Is a kip motherfucker chak	1545	1	carloscnsz
1681	Where is my treb... My only friend in this game	1546	1	carloscnsz
1977	I would X the shit out of that	1808	1	carloscnsz
1682	I just wanted to press the button, it's so big and red	1547	1	zekleinhammer
1683	I found a girlfriend	1548	1	hyunaop
1684	its like going scouts as britons and expecting skirms. it makes no sense and it's no intel!	1549	1	hyunaop
1685	I don't trust her and her one-eyed dog	1550	1	zekleinhammer
1686	take a picture of my ass man. look at the picture!	1551	1	hyunaop
1687	get your monk ass out of there	1552	1	zekleinhammer
1688	and for the final i will need ultra instinct	1553	1	hyunaop
1689	hello my stupid friend we are both stupid	1554	1	zekleinhammer
1690	Is okay i believe in musss, he is too stupid to lie	1555	1	carloscnsz
1691	Yeah you are dead now, but sometimes you are not dead	1556	1	carloscnsz
1692	Just look at the mangonel... looking sus	1557	1	carloscnsz
1693	I'm maybe 35 but I'm still an idiot	1558	1	carloscnsz
1694	if I reach finals I will be so happy and then threw the finals	1559	1	carloscnsz
1695	"There is a limit to my powers man!" Daut while microing	1560	1	carloscnsz
1696	Ok slam, now I'm in your timezone so better be careful!	1561	1	carloscnsz
1697	Was this greedy, or stupid?	1562	1	carloscnsz
1698	when I remember that villager it will be like skeleton	1563	1	zekleinhammer
1699	If i cast i will be like "ohh hahaha look at tatoh losing scout to tc what a pleb" instead of casting	1564	1	carloscnsz
1700	I don't full wall in the kotd but here I even stone wall... priorities man!	1565	1	carloscnsz
1701	He is top score and i'm not? vaat? what does he know that i don't ?	1566	1	carloscnsz
1702	I bet you guys never saw walls like that	1567	1	carloscnsz
1703	BAM and... GG. It was a gg bam	1568	1	carloscnsz
1704	"How much do you need to scroll to find this poor fucker?" daut searching for jordan on the ladder	1569	1	carloscnsz
1705	"come on spanish vills you have blood of the tatoh"	1570	1	carloscnsz
1706	should I kill smarthy? does she deserve to die? yes she does	1571	1	zekleinhammer
1707	I can hide here in the darkness in the corner no one will notice me anyhow	1572	1	zekleinhammer
1708	are those orjan guns? They are bad in every game	1573	1	zekleinhammer
1709	"This is very deep in the night for me" daut at 11am	1574	1	carloscnsz
1710	camera shake? pft, I don't want camera to shake with me - Daut playing AoE3	1575	1	goldeneye_
1711	Do i have pets? yes, viper. is a snake. i call it baldy	1576	1	carloscnsz
1712	oh this civ is like Burmese I see everything. (DauT playing Inca in Age3)	1577	1	hyunaop
1713	I have fat llamas! come here you fat llamas.	1578	1	hyunaop
1714	What do I think of viper getting dominated by yo? .. well is not the first time	1579	1	carloscnsz
1715	"You guys that know all the cards and the game... come queue up and play!" Daut to the caht on aoe3 stream	1580	1	carloscnsz
1716	This was almost as quick as mrYo vs Viper	1581	1	carloscnsz
1717	Everything on this game is like "who cares" Best game ever	1582	1	carloscnsz
1718	We have the plan: Go up, and destroy! The important part is Destroy!	1583	1	carloscnsz
1719	When you play against robo, the counters doesnt exist	1584	1	carloscnsz
1720	I mean I'm playing against robo! Losing is not an option	1585	1	carloscnsz
1721	Panda! I don’t wanna kill a panda, not even for 55 gold dautLove	1586	1	zekleinhammer
1722	je suis, my ass, man!	1587	1	zekleinhammer
1723	Is this a good fight for me? My units look way cooler	1588	1	carloscnsz
1724	I was waiting whole game to raid there but now i don't want to	1589	1	carloscnsz
1725	The build is not good, the build is not good... But the player is amazing! so we will make it work!	1590	1	carloscnsz
1726	Ok guys, you better kill all that without him noticing	1591	1	carloscnsz
1727	I create the wood, man	1592	1	zekleinhammer
1728	Man the fights look much more better when i mix army... I'm done with mixing army, time for full cavalry!	1593	1	carloscnsz
1729	this is the game! they don't know me so they talk trash to me!	1594	1	carloscnsz
1730	"That means you are worse than trash" daut after beating a guy that called him trash	1595	1	carloscnsz
1731	"this is better than kotd, no one enjoys that" Daut streaming aoe3	1596	1	carloscnsz
1732	chargez his ass	1597	1	zekleinhammer
1733	a man, a goat and a monkey. what the fuck is that army?	1598	1	carloscnsz
1734	welcome to the chargez	1599	1	zekleinhammer
1735	why my villagers are not dancing?	1600	1	carloscnsz
1736	Lets go forward! it will be magical! trust me	1601	1	carloscnsz
1737	my dogs will eat him alive!	1602	1	zekleinhammer
1738	I always listen to my chat... but dont read it	1603	1	carloscnsz
1739	it costed me a little bit of my soul but i win!	1604	1	carloscnsz
1740	Yeah I'm going off. Goodbye fucker!	1605	1	carloscnsz
1741	"A nice fucker here!" daut when the opponent told him it was an honor to face him	1606	1	carloscnsz
1742	I'm such a bad treasure hunter!	1607	1	carloscnsz
1775	thank you man for making my life unhealthy I appreciate that	1608	1	hyunaop
1776	he knows the modern micro	1609	1	zekleinhammer
1777	He is expecting tech switch... little does he know	1610	1	carloscnsz
1778	get yourself your own llamas!	1611	1	zekleinhammer
1779	I love Russians!	1612	1	zekleinhammer
1780	I want to make top 50. If nili can do that in aoe2 I can do that in any game	1613	1	zekleinhammer
1781	It is hard to click on that small cat	1614	1	carloscnsz
1782	Even the halbs are like petting them gently	1615	1	carloscnsz
1783	U just patrol in until you win	1616	1	carloscnsz
1784	hyuna welcome to the covid club	1617	1	hyunaop
1785	We are not team playing the treasures man!	1618	1	carloscnsz
1786	Yeah he is smart and you are stupid. This is how the game works	1619	1	carloscnsz
1787	I'm unreasonable good	1620	1	carloscnsz
1788	So you are useless, that's what you are telling to me	1621	1	carloscnsz
1789	I made mistake by trusting you	1622	1	carloscnsz
1790	You were all the game like oh help me daut help me daut	1623	1	carloscnsz
1791	Our army composition is playing alone	1624	1	carloscnsz
1792	That's your counter attack? that's a pathetic counter attack man!	1625	1	carloscnsz
1793	I know why u take so long on the bathroom viper, the doctor did you some things	1626	1	carloscnsz
1794	You are like talking shit and doing shit!	1627	1	carloscnsz
1795	I won't even check enemy civ. I'm just doing my plan here	1628	1	carloscnsz
1796	I did said i don't want double stable but i lied! I want double stable	1629	1	carloscnsz
1797	Can you please stop running away and just die?	1630	1	carloscnsz
1798	I should have deleted some units in that fight to give you hope	1631	1	carloscnsz
1799	"You are not fun you are easy easy to kill" Daut playing aoe3 1v1s against viper	1632	1	carloscnsz
1800	You don't enjoy bush?	1633	1	carloscnsz
1801	You make the units, you kill the stuff, make a bit of economy, lose tournaments	1634	1	carloscnsz
1802	I'm viper, I'm yellow and I have a viking hat!	1635	1	carloscnsz
1803	I'm always weird	1636	1	carloscnsz
1804	camaaan you are stronger than cats	1637	1	carloscnsz
1805	Yeah i was slow at ageing but not in real life	1638	1	carloscnsz
1806	Oh you know that my army is not there and you give a single fuck about that	1639	1	carloscnsz
1807	I'm cool, I'm powerful	1640	1	carloscnsz
1808	"You need to lose some points and get weaker opponents and send them where they belong" daut getting viper on the ladder	1641	1	carloscnsz
1809	"His clan name is washed up? VAT? They are taking our memes man!" daut playing aoe3	1642	1	carloscnsz
1810	You guys that don't know eddie are missing a lot	1643	1	carloscnsz
1811	I was like "ah haha! look at those mustaches" and now I'm dead	1644	1	carloscnsz
1812	Why I'm asking if I can't even read the chat	1645	1	carloscnsz
1813	Screw you viper! just Queue up again	1646	1	carloscnsz
1814	I will trust you more than the sheep guy	1647	1	carloscnsz
1815	Why are you guys so happy? We are losing this game! Don't get happy for our loss	1648	1	carloscnsz
1816	"This must be how nili feels" daut after losing a few aoe3 games in a row	1649	1	carloscnsz
1817	Stop saying you played competitively matt! someone may believe it!	1650	1	carloscnsz
1818	Voices out of my head! Feels good to mute!	1651	1	carloscnsz
1819	Oh he stomped my ass	1652	1	carloscnsz
1820	"Oh... food is in the oven and i can't pause!" daut not knowing aoe3 pause hotkey	1653	1	carloscnsz
1821	I will turn them into man at arms... ohh noo i will turn them into nothing! I will turn them into graveyard....	1654	1	carloscnsz
1822	Do I take his castle and end the game... sounds like an awful idea	1655	1	carloscnsz
1823	Yeah i tend to lie	1656	1	carloscnsz
1824	you make a castle, I make a castle	1657	1	zekleinhammer
1825	You look at my castles and gameplay and you feel better about yourself	1658	1	carloscnsz
1826	they are not even dying man!	1659	1	carloscnsz
1827	GG... The timeline will be embarrassing here...	1660	1	carloscnsz
1828	Those look cool. They look like a happy group of people	1661	1	carloscnsz
1829	Welcome to the third stream of the day	1662	1	carloscnsz
1830	you got to play mandinkas man. your dream came true. living the life.	1663	1	hyunaop
1831	thats a fun fact. I am a fricking idiot!	1664	1	hyunaop
1832	I don't like him. He is mean to me	1665	1	carloscnsz
1833	Hello and welcome to aoe3, The community that respects me	1666	1	carloscnsz
1834	cannot wait to get raided to death on that side as well	1667	1	carloscnsz
1835	The only good thing about corona being around is that we do not meet again	1668	1	carloscnsz
1836	Every wall needs a hole	1669	1	carloscnsz
1837	I remember to ask pro players 20 years ago like a little pleb "oh is that the best strategy?"	1670	1	carloscnsz
1838	"Derp is just playing arena, no ones wants to see that" Daut ignoring Derp recorded game review for months already	1671	1	carloscnsz
1839	I'm nothing if I'm not a castle builder	1672	1	carloscnsz
1840	I hope he will push me and beat me	1673	1	carloscnsz
1841	OHHH!!! HE GOT ANSWERS FOR EVERYTHING!!!!	1674	1	carloscnsz
1842	I don't care if it cost me the game! I WANT MY CASTLE THERE!	1675	1	carloscnsz
1843	I was just trying to be nice, F1re, I don't want to play with you	1676	1	zekleinhammer
1844	I was trying to be nice with you Fire! I don't want 2v2s with you!	1677	1	carloscnsz
1845	Boars against me micro like lierey	1678	1	carloscnsz
1846	Playing TGs with Fire is him thanking subs in portuguese while not muting	1679	1	carloscnsz
1847	Welcome to the arena player	1680	1	carloscnsz
1848	He tries to convert me and then magic happens	1681	1	carloscnsz
1849	"Well maybe he doesn't notice this" Daut sending 3 battering rams agains a castle in the middle of enemy economy	1682	1	carloscnsz
1850	What is worst? Losing to chris or to jordan?	1683	1	carloscnsz
1851	"I have no friends, you guys are all i have" Daut to the chat	1684	1	carloscnsz
1852	I would do that, win a few millions a go live at the countryside. Not giving a single fuck	1685	1	carloscnsz
1853	Lierey trusts me with his life man! That's respect!	1686	1	carloscnsz
1854	I feel like viper! Only he dies so fast	1687	1	carloscnsz
1855	not doing anything is the micro! Predict that!	1688	1	zekleinhammer
1856	Did i just lose everything there? Nevermind they died by themselves... Not my fault	1689	1	carloscnsz
1857	He has more man at arms, but mine are cheaper... and cooler	1690	1	carloscnsz
1858	He is abusing market to create militia man! Only hoang does that	1691	1	carloscnsz
1859	He doesnt create vills? I dont create vills! Who does he think he is?	1692	1	carloscnsz
1860	"Lierey is outmicroing me even through the chat" Daut cuz his boar went back while reading lierey on his twitch chat	1693	1	carloscnsz
1861	"No no no no we stay there together forever and ever! You are my boyfriend now! You don't dare leaving me!" Daut after trapping whole enemy army	1694	1	carloscnsz
1862	Usually when the plan starts with "I hope" the plan is hopeless	1695	1	carloscnsz
1863	He doesn't boom like me, my economy is cooler	1696	1	carloscnsz
1864	"What do i do with my monks now?... He doesn't think of me" Daut cuz enemy switched to full light cavs	1697	1	carloscnsz
1865	Do i buy my way up to imp and say "surprise motherfucker?	1698	1	carloscnsz
1866	I enjoy having TCs	1699	1	zekleinhammer
1867	"We can talk about that, and discuss it" daut dening enemy castle	1700	1	carloscnsz
1868	Lure me like a pig!	1701	1	artofthetroll
1869	scouting bad against mbl who I just lamed last game... Not good!	1702	1	carloscnsz
1870	this must have been the longest conversion in the history of converts	1703	1	carloscnsz
1871	Not going up... I'm going down	1704	1	carloscnsz
1872	I dont have the numbers, I dont have economy... I just have idea	1705	1	carloscnsz
1873	Jaguars make very little sense her but i will made them	1706	1	carloscnsz
1874	There is no problems when u have a castle	1707	1	carloscnsz
1875	Rams are cool but they can't cut down trees	1708	1	carloscnsz
1876	Let's enjoy the fight and type gg	1709	1	carloscnsz
1877	I show that house who is the boss! Now let's do something	1710	1	carloscnsz
1878	"Did they hire nili to write those tips or what?" daut reading the random advices from the game in the loading screen	1711	1	carloscnsz
1879	Everywhere I look I see a dead vill	1712	1	carloscnsz
1881	look guys! 2 food saved! Is all about those economy tricks	1713	1	carloscnsz
1882	"Aren't you supposed to be smarter than knights?" Daut to his light cavs	1714	1	carloscnsz
1883	I don't see ding ding	1715	1	carloscnsz
1884	Get TC get everything! BAM BAM BAM! oh no no don't take my own units! That's not everything	1716	1	carloscnsz
1885	I scout everything and I see nothing	1717	1	carloscnsz
1886	I don't even want to brag about that, just another day in the work	1718	1	carloscnsz
1887	I'm definitely not a smooth clicker	1719	1	carloscnsz
1888	I don't want to resign against him! I can't take another defeat!	1720	1	carloscnsz
1889	"Maybe is time to take a small break from aoe"	1721	1	carloscnsz
1890	"I can cut trees so... Why would i boom if i could cut trees" Daut going 1TC ballista elephants	1722	1	carloscnsz
1891	This guy is not 1k8! He is palying better than fire	1723	1	carloscnsz
1892	No no, I always boom but especially when my teammates are dying!	1724	1	carloscnsz
1893	I'm hoanging the honag man! Let's see how he likes it!	1725	1	carloscnsz
1894	I really like losing. gives me motivation	1726	1	carloscnsz
1895	You wont lose today lierey! You will learn! you will learn who is the better player	1727	1	carloscnsz
1896	I'm just promising something that will never happen	1728	1	carloscnsz
1897	I have more problems vs 1800 elo players than vs 2k3	1729	1	carloscnsz
1898	And you guys ask for a daut castle? This is a little preview	1730	1	carloscnsz
1899	Why they do design maps like that man? Just to annoy me!	1731	1	carloscnsz
1900	"It seems like lierey left us... Left us with all his points!!!" Daut after 4-1 ing the kid	1732	1	carloscnsz
1901	Scared of the better unit? Or the better player?	1733	1	carloscnsz
1902	Ok guys I gonna end the stream now, instead of watching other stream watch my VOD from earlier	1734	1	carloscnsz
1903	"AH! jordan.... I'm hitting rock bottom" daut getting matched with jordan in 1v1 instead of lierey	1735	1	carloscnsz
1904	"I'm ain't scare of a jordan man!" daut sending a vill alone to wall far away	1736	1	carloscnsz
1905	You are fucking up with me now! arent you little fuck	1737	1	carloscnsz
1906	we will chase him, we will find hime and we will have a feast on his blood	1738	1	carloscnsz
1907	*chat asks daut to check for a hole* daut: "it could be a hole but i wont check. if it happens it happens! it would be destiny!"	1739	1	carloscnsz
1908	That's my game plan right now. He sees my barracks and doesn't know he can make Cataphracts [on TheMax].	1740	1	51mpnation
1909	How is that not a bam?!	1741	1	51mpnation
1910	I don't know what my title says but is all a lie	1742	1	carloscnsz
1911	I guess they are dead but... Where are the bodies then???	1743	1	carloscnsz
1912	Civ win is still a win!	1744	1	carloscnsz
1913	"He tried to outsmart me, but I'm not smart at all!"	1745	1	byelo
1914	"This woodline! Even my woodlines got holes, man! And people ask me why I don't wall!"	1746	1	byelo
1915	I'm getting less happy man	1747	1	carloscnsz
1916	Bombard cannon goes boom! And we can resign	1748	1	carloscnsz
1917	Nobody deserves to be Krepost rushed	1749	1	carloscnsz
1918	uh no! agrr ahhrr grwr... I do sounds for living	1750	1	carloscnsz
1919	Get comfortable around there woman	1751	1	carloscnsz
1920	It's more fun with a market.	1752	1	51mpnation
1921	That beautiful son of a bitch man! Look at him!	1753	1	carloscnsz
1922	I'm drinking and having a feast here while playing! Next game will be higher quality I promise!	1754	1	carloscnsz
1923	Probably we have an easy team wall with villese... But he beat me on kotd, he doesn't deserve to be with me, let him die outside	1755	1	carloscnsz
1924	I was thinking we got TG we are chil... No one is chill anymore	1756	1	carloscnsz
1925	I AM A GOD!	1757	1	carloscnsz
1926	The game doesn't allow me to be cool basically	1758	1	carloscnsz
1927	I did asked who wins cataphracts or elephants... but now i don't want to know! Let it be a mystery!!	1759	1	carloscnsz
1928	Daut to Jordan in hamburg "Jordan you are wasting your time working! come play videogames"	1760	1	carloscnsz
1929	"Oh jordan you are so lucky" daut when they start TG and he is jordan's pocket	1761	1	carloscnsz
1930	Im here to chill, boom, make imp units and patrol	1762	1	carloscnsz
1931	Jordan or Nili I dont know who i make more fun off on my stream	1763	1	carloscnsz
1932	Cuman scout, faster than an arrow	1764	1	51mpnation
1933	I need to record my voice and put it on my room so my family thinks I'm working so then i can go sleep	1765	1	carloscnsz
1934	Ok guys i have to go and spend some "quality time" with my family	1766	1	carloscnsz
1935	I am not the guy who traps - I am the guy who gets trapped	1767	1	goldeneye_
1936	Am I booming at home at least?	1768	1	51mpnation
1937	"You lose the army, I lose the game!"	1769	1	byelo
1938	If I wanted to suffer, I'd be playing with my kids right now	1770	1	zekleinhammer
1939	"Pffft, I don't like right clicking you!" DauT to Viper.	1771	1	byelo
1940	Viper: "Dock the right corner, DauT!" DauT "What, I am not docking! Why would I dock!" #JustNomadThings	1772	1	byelo
1941	If he converts me I go to the winning team	1773	1	artofthetroll
1942	even if I played tetris they would still wall against me man	1774	1	hyunaop
1943	he probably thinks I’m outbooming him... maybe I am: DauT with 0 on food	1775	1	zekleinhammer
1944	you don’t want to see my fat ass	1776	1	zekleinhammer
1945	I don't even know what VPN does, man!	1777	1	artofthetroll
1946	resolution for new year: learn to push deers	1778	1	deagle2511
1947	How to test the map that nobody wants to play?	1779	1	carloscnsz
1948	poke it poke it, pokemon! (daut trying to get a spearmen to stab a scout)	1780	1	hyunaop
1949	To be a good caster you need to know very little about the game	1781	1	carloscnsz
1950	"People expect me to lose tournaments, so I lose them. I deliver man"	1782	1	goldeneye_
1951	I was busy for a month trying to survive	1783	1	carloscnsz
1952	"When i was a child i used to cheer for the roadrunner but now i will cheer for the coyote man COYOTEE!!!!" daut luring ostriches	1784	1	carloscnsz
1953	I will send coyote for you man!	1785	1	zekleinhammer
1954	"People expect me to play a tournament and lose, I DELIVER!"	1786	1	artofthetroll
1955	"maybe I went to a Daut mode to fast now"	1787	1	goldeneye_
1956	"one more game. then again, in other room i hear baby crying, so maybe 2 more games is an option as well"	1788	1	artofthetroll
1957	Hyuna if I know you're queueing up I am definitely cancelling	1789	1	hyunaop
1958	I'm still young... Ohhh I'm really young and stupid	1790	1	hyunaop
1959	¡BASTARDO!	1791	1	zekleinhammer
1960	Go and catch mbl? He is not a pokemon man! But I'll try	1792	1	carloscnsz
1961	Easier to catch pokemons than those deers	1793	1	carloscnsz
1962	Wenegor are you really playing nili in a tournament? Did he really sink that low?	1794	1	carloscnsz
1963	"I prefer losing rather than being called T90" daut deleting a bad farm to make a new one in a better place	1795	1	carloscnsz
1964	Nili is joining Suomi? OH PLEASE LET IT NO BE A TROLL. I would love nili in suomi	1796	1	carloscnsz
1965	"I hate viper equally as much" Daut refering to mbl	1797	1	carloscnsz
1966	"I will give you 3 of the letters of the draft link and you guess the rest" Daut not wanting to gave the draft link to memb	1798	1	carloscnsz
1967	We play tomorrow but idk at what time. He ask me if we play tomorrow and i said yes	1799	1	carloscnsz
1968	The plan was not to afford things	1800	1	carloscnsz
1970	I need a castle on his face... That will show him	1801	1	carloscnsz
1971	Look at my resources man! I don't care! I'm winning this shit!	1802	1	carloscnsz
1972	At least I'm up! Is better that being down!	1803	1	carloscnsz
1973	am I right or am I right?	1804	1	carloscnsz
1974	are you a freaking monk? Stop converting the subs!	1805	1	zekleinhammer
1975	Practicing?	1806	1	carloscnsz
1976	"I'm willing to take the sacrifice" daut about letting hera die so he can boom	1807	1	carloscnsz
1978	"this game will be studied for the next generation"	1809	1	artofthetroll
1979	Well somebody is fucking up then... but not me!	1810	1	carloscnsz
1980	You were pressuring me "save the woman, save the woman!" and i did not save the woman	1811	1	carloscnsz
1981	i boomed with 6tcs man! If that's not effort idk wahat it is	1812	1	carloscnsz
1982	Nobody trusts me	1813	1	carloscnsz
1983	What will happen next? Lets find out together!	1814	1	carloscnsz
1984	We are doubleing one guy and we are losing!	1815	1	carloscnsz
1985	Are you having sex man? what are those noises	1816	1	carloscnsz
1986	"Screw u guys! I'm going home!" Daut running away from the battle with his kts	1817	1	carloscnsz
1987	Aren't other players in this game they can fuck around with?	1818	1	carloscnsz
1988	If you sub to me you get EMOTES MAN!!! emotes... and my love	1819	1	carloscnsz
1989	I want to win and get paid	1820	1	carloscnsz
1990	That was madafaka yoink	1821	1	carloscnsz
1991	"I will just go micro-god mode. I'm nothing if not that."	1822	1	artofthetroll
1992	"1, 2, 3, 4...6"	1823	1	artofthetroll
1993	"Oh I lost 20 vills."	1824	1	artofthetroll
1994	Don't fuck with me! Just give me the stone!	1825	1	carloscnsz
1995	Now i say "screw u kids" I will play aoe	1826	1	carloscnsz
1996	better late than hyuna	1827	1	carloscnsz
1997	"dont kill my house, i will get housed"	1828	1	artofthetroll
1998	memb man, he's loud even when chatting	1829	1	hyunaop
1999	why don't I get the instant conversion man?	1830	1	hyunaop
2000	I made hand cannoners? That’s not a bad idea! Good for me!	1831	1	zekleinhammer
2001	I don't want to listen to you. you are not smarter than me!	1832	1	hyunaop
2002	Why did I take that fight? I thoughtI had a cool army... And it was a cool army...	1833	1	carloscnsz
2003	"He is 2k2! Everybody is improving, except one guy; me!"	1834	1	byelo
2004	When i started to stream I coached art of troll and now he is worst than ever	1835	1	carloscnsz
2005	Look at hera wanting to know what dautmas is	1836	1	carloscnsz
2006	Yeah my kids are out of punishment coins so they will have to wait to play with me	1837	1	carloscnsz
2007	monks are the clown killers	1838	1	zekleinhammer
2008	when you have two kids you don’t do a proposal you just get it over with	1839	1	zekleinhammer
2009	Time to switch my name to mbl to make the ladder full of mbls	1840	1	carloscnsz
2010	Ill host daniboy. he is still a pleb but there is hope for him	1841	1	carloscnsz
2011	Spanish builders go BZZOOOMM	1842	1	carloscnsz
2012	"My fellas are toying with you jordan!" Daut during community games (daut team member trapped jordan vills)	1843	1	carloscnsz
2013	"ohh.. my team is trolling you. I like my team" Daut to Jordan during community games	1844	1	carloscnsz
2015	Your rating explains your spelling	1845	1	carloscnsz
2016	I know you dont mind sharing your gold	1846	1	carloscnsz
2017	this is the hardest thing in the life of me	1847	1	carloscnsz
2018	No problem I'm just fighting vs 3 on the water and 5 on the land	1848	1	carloscnsz
2019	I wish I was playing this good on torunaments	1849	1	carloscnsz
2020	You guys are too nice, are you sure you are from my channel?	1850	1	carloscnsz
2021	First time I do something good with my life and you do this!	1851	1	carloscnsz
2022	I killed his vill cuz he was showing off too much with quick walls	1852	1	carloscnsz
2023	You got OUTCLASSED! DESTROYED!!!	1853	1	carloscnsz
2024	Did wolf kill one of your people?	1854	1	carloscnsz
2025	Even elite skirm warriors!	1855	1	carloscnsz
2026	cuz everybody is watching my stream! They only go to yours to check score	1856	1	carloscnsz
2027	Nice castle! I'm proud of you	1857	1	carloscnsz
2028	If I had to lose let it be against elephants	1858	1	carloscnsz
2029	"This was like the charity stream as well" daut after beating nili and getting 70% on nili's donations	1859	1	carloscnsz
2030	That's my new year resolution... Is a lie but people always lies with those	1860	1	carloscnsz
2031	it’s a DauT penta now!	1861	1	zekleinhammer
2032	I hate to play with tatoh man... He is so disappointed when i make a mistake	1862	1	carloscnsz
2033	Oh I won, you are all dead	1863	1	carloscnsz
2034	I should tell them there is a guy behind... nah they will find out	1864	1	carloscnsz
2035	I'm going to wall his TC	1865	1	carloscnsz
2036	Viper wasn't happy about that but we wanted to win	1866	1	carloscnsz
2037	"I used to be #1 player, then Nili was yelling 'Daut Castle'... and now...I'm not"	1867	1	artofthetroll
2038	and you still didn't beat me nili... 20 years of losing	1868	1	carloscnsz
2039	The next generation will even find the Pac Man complicated	1869	1	carloscnsz
2040	Hyuna man you are one weird fuck, but you have a valid point.	1870	1	yuna_op
2041	Hello boy! What are you doing alone in the woods?	1871	1	zekleinhammer
2042	mrYo streams on doyutv so his stream is like hera's full of adds everywhere	1872	1	carloscnsz
2043	How do people cast for living?	1873	1	carloscnsz
2044	“[On a donation] I actually forgot the message but still liked the monies”	1874	1	51mpnation
2045	The only thing i did not prepare is my homemaps	1875	1	carloscnsz
2046	I got lyxed apparently	1876	1	zekleinhammer
2047	I demand my demo to be in that gold	1877	1	zekleinhammer
2048	I wanted to be smart now I look stupid	1878	1	zekleinhammer
2049	"that's why this game is so good, when you lose a unit, you can make another unit"	1879	1	zekleinhammer
2050	what kind of person doesn't let another person spam outposts	1880	1	zekleinhammer
2052	If could travel back in time i wouldn't even play the lottery, I will just play aoe and win it all and laugh at people	1881	1	carloscnsz
2053	never always 5 tc (dautWat?)	1882	1	zekleinhammer
2054	micro is always strong on the boars against me	1883	1	carloscnsz
2055	I might be able to finish, might not	1884	1	zekleinhammer
2056	nothing that my good friend the market cannot fix	1885	1	zekleinhammer
2057	Viper told me that jordan beated lierey and yo... I still don't buy it	1886	1	carloscnsz
2058	You lost to nili! is not too soon when u lost too nili	1887	1	carloscnsz
2059	Welcome to the f1re club	1888	1	carloscnsz
2060	Damn right he enjoys getting beaten off	1889	1	carloscnsz
2061	Don't listen to me guys	1890	1	carloscnsz
2062	I'm allowed to talk about legal stuff	1891	1	carloscnsz
2063	I lost my virginity vs the mimmox	1892	1	carloscnsz
2064	Chat wanted me to douche and i get persians as random civ... Is that a signal?	1893	1	carloscnsz
2065	We are super close adn he is super walled	1894	1	carloscnsz
2066	How do i post on youtube if i lose?	1895	1	carloscnsz
2067	No wood, no nothing... A lot of believing	1896	1	carloscnsz
2068	Im nothing if i'm not crazy	1897	1	carloscnsz
2069	When you have villagers you are always doing fine	1898	1	carloscnsz
2070	At least i have something to turn grey! You baldy fuck!	1899	1	carloscnsz
2071	This SPARTA!! ... I thought this is sparta...	1900	1	carloscnsz
2072	"I was thinking, this is Sparta"	1901	1	51mpnation
2073	slam man! Dont be salty cuz you lose to the hoang and some people can delete vills and win	1902	1	carloscnsz
2074	Why no attack ground with mangos? We dont do that	1903	1	carloscnsz
2075	Is he forwarding me? ... nooo dont kill my fuuun.....	1904	1	carloscnsz
2076	I'm doing podcast anyway	1905	1	carloscnsz
2077	VAT! He moved my militia man! He moved it to build his palisade	1906	1	carloscnsz
2078	I cannot be such a nicov	1907	1	carloscnsz
2079	"Better fight now. Trust me, soon, it will not be an option for you."	1908	1	51mpnation
2080	What's Serbian moonshine called? Viper! Viper is my moonshine	1909	1	carloscnsz
2081	I need to respect that viewer. Lets go full man at arms!	1910	1	carloscnsz
2082	Don't do this at home, is a bad move... I just like doing it	1911	1	carloscnsz
2083	This is why I love to have memb in the community and in my channel... I feel young again	1912	1	carloscnsz
2084	Ill attack u, look, im 10years old, look	1913	1	carloscnsz
2085	"And he is like a micro player right? I heard stories" Daut playing with lierey	1914	1	carloscnsz
2086	If each of you guys send me two thousand dollars right now, I would be a millionaire Just saying	1915	1	carloscnsz
2087	"You gave gift 50 subs to this guy! I appreciate it!" daut after losing half his xbow before imperial upgrades to a random mangonel	1916	1	carloscnsz
2088	Ballistics dodgers!	1917	1	carloscnsz
2089	MBL is my friend, he is a little bit young and stupid but he is nice	1918	1	carloscnsz
2090	If aoe4 is a big success I will have to o professional interviews without curse and stuff... so I hope aoe4 fails	1919	1	carloscnsz
2091	I dont like deleting houses! People live in those!	1920	1	carloscnsz
2092	Seeing your support guys is actually more important for me than actually winning the tournament	1921	1	carloscnsz
2093	Players are like my puppets! I pick their civs and they do as i want them to do	1922	1	carloscnsz
2094	If u take the risk and lose u feel good tomorrow, If u play scared and lose u feel like shit	1923	1	carloscnsz
2095	My economy says go for the wagons! I listen to my economy	1924	1	carloscnsz
2096	This time is when the throw begins	1925	1	carloscnsz
2097	I so crazy how much u can learn from watching recorded games	1926	1	carloscnsz
2098	The shield armor, the fuck armor	1927	1	carloscnsz
2099	First place is the only that matters	1928	1	carloscnsz
2100	They are wasting my time here...	1929	1	carloscnsz
2101	U guys feel the pressure playing with the best player?	1930	1	carloscnsz
2102	"I'm trying to be one of u guys" daut to his teammates after he won red bull	1931	1	carloscnsz
2103	Yeah we were good and we are not	1932	1	carloscnsz
2104	GG guys! GG!! Thank you for dragging me down with you	1933	1	carloscnsz
2105	Is play all 7 games so 7-0 is an option	1934	1	carloscnsz
2106	I believe in Viper, he is like 2nd best player in GL	1935	1	carloscnsz
2107	Whoever wins that fight both loses	1936	1	carloscnsz
2108	Tatoh made me do stone walls so now i'm missing 1 TC	1937	1	carloscnsz
2109	A few more drop tricks and we got this	1938	1	carloscnsz
2110	If i send 1 vill forward i lose 2	1939	1	carloscnsz
2111	What do you need to get out of the Do Do	1940	1	carloscnsz
2112	We save indians for u guys to lose on acropolis	1941	1	carloscnsz
2113	slam was like "i got u daut dont worry" and then i lost all villagers and slam was like "sorry daut i didnt had stone"	1942	1	carloscnsz
2114	Whoever won red bull raise your hand!	1943	1	carloscnsz
2115	if u want to reschedule please ask the same to all my team.. Im not doing your work for u!	1944	1	carloscnsz
2116	"i can't sit but i'll not, LET'S GO"	1945	1	carloscnsz
2117	I give you some konnicks	1946	1	zekleinhammer
2118	All the deals in my channel are made with myself to myself	1947	1	carloscnsz
2119	I went full fire here	1948	1	carloscnsz
2120	Yes yes, i made my mistakes and i'm proud of them	1949	1	carloscnsz
2121	I dont know if i stole somebody's sheep but i will	1950	1	carloscnsz
2122	freaking miguel man! everywhere i look a relic is being picked	1951	1	carloscnsz
2123	I micro with the brain	1952	1	carloscnsz
2124	Fire is slower than my internet	1953	1	carloscnsz
2125	I think fire should do it. I gonna watch him	1954	1	carloscnsz
2126	and he just answers ok... that's such a girly thing to do	1955	1	carloscnsz
2127	he best thing is this is my last game so... hit and run! beat him and host him to let him with more salt	1956	1	carloscnsz
2128	I take no peoples llama	1957	1	carloscnsz
2129	boost me, man!	1958	1	zekleinhammer
2130	"I have lierey on my side!" Daut when his kts were not converted by enemy monk	1959	1	carloscnsz
2131	Man, is it hard to get wood or what?	1960	1	zekleinhammer
2132	whats my best joke? nili giving up casting will make him a top player	1961	1	carloscnsz
2165	goodbye webcam, hello shisha	1962	1	zekleinhammer
2166	I respect the promise and still fail at the same time	1963	1	yuna_op
2167	Obviously casting hurts your skill... look at memb	1964	1	carloscnsz
2168	I don't want to make hera mad, he will make a post or something	1965	1	zekleinhammer
2169	I dont want to lame hera man! he will make a post about me or something	1966	1	carloscnsz
2170	That lion is helping more than you	1967	1	carloscnsz
2171	I want to build a castle but not only build it but also finish it you know?	1968	1	carloscnsz
2172	Do you see my wood economy? well i not!	1969	1	carloscnsz
2173	Yeah yeah, let's kill your guy so you look even better	1970	1	carloscnsz
2174	I always ask myself how can i die for my team	1971	1	carloscnsz
2175	they cannot steal the shorefish	1972	1	carloscnsz
2176	Jordan once again no knowing a single thing about the game	1973	1	carloscnsz
2177	"Im not proud of my team right now!" daut casting TG where tatoh and jordan both did 3 layers of stonewalls in castle age	1974	1	carloscnsz
2178	No micro can stop Jordi	1975	1	carloscnsz
2179	Microing boars, microing fighting microing	1976	1	carloscnsz
2180	why would I make scouts when knights are stronger?	1977	1	zekleinhammer
2181	Where do sign up for tempo?	1978	1	carloscnsz
2182	Oh man u have more army, more villagers and still managed to lose	1979	1	carloscnsz
2183	good hole, viper, good hole	1980	1	zekleinhammer
2184	Viper: we actually have a chance to win now. Daut: no no no	1981	1	carloscnsz
2185	Actually We are going for the win! Jorda! enjoy the bench	1982	1	carloscnsz
2186	Lets do it like this lets guess how many continents there are in the world, the closest one gets to play... i mean cuz jordan once said there were only 4 continents or so	1983	1	carloscnsz
2187	I have more economy this game that all you guys together, the whole team	1984	1	carloscnsz
2188	And viper was like oh daut can u please send me a few knights.... prety please!	1985	1	carloscnsz
2189	"I have the feeling my team mates will attack me first" daut about to play a FFA event	1986	1	carloscnsz
2190	I'm a bit busy for your bullshit hera....	1987	1	carloscnsz
2191	At least i'm the best of the worst	1988	1	carloscnsz
2192	Why did i bully viper?? Didn't you saw what he did to my relic	1989	1	carloscnsz
2193	This is bad now I have to micro	1990	1	batbeetch
2194	Viper lowest score, i like that... doesnt mean anything right now but i like to watch it	1991	1	carloscnsz
2195	Age of empires is much higher quality than those new shinny things	1992	1	carloscnsz
2196	are my units also invisible? No they are alive	1993	1	zekleinhammer
2197	Make man at arms faster or slower!	1994	1	carloscnsz
2198	Douche? ... I absolutely get nothing from that	1995	1	carloscnsz
2199	"WHAAAA whaa? whaaa? X please!" daut when scouts entered his presumed fully walled base	1996	1	carloscnsz
2200	ho ho!! Daut snipe!	1997	1	carloscnsz
2201	"The hole was a lie, man."	1998	1	51mpnation
2202	"I'm going to try hard like it's a RedBull."	1999	1	51mpnation
2203	Stupid ass game!	2000	1	artofthetroll
2204	One day I will make another Donjon and that one will be amazing!	2001	1	carloscnsz
2205	Im gonna yoink you all day man!	2002	1	carloscnsz
2206	Just get that castle down and i'm happy panda	2003	1	carloscnsz
2207	I promise you I'm good at this game!	2004	1	carloscnsz
2208	Why they put Regicide in the homemaps and not fortress? dautWat	2005	1	carloscnsz
2209	"normally you would need skirms in this mix, but I'm not normal"	2006	1	carloscnsz
2210	Now he thinks I got ballistics, but that was all micro	2007	1	carloscnsz
2211	I win the fight I lose the game	2008	1	carloscnsz
2212	yeah vill count doesnt look good... but subs count is nice! thx for the subs guys!	2009	1	carloscnsz
2213	Playing HC finals against fire would be my dream	2010	1	carloscnsz
2214	Who am I if I dont boom with 5 tc?	2011	1	carloscnsz
2215	I dont want to boom with only 4 TCs	2012	1	carloscnsz
2216	I mean i will go sarjeants cuz is really cool to go for the unique units	2013	1	carloscnsz
2217	They have a little bit of everything and a little bit of shit	2014	1	carloscnsz
2218	their unique unit is a villager that can build but not collect resources	2015	1	zekleinhammer
2219	HAHA!!! He overmicroed	2016	1	carloscnsz
2220	"It did start as a fun game!" Daut after his inca rush failed	2017	1	carloscnsz
2221	You cannot win if you don't believe!	2018	1	carloscnsz
2222	I get excited by stupid things and then i lose focus	2019	1	carloscnsz
2223	if I win HC4 I am thinking about retiring from competetive scene	2020	1	deagle2511
2224	DauT: "He has monk in a transport ship...he should jump out, convert villager, then put the villager in the transport ship and delete it. Must establish dominance"	2021	1	batbeetch
2225	"'Oh my gosh, Jordan is making tower on the gold, is that amazing move?' Yes, if you're stupid"  -Daut casting TheMax vs Jordan	2022	1	batbeetch
2226	Yeah but i want to destroy a bit more	2023	1	carloscnsz
2227	"A quick interview? With that loud fucker??" daut being asked for an interview after winning showmatch	2024	1	carloscnsz
2228	Well if jordan can do an interview after signing up for a tournament, I can do interview after winning showmatch	2025	1	carloscnsz
2229	Nili thinks that because i beated lierey and won RB he can improve as well	2026	1	carloscnsz
2230	I'm getting trolled by my own economy	2027	1	carloscnsz
2231	How much money did I won in aoe? Not enough man! NOT ENOUGH!!!	2028	1	carloscnsz
2232	You didn't even attack me! U just splash damaged the shit out of me	2029	1	carloscnsz
2233	Score is lying like in the last game! all lies	2030	1	carloscnsz
2234	Stop running away! Just die!	2031	1	carloscnsz
2235	"Actually i won't scout that! I want to be surprised in post imp finding a gold or something" Daut deliberately not scouting the back of his base in arena	2032	1	carloscnsz
2236	"I prefer to win with luck that winning without any luck" dautWat	2033	1	carloscnsz
2237	I'm not sure what i'm trying to quickwall here... apparently nothing	2034	1	carloscnsz
2238	Zek man, whatever you do with your punishment coins today is not respected!	2035	1	carloscnsz
2239	Can you chill man! is your smurf account! You are supposed to chill!	2036	1	carloscnsz
2240	NOOO I did so nice and then i did so bad	2037	1	carloscnsz
2241	don't do this at home	2038	1	zekleinhammer
2242	We shoot to miss!	2039	1	zekleinhammer
2244	"Is halbs the counter unit? I don't feel countered" daut patrolling paladins into halbs	2040	1	carloscnsz
2245	deagle is not important	2041	1	zekleinhammer
2246	the good thing is, nothing else can go wrong this game	2042	1	zekleinhammer
2247	Okay 26population is not what I have in mind for a comeback	2043	1	carloscnsz
2248	I want pretty things too	2044	1	carloscnsz
2249	I may feel a bit like a dirty sellout but we could do that	2045	1	carloscnsz
2250	"This nice guy is collecting all the relics for me" Daut spotting a enemy monastery in the middle of the arena	2046	1	carloscnsz
2251	Thanks you for the gifted subs and the gifted relic	2047	1	carloscnsz
2252	Don't wall your boar	2048	1	carloscnsz
2253	Give me that ding ding sound man!	2049	1	carloscnsz
2254	I imagine the castle will be there. If i imagine it, it will happen	2050	1	carloscnsz
2255	I didn't knew Celts was such a monk civ	2051	1	carloscnsz
2256	My units are expensive and my units are his units	2052	1	carloscnsz
2257	Even if this is the best strategy in the game... Feels shit to play	2053	1	carloscnsz
2258	Prize pools are now big enough i dont need u anymore! 11	2054	1	carloscnsz
2259	I did ok top 8, losing to yo, it’s good as long as you ignore Jordan beating yo	2055	1	zekleinhammer
2260	I'm so happy i lost to Yo	2056	1	carloscnsz
2261	I am proud to lose against yo	2057	1	zekleinhammer
2262	I micro like a beast when i see nothing	2058	1	carloscnsz
2263	that grandpa can dodge or what?!	2059	1	zekleinhammer
2265	let’s kill those healing brothers	2060	1	zekleinhammer
2266	man, the grandpas are not freaking dying!	2061	1	zekleinhammer
2267	He did really good wit... Wall micro? ... let's call it like that	2062	1	carloscnsz
2268	No football player will steal a boar of mine!	2063	1	carloscnsz
2269	I had the speed but the boar was mean to me	2064	1	carloscnsz
2270	Everything is a fail	2065	1	carloscnsz
2271	Football players are microing like me	2066	1	carloscnsz
2272	Meow Meow?	2067	1	carloscnsz
2273	My favorite color? It's used to be 5 but then i grew up and i dont care	2068	1	carloscnsz
2274	I'm showing the great stupidity there	2069	1	carloscnsz
2275	oh i forgot to micro them	2070	1	carloscnsz
2276	If you call me weird for play at this hour then look at the clock and look at the mirror	2071	1	carloscnsz
2277	When i started my stream and it was my job, i was thinking about my child and thinking i need to win tournaments to get viewers and then i saw T90 streaming low elo legends	2072	1	carloscnsz
2278	too much shit talk... I dont even know what i'm doing here	2073	1	carloscnsz
2279	Is my TC broken or something?	2074	1	carloscnsz
2280	And these players that dont understand how the game works! he wasn't supposed to do that	2075	1	carloscnsz
2281	He doesnt have units to follow this push... And i don't have houses. Everybody is missing something	2076	1	carloscnsz
2282	He gets my fish but i will get everything from him	2077	1	carloscnsz
2283	It's time for hoang! Get out daut, this is hoang!	2078	1	carloscnsz
2284	He will not be able to micro that forever	2079	1	carloscnsz
2285	"Soon enough i will be back to Europe" Daut playing form his home in europe	2080	1	carloscnsz
2286	DeadMatch is not for old people	2081	1	carloscnsz
2287	Don't walk through me!	2082	1	carloscnsz
2288	and nili... nili can follow the plan :)	2083	1	carloscnsz
2289	"He is playing cooler than me" daut beating sarjeants with cavaliers in sicilian war	2084	1	carloscnsz
2290	He dont notice	2085	1	carloscnsz
2291	Look at nili! He is good at deadmatch and he is destroying in random map	2086	1	carloscnsz
2292	You will give me 100 dollars if I beat u in dodgeball? Ok i¿ll give you one thousand if you beat me in random map... 1 million even	2087	1	carloscnsz
2293	You will give me 100 dollars if I beat u in dodgeball? Ok i'll give you one thousand if you beat me in random map... 1 million even	2088	1	carloscnsz
2294	He knows where i'm moving before i know	2089	1	carloscnsz
2295	I appreciate the 5 gifted subs but i don't deserve it	2090	1	carloscnsz
2296	Feudal boom? against hoang? sounds like suicide... Let's go!!!	2091	1	carloscnsz
2297	Why do i think viper lost intentionally to hera? well he has been losing for like 10 tournaments already, he likes to do that.	2092	1	carloscnsz
2299	I wanted perfect angle so every arrow hit him in the ass but I failed... His ass is safe	2093	1	carloscnsz
2300	I dont need to go for the kill, i can just boom and win. Obviously i'm going for the kill	2094	1	carloscnsz
2301	I lost slamboy... could farm him like vinchester did	2095	1	carloscnsz
2302	score could be lying. I dont think it is, but it could be	2096	1	batbeetch
2303	I just happend to not be the smartest guy ever	2097	1	carloscnsz
2304	5 famrs? yeah 5 too many if you ask me!	2098	1	carloscnsz
2305	i believe in you ninja monk	2099	1	carloscnsz
2306	at least im losing with style! no one can deny that	2100	1	carloscnsz
2307	I'm just kidding, jonslow is an amazing guy... but dont sub to him	2101	1	carloscnsz
2308	What? catafracts man! You are cats!	2102	1	carloscnsz
2309	Risky moves always bring the fun games	2103	1	carloscnsz
2310	if I lost to Jordan. goodbye streaming. goodbye life.	2104	1	yuna_op
2311	If he doesn't do economy upgrades why should do I ?	2105	1	carloscnsz
2312	I'm not trying to lose, I'm trying to win but the strategies i use are a bit questionable	2106	1	carloscnsz
2313	Why you kill me and I don't kill you	2107	1	carloscnsz
2314	I think I'm negative five right now	2108	1	carloscnsz
2315	it looks really bright? as my future	2109	1	yuna_op
2316	I wish i could blame the chat for this bad strategy... but is all my bad playing	2110	1	carloscnsz
2317	Tatoh started his stream with the title "we are playing this tournament" Little does he know i will be late	2111	1	carloscnsz
2318	I'm disrespecting the tournament as hera... i dont know if is 1 rhino or 2	2112	1	carloscnsz
2319	at least I tried to micro	2113	1	zekleinhammer
2320	looks like he sniped something. his chances to win the game	2114	1	deagle2511
2321	Sometimes you are lucky and sometimes you are not lierey	2115	1	carloscnsz
2322	You wanna play it like that? you gonna get one in the face!	2116	1	carloscnsz
2323	luckily I boom like crazy person	2117	1	carloscnsz
2324	"Idea was decent nobody can say that" dautWat	2118	1	carloscnsz
2325	everybody beats Yo. Except one guy, one dautBaldy guy	2119	1	zekleinhammer
2326	nice mangonel shot? shut up! We don't talk about that here	2120	1	carloscnsz
2327	When those turn into crossbows i will turn inot a dead boy	2121	1	carloscnsz
2328	I want to take this fight! I don't care if I lose	2122	1	zekleinhammer
2329	Nili is going to carry... our drinks	2123	1	carloscnsz
2330	Babies cry less and less, they are like kids now	2124	1	carloscnsz
2331	Any food is a good food	2125	1	carloscnsz
2332	"Do you want me to teach you how to play?" Daut to viper during TGs	2126	1	carloscnsz
2333	"that's not being dumb, that's being an idiot"	2127	1	artofthetroll
2334	"I prefer to lose than be toyed with"	2128	1	artofthetroll
2335	“Daut what is your pop?”... “the pop is gg”	2129	1	batbeetch
2336	I don't like deleting my children	2130	1	carloscnsz
2337	Nicov man! click on that link! play to level ten! And make me rich!!!	2131	1	carloscnsz
2338	I had to play showmatch vs Laaan and then fire and they both didn't show up nor sent anything telling they won't show up. they just ignored everything	2132	1	carloscnsz
2339	Imagine the level of people don't giving a shit	2133	1	carloscnsz
2340	"Oh i appreciate that new raider" daut selling out his Raid Shadow Legends sponsor	2134	1	carloscnsz
2341	This is my channel so is all about of me	2135	1	carloscnsz
2342	500euro coaching was booked and i don't really know how to deliver 500euro coaching 11 I may have to rpepare something even	2136	1	carloscnsz
2411	global warming hit ages of empires 2, man!	2205	1	zekleinhammer
2343	Probably viper used his 10th place prize to buy my 500euro coaching. Is an investment for his future	2137	1	carloscnsz
2344	30 minutes? but you are trying to enjoy the game, that's not the point here (DauT sellout)	2138	1	deagle2511
2345	Do I ever make mistake?	2139	1	carloscnsz
2346	I will get a bit of help from my friend market	2140	1	carloscnsz
2347	3 stables knights? Nahh that's good for vikings. not for berbers. with berbers I'll go crossbow	2141	1	carloscnsz
2348	villagers can be replaced, markets not	2142	1	carloscnsz
2349	This is one blind ass fucker	2143	1	carloscnsz
2350	The only desicion i need to make is who to sacrifice	2144	1	carloscnsz
2351	That's my way of the winning. Sacrifice people	2145	1	carloscnsz
2352	walls-wise he is good	2146	1	carloscnsz
2353	orange color is dangerous one	2147	1	carloscnsz
2354	I think T90 is the closest thing we have to a professional caster	2148	1	carloscnsz
2355	I will leave the hole open	2149	1	carloscnsz
2356	Oh he is passing in...	2150	1	carloscnsz
2357	I will hoang him out of the game so hard he will uninstall the game after this	2151	1	carloscnsz
2358	Give me that taste of market	2152	1	carloscnsz
2359	clowns are scared of archers, we know that	2153	1	carloscnsz
2360	Should i pick all the tower rush civs? tower rush him back to the voobly	2154	1	carloscnsz
2361	Come here with your smelly archers!	2155	1	carloscnsz
2362	Apparently I don't know how to convert	2156	1	carloscnsz
2363	where should i send my beautiful army of nothing ?	2157	1	carloscnsz
2364	I'm done defending you guys	2158	1	carloscnsz
2365	"You defend me and I complain" daut discussing the game plans at the start of the TG	2159	1	carloscnsz
2366	"You can tell the difference when jordan and nili are not here? this is like the opposite of losing"	2160	1	carloscnsz
2367	I was playing like a butterfly, all over the place	2161	1	carloscnsz
2368	slam is an amazing player, especially in tournaments	2162	1	carloscnsz
2369	I read it and i know what i want to answer but i had to type it and got lazy... ill do it later. maybe tonight	2163	1	carloscnsz
2370	Wait?! Your army is 2 skirmishers!!	2164	1	carloscnsz
2371	"Look at him!  Who is Lyx now??" Daut donjon rushing lyx	2165	1	carloscnsz
2372	I will not host anybody, no one deserves the host	2166	1	carloscnsz
2373	One day, when i get all the upgrades, this will be a beautiful game	2167	1	carloscnsz
2374	Damn, I am stupid.	2168	1	artofthetroll
2375	Units lives matter	2169	1	carloscnsz
2376	for 500 euros an hour I can show you how to lure a boar	2170	1	yuna_op
2377	I am not going to do that, I am just saying what I should have done	2171	1	zekleinhammer
2378	If he sees this... nah, he is like fire, tunnel vision boys	2172	1	carloscnsz
2379	Play arena and shut up!	2173	1	carloscnsz
2380	he! "britons are not the best civ here" you guys are clueless	2174	1	carloscnsz
2381	Walling him in with castles is basically my plan here	2175	1	carloscnsz
2382	If luck was no fun nobody would gamble	2176	1	batbeetch
2383	is he going scouts!? oh thats my scout	2177	1	batbeetch
2384	if you are missing 5 food to click up you should be allowed to click up	2178	1	zekleinhammer
2385	double walled for double the pleasure	2179	1	zekleinhammer
2386	don't make a farm when people are dying!	2180	1	batbeetch
2387	Not everything is good from behind	2181	1	zekleinhammer
2388	win only in tournaments? No, I dont win even in tournaments	2182	1	batbeetch
2389	There's no limit to my micro	2183	1	batbeetch
2390	I have two options... Either to go for the water play, or basically eagles... I dont think plumes would do anything. But you know what guys? I'll go for plumes. Why? I like them	2184	1	batbeetch
2391	Being stupid is the new smart	2185	1	batbeetch
2392	Don't they teach you how to micro in the Starcraft?	2186	1	batbeetch
2393	if i wanted an 8hr job i would not be a streamer	2187	1	batbeetch
2394	he's going full Game of Thrones on me	2188	1	batbeetch
2395	I'm a BAD BAD player! I deserve everything coming to me!	2189	1	zekleinhammer
2396	Missing one relic... I want all the pokemons!	2190	1	carloscnsz
2397	Other way to call him stupid: Misjudged	2191	1	carloscnsz
2398	kiss me you beautiful beast!	2192	1	zekleinhammer
2399	"Jordan congratz on 2nd place!" DauT after beating Jordan in Bo21	2193	1	carloscnsz
2400	it was hard to play mind games with jordi, he doesn't have mind	2194	1	zekleinhammer
2401	Is hard to play mind games against jordan. he doesnt have mind	2195	1	carloscnsz
2402	I'm just competing in tournaments, streaming is my side job	2196	1	carloscnsz
2403	Man! he is surviving left and right	2197	1	carloscnsz
2404	That's why they teach you in the freaking starcraft??!!!	2198	1	carloscnsz
2405	The plan did not working according to the plan	2199	1	carloscnsz
2406	"ok maybe i need units and stuff like that"	2200	1	artofthetroll
2407	Give me your nili joke!	2201	1	carloscnsz
2408	"Now you know how slam feels!" DauT after carrying jordan for a 3-0 in 2v2 showmatch	2202	1	carloscnsz
2409	Let's dance the Tango with the mango!	2203	1	carloscnsz
2410	I do not share!	2204	1	carloscnsz
2412	Come on hello kitty! Get them!	2206	1	carloscnsz
2413	I can do a lot of pretty things...  i dont know which is the prettiest	2207	1	carloscnsz
2414	“I will win the fight and the game? nononono, I want to play more”	2208	1	zekleinhammer
2415	If you think this is enough TCs, you are wrong	2209	1	carloscnsz
2416	Is GL B and B stands for nili	2210	1	carloscnsz
2417	Never saw you guys lame as much as on this tournament were laming is not allowed	2211	1	carloscnsz
2418	I got lamed by the boar... does that count?	2212	1	carloscnsz
2419	I cannot go! I'm still laughing	2213	1	carloscnsz
2420	nili is playing deadmatch from dark age	2214	1	carloscnsz
2421	What's Nili's homemap? Who cares he is not going to win anyway	2215	1	carloscnsz
2422	Is not like when jordan lose a tournament. that nobody cares	2216	1	carloscnsz
2423	We need triple elimination for Nili	2217	1	carloscnsz
2424	He is not under pressure man, his pressure is his speed	2218	1	carloscnsz
2425	"Well at least you have deathmatch" Daut to nili	2219	1	carloscnsz
2426	And all change completely when slam did that micro I have been teaching him for all these years	2220	1	carloscnsz
2427	Everyone learned this game from watching my recorded games	2221	1	carloscnsz
2428	"OHHH!!! I cannot look! Wake me up when it is over!" Daut casting nili	2222	1	carloscnsz
2429	Goodbye chair, goodbye slam!	2223	1	carloscnsz
2430	When s the giveaway? You can take nili! I give him away	2224	1	carloscnsz
2431	Wanna micro young man?	2225	1	carloscnsz
2432	Man! The hill is freaking saracens	2226	1	carloscnsz
2433	ohhh he is going to clown me down	2227	1	carloscnsz
2434	I remember I told him he doesnt know how to play.. but i tell him that everyday tho	2228	1	carloscnsz
2435	I don't want to be yelled at	2229	1	deagle2511
2436	no stone walls allowed? But we made fortified walls!	2230	1	zekleinhammer
2437	Ohh mangonel micro tatoh I need you!... *wins fight* Haha! I dont need you	2231	1	batbeetch
2438	*Daut using monks and mangos* do we have second building?	2232	1	batbeetch
2439	I'm hearing dying sounds... And i think those are my villagers	2233	1	carloscnsz
2440	Is there anything with more than 0hp there?	2234	1	carloscnsz
2441	I'm not dancer, I'm player	2235	1	carloscnsz
2442	well im good, im old so this will be my cup	2236	1	carloscnsz
2443	stop micronerding? no man! i learned that!	2237	1	carloscnsz
2444	No need to throw this game	2238	1	carloscnsz
2445	Never micro is what you need to do.. or not to do	2239	1	carloscnsz
2446	Actually i was housed so i had to fight. True fact!	2240	1	carloscnsz
2447	"Is really bad for me to move out now" Daut while moving out with his army anyway	2241	1	carloscnsz
2448	"Look at him! So old and yet happy" Daut watching memb's stream	2242	1	carloscnsz
2449	I am a Jordan today?	2243	1	carloscnsz
2450	Doesn't feel good to be a Jordan	2244	1	deagle2511
2451	If market did not exist i would be 1k5	2245	1	carloscnsz
2452	"I almost wasted resources on fletching" daut selling everything to go castle age without army	2246	1	carloscnsz
2453	Those fuckers are like "we nerf market, we nerf market" ... NO YOU DONT NERF MARKET!!!	2247	1	carloscnsz
2454	And Aftermath lost to this? they will loe again	2248	1	carloscnsz
2455	Onagers go BAM	2249	1	carloscnsz
2456	Should i go halbs or  not halbs	2250	1	carloscnsz
2457	I signed up for easy money but is not going that way...	2251	1	carloscnsz
2458	I went there for a villager war and they didn't show up	2252	1	carloscnsz
2459	I also don't know what i'm doing, i'm exited to see it as well	2253	1	carloscnsz
2460	I want them to see it man, I want them to look to the death on the eyes	2254	1	carloscnsz
2461	It's all about perspective. It depends on what side of the wall you are	2255	1	carloscnsz
2462	We should tell them to alt F4 cuz we sneak so they know we sneak but they dont see where	2256	1	carloscnsz
2463	Anybody else wants to go fast imp with us?	2257	1	carloscnsz
2464	to nili: I have a wood for you!	2258	1	zekleinhammer
2465	I'm a scary guy	2259	1	carloscnsz
2466	Nili is my pond!	2260	1	byelo
2467	he says elephants are overrated and he made only 1	2261	1	carloscnsz
2468	what can go wrong when I play like nili?	2262	1	zekleinhammer
2469	I am scared and confused at the same time	2263	1	zekleinhammer
2470	I lost so many units, that’s what happens when his units counter mine	2264	1	zekleinhammer
2471	Monks are a good thing, they take relics they... ?? .. best unit in the game	2265	1	carloscnsz
2472	Getting to finals is not special anymore, even jordan did it	2266	1	carloscnsz
2473	Don't try this at home, this requires a lot of apm	2267	1	carloscnsz
2474	don't try this at home. this requires a lot of APM	2268	1	yuna_op
2475	I'm capable of being smart sometimes	2269	1	carloscnsz
2476	This one is dead, or this one is dead! ... no one is dead	2270	1	carloscnsz
2477	where is your converting boy?	2271	1	carloscnsz
2478	if you cannot beat them with army, you boom!	2272	1	yuna_op
2479	Entirely blame this one on chat	2273	1	carloscnsz
2480	Let me click up and then we fight again	2274	1	carloscnsz
2481	i dive in, i dont care	2275	1	carloscnsz
2482	"Wait! I know what's my economy upgrade!" *Builds a market	2276	1	carloscnsz
2483	Relic goes YOINK https://clips.twitch.tv/HealthyColdPonyKreygasm-n24nocHPjFqs0HP0	2277	1	yuna_op
2484	even with all those clicking I cannot get over 300, screw that back to the chill mode https://clips.twitch.tv/UnsightlyPreciousSrirachaSSSsss-lPq8ngV5VbgqLzUw	2278	1	yuna_op
2485	that one is going to YouTube just because of my APM!	2279	1	yuna_op
2487	Yeah miguel is around but he lost red bull qualifiers so he doesnt deserve a host	2280	1	carloscnsz
2488	I'm starting to believe that full cavalry is not the right play against full camels	2281	1	carloscnsz
2489	I will outlast lierey as well, not only viper	2282	1	carloscnsz
2490	I have a tendency to play stupid games	2283	1	carloscnsz
2491	if i could micro like a boar i would be unstoppable	2284	1	yuna_op
2492	Who will i play in the finals? well not tatoh or slam	2285	1	carloscnsz
2493	The villagers keep screaming in my economy	2286	1	carloscnsz
2494	i went ZUUUM! and he went zum... I guess my "zum was higher	2287	1	carloscnsz
2495	Those settings are good... although i'm failing on that tournament miserably... yeah those settings are bad then	2288	1	carloscnsz
2496	don't cheat man! That was a test!	2289	1	carloscnsz
2497	I think relics should tell me where he is? ... I have no clue where he is	2290	1	carloscnsz
2498	There is always a bigger hill	2291	1	carloscnsz
2499	Even slam should be able to win from this spot	2292	1	carloscnsz
2500	report me to twitch? For being awesome? Go ahead	2293	1	zekleinhammer
2501	I will actually google it after the stream	2294	1	carloscnsz
2502	He wants a motherfucker win now	2295	1	carloscnsz
2503	Can you say monkas? oh noo i said it.. fuck... i feel like viper now.. i shouldn't read that i was in autopilot	2296	1	carloscnsz
2504	"oh he is housed!! he is housed!!" *fails to kill the vill* "Oh.. i wish i would have killed that one. then he wouldn't be housed anymore"	2297	1	carloscnsz
2505	"if viper has the dog camera and fire as well, i can have the medal camera" Daut talking about having webcam pointing only to his red bull medal	2298	1	carloscnsz
2506	"Traitor! nice, short and true" Daut after hyuna asked for a new name	2299	1	carloscnsz
2507	I saw the dream team thingy on my discord, i messaged hera and lierey and we sign up for it	2300	1	carloscnsz
2508	I got quickwalled by the freaking boar	2301	1	zekleinhammer
2509	Hopefully no monks... There is always a monk	2302	1	carloscnsz
2510	Why I'm so amazing? I have hair	2303	1	carloscnsz
2511	"I kill you, you dont kill me, that's the game we're playing"	2304	1	carloscnsz
2512	I think i can attack the lame way	2305	1	carloscnsz
2513	hmm he is full of good moves	2306	1	carloscnsz
2514	If I was a castle where do i wanted to be	2307	1	carloscnsz
2515	Oh a TC there? I would like a castle there	2308	1	carloscnsz
2516	Haha! getting out-microed by DauT!	2309	1	batbeetch
2517	"if i picked Chinese, i would have more villagers than I have right now"	2310	1	artofthetroll
2518	I was doing fine until you micro back! I hate when people micro back !	2311	1	yuna_op
2519	"Let's see if it's Serbian thing to micro badly or just a me thing." DauT, facing Big Don Bepis on Clown Arena.	2312	1	byelo
2520	Jordan, man! It was funny when I lamed, not when I get lamed!	2313	1	zekleinhammer
2521	nothing to see here! I'm failing. - DauT when his stream is dropping frames and losing	2314	1	yuna_op
2522	time to call it, my frames are dropping, I am dropping, everything is dropping!	2315	1	yuna_op
2523	5 relics, thank you for collecting! dautKotd	2316	1	deagle2511
2524	I'm playing against some beast here!	2317	1	artofthetroll
2525	afk? who cares, its just jordy	2318	1	hyunaop
2526	my real job? I don't even know my real job yet!	2319	1	hyunaop
2527	he think he can beat me in mangonel war? I guess he's right	2320	1	hyunaop
2528	Vikings are fricking Vikings	2321	1	artofthetroll
2529	memb is playing, I will not forward you to the memb	2322	1	zekleinhammer
2530	well, Viper is also going bald but nobody cares about that	2323	1	deagle2511
2531	I live for stupid fights	2324	1	hyunaop
2532	if it is 5 tc it’s a good decision	2325	1	zekleinhammer
2533	let the clowns study this build, thinking I did it on purpose	2326	1	zekleinhammer
2534	I don’t like to look in the mirror. I don’t like that man	2327	1	zekleinhammer
2535	I think any map is my home map	2328	1	zekleinhammer
2536	Friggen "Fabulo" man	2329	1	artofthetroll
2537	this is the classic cup and we cast the classic way	2330	1	zekleinhammer
2538	Aztecs are like crossbow	2331	1	zekleinhammer
2539	Jordan is asking for advice, I can screw him over	2332	1	zekleinhammer
2540	I want to be Lierrey too. I never will be Lierrey	2333	1	deagle2511
2541	cmon it's slam man. he is probably in ER man, trying to get his heart working again!	2334	1	hyunaop
2542	GL finals. Been a while dautKotd	2335	1	deagle2511
2543	I am more than stupid	2336	1	batbeetch
2544	let's do the one thing I know to do	2337	1	deagle2511
2545	Interview "daut, did you prepare?"  I prepare shisha, does that count?	2338	1	deagle2511
2546	I realized something about myself, I feel better when I’m winning	2339	1	zekleinhammer
2678	Another Day, Another Fail	2406	1	artofthetroll
2547	don't blame the player, blame the game. Well maybe blame the player as well	2340	1	hyunaop
2548	if this was a tournament game I would have to pay the damage for the breaking of the computer	2341	1	zekleinhammer
2549	I like viper’s body	2342	1	zekleinhammer
2551	"Yeah gbettos are very situational, and in every situation they are bad" https://clips.twitch.tv/HeadstrongRamshackleAsparagusGingerPower-aiCz9xQUFoj4AW5Q	2343	1	artofthetroll
2552	When I start massing lancers... that's it... I am massing lancers.	2344	1	artofthetroll
2553	I use the market when I sleep	2345	1	deagle2511
2554	everytime I met lierrey he is 16 years old	2346	1	zekleinhammer
2555	oooh, my proud little moment just went to shit	2347	1	zekleinhammer
2556	I’ve seen some bams in my life and that was a good bam	2348	1	zekleinhammer
2557	those people that raid are the worst….. let’s raid!	2349	1	zekleinhammer
2558	sorry I dont know pokemon	2350	1	hyunaop
2559	so magikarp is a pokemon? you learn something useless everyday	2351	1	hyunaop
2560	monks should need redemption to convert elephants	2352	1	zekleinhammer
2561	"He's gonna wall you in... He walled you in. This is a true zoo" -Daut casting Deagle's walled in Elephantos	2353	1	batbeetch
2562	How are you not getting converted there? I would get so converted here	2354	1	batbeetch
2563	this is what happens when you watch deagle too much	2355	1	zekleinhammer
2564	if i start winning more people are gonna give up on this game	2356	1	batbeetch
2565	I am taking dock spot here, go away as far as possible from me	2357	1	zekleinhammer
2566	deagle get out	2358	1	batbeetch
2567	I go to sleep at 4am like a normal person	2359	1	zekleinhammer
2600	guys wanna see something cool? *fails to trap enemy scout* well not in this stream	2360	1	batbeetch
2601	without memb, there are no amigos	2361	1	zekleinhammer
2602	surprise, muthafucka!	2362	1	zekleinhammer
2603	it’s all fun and games until it stops being fun and games	2363	1	zekleinhammer
2604	I put Serbia on the map	2364	1	zekleinhammer
2605	He won't finish, and even if he finish, it won't last long., Okay he finish and it last long.	2365	1	sharkfins0up
2606	This world makes no sense.	2366	1	synapse16
2607	nobody saw that, or at least I didn’t, at least one person didn’t see that	2367	1	zekleinhammer
2608	who cares about the hit and run? Just hit and hit	2368	1	zekleinhammer
2609	AND THAT IS HOW YOU PLAY THIS GAME!	2369	1	artofthetroll
2610	One day, I'll learn the game of Age of Empires.	2370	1	synapse16
2611	it’s important to show the class even when you’re getting wrecked	2371	1	zekleinhammer
2612	Don't put your junk on me man	2372	1	woootman_
2613	blacksmith! That’s why he is winning…screw the blacksmith let’s go up	2373	1	zekleinhammer
2614	Show me your junk.	2374	1	woootman_
2615	Don't judge me, I'm a market abuser.	2375	1	woootman_
2616	Guys! Always remember to protect and upgrade your junk	2376	1	goldeneye_
2617	I feel like Bambi on the ice playing this game	2377	1	zekleinhammer
2618	*loses monk* I love that guy, he was like a father to me	2378	1	woootman_
2619	woot, man they don’t need to know that I’m an idiot!	2379	1	zekleinhammer
2620	justice for the no knight civs	2380	1	zekleinhammer
2621	man the Khan is dominating my ass	2381	1	zekleinhammer
2622	I don’t want to play galley war into demos into killing myself	2382	1	zekleinhammer
2623	hyuna: I just shared a useless fact. daut: no fact is useless to me	2383	1	hyunaop
2624	Daut castles left and right!	2384	1	artofthetroll
2625	never do the Math on stream.	2385	1	synapse16
2626	"You will drop where I tell you to drop, or die trying"	2386	1	woootman_
2627	Holy Roman Empire without relics will not be so holy	2387	1	zekleinhammer
2628	I am a book to him	2388	1	zekleinhammer
2629	Nobody calls me a chicken anymore	2389	1	artofthetroll
2630	lying is nice when people don't check, well they can but they don't bother (DauT lying to his family about his job)	2390	1	hyunaop
2631	guys never be nice, nice doesn't pay man	2391	1	hyunaop
2632	Tatoh... don't talk to me	2392	1	batbeetch
2633	if I do Alt F4 I get banned... I dont want to get banned! I do this for a living!	2393	1	batbeetch
2634	ohhh I lost so many houses now!... luckily Im losing villagers so its good	2394	1	batbeetch
2635	Jordan Jordan! this is the most important thing in my life *attacks ground 2 petards*	2395	1	batbeetch
2636	mamelukes are shit	2396	1	batbeetch
2637	oh I got outmicroed here. also I got outstupid here	2397	1	hyunaop
2638	If you are housed, you take the fight. thats the rule, basic age of empires.	2398	1	harooooo1
2639	That stable says that he is up. I don't like when stable is talking.	2399	1	harooooo1
2640	"i know you guys all love slam, but he's just not there yet. just not there. he is younger than me, he's got time"	2400	1	artofthetroll
2673	I don’t want to play blaming game but it’s viper’s fault	2401	1	zekleinhammer
2674	"estupido!" -Daut to himself after hitting a boar with his scout	2402	1	batbeetch
2675	Im a GL player after all... if I know anything is how to hide behind my ally	2403	1	batbeetch
2676	get the fuck out of my game! -pause- shut up guys	2404	1	deagle2511
2677	I have wheelbarrow, bitch! Let’s go!	2405	1	zekleinhammer
2679	1 villager on gold is better than no villagers on gold	2407	1	zekleinhammer
2680	"Cannot focus with those d**** around the TCs". DauT forgetting to enable small tree mod after patch.	2408	1	synapse16
2681	can you ascu the sheep? can you ascu the sheep?	2409	1	zekleinhammer
2682	there is a few mysteries in this life, but the f1re being in top ten constantly is the biggest one	2410	1	zekleinhammer
2683	"Hussars!? That's so last year"	2411	1	woootman_
2684	You're fat, you're cool. you're deadly	2412	1	deagle2511
\.

SELECT pg_catalog.setval('public.quotes_id_seq', 2684, true);
