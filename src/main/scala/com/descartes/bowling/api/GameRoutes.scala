package com.descartes.bowling.api

import cats.effect.IO
import com.descartes.bowling.api.domain.ApiDomainConvertersions._
import com.descartes.bowling.api.domain.{BowlingGameInfo, CreateGameRequest, RollInfo}
import com.descartes.bowling.api.domain.ApiDomainConvertersions._
import com.descartes.bowling.api.domain._
import com.descartes.bowling.domain.{BowlingGame, DomainError, Frame, GameNotFoundError, Roll}
import com.descartes.bowling.persistence.{GameRepository, GameRepositoryPostgres}
import com.descartes.bowling.service.{GameService, ScoringService}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.literal._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import org.slf4j.LoggerFactory


class GameRoutes(gameService: GameService, repository: GameRepository) extends Http4sDsl[IO] {
  val logger = LoggerFactory.getLogger(getClass)
  implicit val rollDecoder: Decoder[Roll] = deriveDecoder[Roll]
  implicit val frameDecoder: Decoder[Frame] = deriveDecoder[Frame]
  implicit val bowlingGameDecoder: Decoder[BowlingGame] = deriveDecoder[BowlingGame]

  val routes = HttpRoutes.of[IO] {

    case req@POST -> Root / "api" / "game" =>
      for {
        game <- {
          req.decodeJson[CreateGameRequest]
        }
        created <- repository.insert(convert[BowlingGameInfo,BowlingGame](game.game))
        response <- {
          logger.info("POST game created game {}",game)
          Created(created.asJson)
        }
      } yield response

    case req@PUT -> Root / "api" / "game" / LongVar(id)  =>
         req.decodeJson[BowlingGameInfo].flatMap { game =>
           val converted = convert[BowlingGameInfo, BowlingGame](game.copy(id=Some(id)))
           for {
             updateResult <- repository.updateGame(converted.id.get, converted)
             response <- gameResult2(updateResult)
           } yield response
         }

    case req@PUT -> Root / "api" / "game" / LongVar(id) / "roll" =>
      for {
        roll <- req.decodeJson[RollInfo]
        updateResult <- gameService.roll(id, convert[RollInfo,Roll](roll)) // convert to service model
        response <- gameResult2(updateResult)
      } yield response

    case GET -> Root / "api" / "game" / LongVar(id) =>
      for {
        game <- repository.findById(id)
        response <- {logger.info("GET game {} returned {}",id,game);toGameInfoResponse(game) }
      } yield response

    case GET -> Root / "api" / "game" / LongVar(id) / "score" =>
      for {
        game <- repository.findById(id)
        response <- scoreResponse(game)
      } yield  response

    case DELETE -> Root / "api" /"game" / LongVar(id) => {
      logger.info("delete requested {}",id)
     for {
      delete <- repository.deleteGame(id)
      response <-  deleteResponse(delete)
     } yield response
    }

  }

  private def toGameInfoResponse(maybeAGame: Option[BowlingGame]): IO[Response[IO]] = {
    val maybeAGameInfo = maybeAGame.map(g=>convert[BowlingGame,BowlingGameInfo](g))
    maybeAGameInfo match {
      case Some(g) => Ok(g.asJson)
      case None => NotFound()
    }
  }

  private def gameResult2(result: Either[GameNotFoundError, BowlingGame]) = {
    result match {
      case Left(e) => NotFound(ApiDomainError(e))
      case Right(game) => {
        Ok(convert[BowlingGame, BowlingGameInfo](game).asJson)
      }
    }
  }


  private def scoreResponse(game:Option[BowlingGame]):IO[Response[IO]] = {
    game match {
      case None  => NotFound()
      case Some(g)   => {
       val score = ScoringService.finalScore(g)
        if(score.isDefined){
          Ok(score.get.asJson)
        }else{
          BadRequest(ApiLayerError("No final sore for game in progress",g.id.map(_.toString)).asJson)
        }
      }
    }
  }

  private def deleteResponse(deleted: Either[GameNotFoundError, Unit]):IO[Response[IO]] = {
    deleted match {
      case Left(ex) => NotFound(ApiDomainError(ex).asJson)
      case Right(_) => Ok()
    }
  }
}

