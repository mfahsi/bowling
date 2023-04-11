package com.descartes.bowling.api

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.descartes.bowling.api.domain._
import com.descartes.bowling.config.{AppConfig, memory}
import com.descartes.bowling.domain.BowlingGame.dsl.rolls
import com.descartes.bowling.domain.{BowlingGame, Roll}
import com.descartes.bowling.persistence.GameRepositoryH2
import doobie.Transactor
import io.circe.Json
import io.circe.generic.auto._
import io.circe.literal._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ApiDomainConvertersions._
import com.descartes.bowling.service.GameService

import java.util.UUID

class GameRoutesH2Test extends AnyWordSpec with Matchers {
  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  val repository = AppConfig.makeCustomRepository.run(memory).use(sys => IO.pure(sys)).unsafeRunSync()
  val transactor: Transactor[IO] = repository.transactor()
  val gameservice = new GameService(repository)
  private val api = new GameRoutes(gameservice, repository).routes


  "GameRoutes API CRUD" should {

    "POST /api/game createServer a game" in {
      val id = createAGame("Peter", "lane 4")
      id should be >= 1L
    }

    "PUT /api/game update a game" in {
      val id = createAGame("Peter", "lane 4")
      val frames_2 = List(FrameInfo(List(RollInfo(5, None), RollInfo(5, None))), FrameInfo(List(RollInfo(0, Some('F')))))
      val game = BowlingGameInfo(Some(id), frames_2)
      apiCall(Request[IO](PUT, Uri.unsafeFromString(s"/api/game/$id")).withEntity(game.asJson))
      val gameUpdated = findGameById(id)
      gameUpdated.get.frames.head.rolls.head should be(RollInfo(5, None))
    }

    "GET /api/game/$id Get by id" in {
      val id = createAGame("Peter", "lane 4")
      val response = apiCall(Request[IO](GET, Uri.unsafeFromString(s"/api/game/$id")))
      response.status shouldBe Status.Ok
      println(response.as[Json].unsafeRunSync())
      response.as[Json].unsafeRunSync() shouldBe
        json"""{
          "id" : $id,
          "frames" : [
          ],
          "player" : "Peter",
          "lane" : "lane 4"
        }"""
    }
  }

  "Game API Roll" should {
    "PUT ROLL /api/game/roll add a roll to a game" in {
      val id = createAGame("Peter", "lane 4")
      val roll = RollInfo(3, None,Some(UUID.randomUUID().toString))
      val response = apiCall(Request[IO](PUT, Uri.unsafeFromString(s"/api/game/$id/roll")).withEntity(roll))
      response.status shouldBe Status.Ok
      val gameUpdated = findGameById(id)
      gameUpdated.get.roll(0, 0) should be(Some(RollInfo(3, None)))
    }
  }

  "Scoring API /api/game/$id/score" should {
    "Return API error with status BadRequest if game is not completee" in {
      val id = createAGame("Peter", "lane 4")
      val frames9 = BowlingGame.dsl.rollsN(9)(Roll(1), Roll(2))
      val game = BowlingGame(Some(id), frames9)
      val gameInfo = convert[BowlingGame, BowlingGameInfo](game)
      apiCall(Request[IO](PUT, uri"/api/game").withEntity(gameInfo.asJson))
      val response = apiCall(Request[IO](GET, Uri.unsafeFromString(s"/api/game/$id/score")))
      response.status shouldBe Status.BadRequest
      response.as[ApiLayerError].unsafeRunSync() shouldBe((ApiLayerError("Can't provide a final score on a pending game",Some(id.toString))))
    }

    "Return the Score of game is complete" in {
      val id = createAGame("Peter", "lane 4")
      val frames9 = BowlingGame.dsl.rollsN(10)(Roll(1), Roll(2))
      val game = BowlingGame(Some(id), frames9)
      val gameInfo = convert[BowlingGame, BowlingGameInfo](game)
      apiCall(Request[IO](PUT, Uri.unsafeFromString(s"/api/game/$id")).withEntity(gameInfo.asJson))
      val response = apiCall(Request[IO](GET, Uri.unsafeFromString(s"/api/game/$id/score")))
      response.status shouldBe Status.Ok
      response.as[Long].unsafeRunSync() shouldBe(30L)
    }
  }

    "DELETE /api/game/$id delete game with id" should {
      "delete existing game" in {
        val id = createAGame("Peter", "lane 4")
        val check = apiCall(Request[IO](GET, Uri.unsafeFromString(s"/api/game/$id")))
        val deleted = apiCall(Request[IO](DELETE, Uri.unsafeFromString(s"/api/game/$id")))
        val recheck = apiCall(Request[IO](GET, Uri.unsafeFromString(s"/api/game/$id")))
        check.status shouldBe Status.Ok
        deleted.status shouldBe Status.Ok
        recheck.status shouldBe Status.NotFound
      }

      "Return 404 with API error if NON existing game" in {
        val id = Some(8635)
        val deleted = apiCall(Request[IO](DELETE, Uri.unsafeFromString(s"/api/game/${id.get}")))
        deleted.status shouldBe Status.NotFound
       val result = deleted.as[ApiLayerError].unsafeRunSync() //shouldBe json"""30"""
        result shouldBe(ApiLayerError("Requested Game Not found",Some("8635")))
      }
    }



  def createAGame(player: String, lane: String): Long = {
    val gameInfo = BowlingGameInfo(None, player = Some(player), lane = Some(lane))
    val request = CreateGameRequest(gameInfo,Some(UUID.randomUUID().toString))
    val response = apiCall(Request[IO](POST, uri"/api/game").withEntity(request))
    val id = response.as[Long].unsafeRunSync()
    id
  }

  private def apiCall(request: Request[IO]): Response[IO] = {
    api.orNotFound(request).unsafeRunSync()
  }

  def findGameById(id: Long): Option[BowlingGameInfo] = {
    val response = apiCall(Request[IO](GET, Uri.unsafeFromString(s"/api/game/${id.toString}")))
    val game = response.as[Option[BowlingGameInfo]].unsafeRunSync()
    game
  }




}
