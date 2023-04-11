package com.descartes.bowling.api.domain

import cats.implicits.toShow
import com.descartes.bowling.domain.BowlingGame.dsl.{frameN, rolls}
import com.descartes.bowling.domain.{DomainError, FrameScore, GameNotFoundError, IncompleteGameScoreAttempt, MISS, RollAttemptedGameComplete, RollScore, STRIKE}
import io.circe.Decoder
import io.circe.literal.JsonStringContext
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ApiModelTest extends AnyFlatSpec with should.Matchers {

  import io.circe.syntax._

  val gameA = BowlingGameInfo(None, List())
  val gameB = BowlingGameInfo(Some(44), frames=List(FrameInfo(List(RollInfo(10,STRIKE)))),player=Some("Anna"))
 // val gameComplete = BowlingGameInfo(Some(44), frames=frameN(10)(rolls(2,2)),player=Some("Anna"))
  "RollInfo accessor" should "access pin and marker" in {
    RollInfo(3,Some('S')).pins  should be(3)
    RollInfo(3,Some('S')).marker.get  should be('S')
  }

  "BowlingGameInfo" should "access player and marker" in {
    gameB.player should be(Some("Anna"))
    gameB.roll(0,0).get.marker shouldBe(STRIKE)
  }

  "FrameScore" should "access player and marker" in {
   val fs = FrameScore(20,List(RollScore(10,Some('X')),RollScore(10,Some('X'))))
    fs.total should be (20)
    fs.show should be ("score 20 X X")
  }

  val gameNotFound =  ApiDomainError(GameNotFoundError(43443))
  val gameIncompleteScore =  ApiDomainError(IncompleteGameScoreAttempt(999))
  val rollOnCompleteGame =  ApiDomainError(RollAttemptedGameComplete(999))
  "ApiDomainError" should "GameNotFoundError capture game Id as a reference string" in {
    gameNotFound.domainError.reference() should be (Some("43443"))
    gameNotFound.domainError.errorMessage() should be ("Game not found")

  }
  it should "adapt error messages when converting to API layer" in {
    gameNotFound.domainError.errorMessage() should be("Game not found")
    gameNotFound.asFlatApiLayer.error should be("Requested Game Not found")
    gameIncompleteScore.domainError.reference() should be(Some("999"))
    gameIncompleteScore.asFlatApiLayer.error should be("Can't provide a final score on a pending game")
    gameIncompleteScore.asFlatApiLayer.ref should be(Some("999"))
    rollOnCompleteGame.asFlatApiLayer.ref should be(Some("999"))
    rollOnCompleteGame.asFlatApiLayer.error should be("Can't roll on a complete game")
  }

  "asFlatApiLayer" should "add a generic error if message" in {
    val err = new DomainError("Message",Some("any ref"))
    err.reference() should be(Some("any ref"))
    err.errorMessage() should be("Message")
    ApiDomainError(err).asFlatApiLayer.error should be("Message")
  }

}
