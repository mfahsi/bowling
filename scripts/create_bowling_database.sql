CREATE TABLE IF NOT EXISTS BowlingGroupGame (
	id serial NOT NULL PRIMARY KEY,
	players SMALLINT NULL,
	gameSetDate DATE NULL,
	lane SMALLINT NULL
);

CREATE TABLE IF NOT EXISTS Game (
	id serial NOT NULL PRIMARY KEY,
	frames JSON NULL,
	player TEXT NULL,
	lane   TEXT NULL,
    gameDate DATE NULL,
	gameSetId integer NULL REFERENCES BowlingGroupGame(id) ON DELETE CASCADE
);

