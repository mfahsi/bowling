# Bowling Score API

## Table of Contents

- [Assumptions](#assumptions)
- [Data Model](#data-model)
- [Testing](#testing)
- [Procedures](#procedures)
- [API Documentation](#api-documentation)
- [Sample cURL commands and Responses](#sample-curl-commands-and-responses)

## Get Started
- git clone git@bitbucket.org:winov/bowling.git


## Assumptions

- Group games are not handled by this version but we did provide a simplified data model and some design elements to illustrate how we can easily add group games.
  the calculation of "who's next" in multi-player end of game will require an additional end point or can be added to response of roll end point.
- The API provided does support markers like '-' 'S' '/' 'X' 'F', but no sophisticated display is provided. The framesScores function will return these flags along with detailed score of each frame.
- the frameScores and roll functions will infer missing markers 'X' and '-'. typically if it is not a foul and is 0 then it is '-'. 
- The cumulative score is not provided by frameSores. but Bonus is applied. Cumulative can be done as well, i prefered to keep non cumulative scores for it's ease of testing. the clients can do cumulation themselves.


## Data Model

### Database Schema

The bowling game application uses two main tables to store game information:

1. `BowlingGroupGame`: Represents a group of games played together on a specific date and lane.
2. `Game`: Represents individual bowling games, including information about the player, lane, rolls, and associated group.

The schema for these tables is as follows:

#### BowlingGroupGame

```sql
CREATE TABLE IF NOT EXISTS BowlingGroupGame (
    id serial NOT NULL PRIMARY KEY,
    players SMALLINT NULL,
    gameSetDate DATE NULL,
    lane SMALLINT NULL
);

id: Primary key for the group game.
players: Number of players in the group.
gameSetDate: Date when the group of games was played.
lane: Lane number where the group of games was played.
```
#### Game
```sql
CREATE TABLE IF NOT EXISTS Game (
    id serial NOT NULL PRIMARY KEY,
    frames JSON NULL,  //TEXT for H2
    player TEXT NULL,
    lane   TEXT NULL,
    gameDate DATE NULL,
    gameSetId integer NULL REFERENCES BowlingGroupGame(id) ON DELETE CASCADE
);

id: Primary key for the individual game.
frames: JSON representation of the frames in the game.
player: Name of the player.
lane: Lane number where the game was played.
gameDate: Date when the game was played.
gameSetId: Foreign key referencing the associated BowlingGroupGame (if applicable).
```


### Notes
- The system supports production persistence in either Postgres or H2. The H2 schema use TEXT instead of JSON type to store frames. 
- Postgres tables are created with boot of Postgres service in the docker container.
- H2 tables are created when The Repository instance is created. when using H2 the Postgres service does not need to be started when running on H2. 
- Repository Tests do not cover Postgres specific parts in this version. It is possible to add postgres tests based on containers test framework.
  or just push postgres queries to configuration to improve the coverage.
  this version have a test weaknesss around Postgres repository implementation, so need manual tests of update and create methods. 
- most of fields of Game and group game level are created for illustration purpose only to show how the design can evolve to manage multiplayer games. 
  for this version these fields are nullable and not used.
  
## Testing
- coverage is generated in target/coverage. minimum set for build is 80%.
- Coverage is over 90% for most packages and uses H2 persistence where needed.
- coverage on postgres part of persistence package is weak (41% only for postgres), externalizing queries or using containerized postgres tests need a good amount of time to do.
  we keep this in the TODO list.
- testing with a couple of curl commandes is a suggested procedure for postgres (in this version).
- testing run in sequence to keep logs easy to read. we can change this if there is pressure on build times.


## Procedures
#### Build
- build : sbt docker:publishLocal
- test coverage : sbt clean coverage test coverageReport
- results of test coverage are in <project>/target/coverage/scoverage-report/index.html

#### Production Run
- API runs on port 8086 and Postgres on 5432
- run production on Postgres : docker-compose up &                         // postgres & bowling containers
- run production on H2       : STORAGE_DB=memory docker-compose up bowling //bowling container only
##### Troubleshooting 
- check if port 5432 is in use (before running the app) : sudo lsof -i :5432
  

#### Useful commands
- to delete postgres data base : rm -r <project root>/postgres-data/   //may need sudo
- to stop containers : docker-compose down 
- to check containers : docker ps

Note : docker-compose file is at root of project.

## API Documentation

see Yaml file at root folder. and the section : Sample CURL commands and Responses.

## Design Elements
#### Data Model
- We did not see any benefit on tables such as Frame or Roll with current requirement. The access will always be on the game level except for rolling, but even then we need other rolls anyway.
- A single game table with frames composed to it is the solution chosen.
- Postgres supports Json type, we use that for frames in case we want to do some indexing later.
- Object size is small so no issue working at game level only (the cost of joining would be worse).
- We could store game data without Frame level, just a List of Rolls in database, but we opted for Frame/Roll. both options are ok, as inferring frames from rolls is doable and fast enough.

#### Scoring
- In order to implement scoring, we did split the concept into bonus points and main points.
Each roll have main points and have bonus points it generates and transfers to previous rolls.
- We made a decision to reverse the frames and remove the frame layer for ease of computation.
the 10th frame is special : it has bonus rolls that generate only bonus points, some code needed to deal with this special rule.
example : in 10 th frame Frame X X X   => 10 main points and 20 bonus.
          3 frames             X, X, X => 30 main points and 30 bonus. 
- The solution support markers X / - S F ..etc. some markers are inferred from pins, other like F and S must be provided as inputs.
- The solution offer Show methodes to display games in friendly way (use markers instead of pins where relevant)

#### Performance
- The data model favors performance already by opting for a single table for game.
- there is no bulk loads or searches required.
- expected volumes are supposed to be in hundred or millions of games only (per year) and only unitary access via primary key.
- if needed caching of current day games or pending games would be recommended. but the current solution is fine without it.
- a cache of find game by memoization should reduce the reads to the dabase, typically 10 rolls per game is a chance to save 10 reads by keeping active games in memory.
- No performance metrics are in place (recommanded)
- No performance tests were conducted (recommanded before production release).

### Security : 
- API is not secured in this solution, it is a must do in real production. solutions exist to add a security middlware to the API. and of course passwords/ secret mechanism is needed or if some authentication service exists it can be used.

### Resilience
- In case of failures, clients may retry the same calls if they were disconnected while the response is being pushed or not pushed yet.
- The API need to be Idempotent and allow retries without inserting duplicates, we have added a Unique id to Rolls that clients need to set.
  server need to check if the same roll have been persisted or not and fail attempted duplication.
- The above is designed for but not implemented in this version. Current version will insert duplicates.
- Also this version does not have much data validation, it is recommended to add validation of rolls and frames to ensure persisted data is never corrupt..

## Sample CURL commands and Responses

#### Create a game

```bash

curl -X POST -H "Content-Type: application/json" -d '{"uid": "123", "game": {"player": "John", "lane":"3", "frames":[]}}' http://localhost:8086/api/game
```
Response sample :
```json
1
```
#### Get the game
```bash
curl -X GET http://localhost:8086/api/game/1
```
Response:
```json
{"id":1,"frames":[],"player":"John","lane":"3"}
```
#### Get a nonexistent game
```bash
curl -X GET http://localhost:8086/api/game/9999
```
Response :
Empty Body, Status NotFound 404

#### Update the game (Not required by specs but needed to implement ROLL)
```bash
curl -X PUT -H "Content-Type: application/json" -d '{"player": "Jane", "lane":"3", "frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0, "marker":"F"}]}]}' http://localhost:8086/api/game/1
```
Response:
```json
{"id":1,"frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0,"marker":"F"}]}],"player":"Jane","lane":"3"}
```
#### Roll (Miss with inference of marker)
```bash
curl -X PUT -H "Content-Type: application/json" -d '{"pins": 0, "marker":"-"}' http://localhost:8086/api/game/1/roll
OR
curl -X PUT -H "Content-Type: application/json" -d '{"pins": 0}' http://localhost:8086/api/game/1/roll
```
Response`
```json
{"id":1,"frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0,"marker":"F"}]},{"rolls":[{"pins":0,"marker":"-"}]}],"player":"John","lane":"3"}
```
#### Roll (with Strike inference)
```bash
curl -X PUT -H "Content-Type: application/json" -d '{"pins": 10}' http://localhost:8086/api/game/1/roll
````
Response:
```json
{"id":1,"frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0,"marker":"F"}]},{"rolls":[{"pins":0,"marker":"-"},{"pins":10,"marker":"X"}]}],"player":"John","lane":"3"}
```
#### Roll on Game Already complete
```bash
Repeat until reaching 10 frames: curl -X PUT -H "Content-Type: application/json" -d '{"pins": 10}' http://localhost:8086/api/game/1/roll
```
Response : Illegal State, Empty body with status 

#### Get final score // this fails if game is incomplete. there is an other unexposed function to get frame per frame score for incomplete games as well.
curl -X GET http://localhost:8086/api/game/1/score
Response:
```json
{"error":"Can't provide a final score a pending game","ref":"1"}
```
#### Get final score 
160
#### Delete the game
curl -X DELETE http://localhost:8086/api/game/1

### TODOs
- docker is using host networking mode which does not provide good isolation. TODO change to Bridge mode.
- caching (as discussed in design)
- performance (as discussed in design)
- validation (as discussed in design)
- testing improvement of postgres