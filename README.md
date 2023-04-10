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
- 

## Assumptions

- Group games are not handled by this version but we will provide a simplified data model and design to illustrate how we can easily add group games.
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

#### Game```
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
- The support production in either Postgres or H2, the H2 model have frames Field as Text instead of Json.
- Postges tables are created with boot of Postgres service in the container.
- H2 tables have are created when The Repository instance is created. when using H2 the Postgres service does not need to be started. 
- Tests support only H2 in this version. it is possible to add postgres tests based on containers test framework.
   with this version a minimum of manual tests is needed to ensure Postgres Repository is fully validated.
   this version have a test weaknesss around Postgres repository implementation. 
- most of fields and group level are created for illustration purpose on how the design can evolve to manage multiplayer games. for this version fields are nullable and not used.
- update function (not requested) updates only frames (should update all game data normally)   
## Testing

- all non mocked tests use H2 database if persistence is needed by the test scenario.
- coverage may not be very complete, specially for postgres. a TODO is to add containers test framework and run postgres repository.
- curl commandes is suggested for postgres (in this version) to check data layer.

## Procedures
API runs on port 8086 and Postgres on 5432
- build : sbt docker:publishLocal
- run on Postgres : docker-compose up                           // postgres & bowling containers
- run on H2       : STORAGE_DB=memory docker-compose up bowling //bowling container only
 Note : docker-compose file is at root of project.
- to delete postgres data base : delete /postgres-data in project folder
- to stop containers : docker-compose down 
- to check containers : docker ps

## API Documentation

see Yaml file at root folder.
apologies it may not have the latest changes.

## Design Elements
### Scoring
In order to implement scoring, we did split the concept into bonus points and main points.
Each roll have main points and have bonus points it generates and transfers to previous rolls.
we made a decision to reverse the frames and remove the frame layer for ease of computation.
the 10th frame is special : it has bonus rolls that generate only bonus points, some code needed to deal with this special rule.
example : in 10 th frame Frame[X X X]  => 10 main points and 20 bonus.
          3 frames            [X][X][X]=> 30 main points and 30 bonus. 

## Sample cURL commands and Responses

### cURL Commands

```bash
# Create a game
curl -X POST -H "Content-Type: application/json" -d '{"uid": "123", "game": {"player": "John", "lane":"3", "frames":[]}}' http://localhost:8086/api/game

Response sample :
1

# Get the game
curl -X GET http://localhost:8086/api/game/1
Response:
{"id":1,"frames":[],"player":"John","lane":"3"}
# Get a nonexistent game
curl -X GET http://localhost:8086/api/game/9999
Response :
Empty Body, Status NotFound 404

# Update the game (Not required by specs but needed to implement ROLL)
curl -X PUT -H "Content-Type: application/json" -d '{"player": "Jane", "lane":"3", "frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0, "marker":"F"}]}]}' http://localhost:8086/api/game/1
Response:
{"id":1,"frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0,"marker":"F"}]}],"player":"Jane","lane":"3"}

# Roll (Miss with inference of marker)
curl -X PUT -H "Content-Type: application/json" -d '{"pins": 0, "marker":"-"}' http://localhost:8086/api/game/1/roll
// OR without marker,'-' will be inferred
curl -X PUT -H "Content-Type: application/json" -d '{"pins": 0}' http://localhost:8086/api/game/1/roll
{"id":1,"frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0,"marker":"F"}]},{"rolls":[{"pins":0,"marker":"-"}]}],"player":"John","lane":"3"}

# Roll (Strike, marker inferred)
curl -X PUT -H "Content-Type: application/json" -d '{"pins": 10}' http://localhost:8086/api/game/1/roll
Response:
{"id":1,"frames":[{"rolls":[{"pins":5},{"pins":0,"marker":"-"}]},{"rolls":[{"pins":7},{"pins":0,"marker":"F"}]},{"rolls":[{"pins":0,"marker":"-"},{"pins":10,"marker":"X"}]}],"player":"John","lane":"3"}
# Roll on Game Already complete
Response : Illegal State, Empty body with status 

# Get final score // this fails if game is incomplete. there is an other unexposed function to get frame per frame score for incomplete games as well.
curl -X GET http://localhost:8086/api/game/1/score
Response:
{"error":"No final sore for game in progress","ref":"1"}

# Get final score // this fails if game is incomplete (after many rolls).
160
# Delete the game
curl -X DELETE http://localhost:8086/api/game/1


// Create game response
1

// Get game response
{
  "id": 1,
  "frames": [],
  "player": "John",
  "lane": null,
  "gameSet": null
}

// Get nonexistent game response
{
  "error": "Requested Game Not found",
  "ref": "9999"
}

// Update game response
{
  "id": 1,
  "frames": [],
  "player": "Jane",
  "lane": null,
  "gameSet": null
}

// Roll (Miss) response
{
  "id": 1,
  "frames": [
    {
      "rolls": [
        {
          "pins": 0,
          "marker": "-"
        }
      ]
    }
  ],
  "player": "Jane",
  "lane": null,
  "gameSet": null
}

// Roll (Strike) response
{
  "id": 1,
  "frames": [
    {
      "rolls": [
        {
          "pins": 10,
          "marker": "X"
        }
      ]
    }
  ],
  "player": "Jane",
  "lane": null,
  "gameSet": null
}

//Example Response for score of incomplete game
{"error":"No final sore for game in progress","ref":"1"}

// Example JSON Responses for Delete Game

//Game Exists

Empty response with 204 No Content status.

// Game Does Not Exist

{
  "error": "Game Not Found",
  "ref": "9999"
}

## TODO Improvements
####Caching : a cache of find game by memoization should reduce the reads to the dabase, typically 10 rolls per game is a chance to save 10 reads by keeping active games in memory.

### Security : this was not covered in this solution, it is a must do in real production. solutions exist to add a security middlware to the API. and of course passwords/ secret mechanism is needed or if some authentication service exists it can be used.

###Performance : no metrics were done. we should do. the current solution specially with the cache should be more than enough for bowling. if this is really big (millions of daily games), then a good start will be partitioning by game status and date. most activity will be on recent games, specially active games or active within last hour. adding an index on gameDate may also be needed.