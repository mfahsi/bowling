package com.descartes.bowling.api.domain

import com.descartes.bowling.api.domain.ApiDomainConvertersions._
import com.descartes.bowling.domain.BowlingGame
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DomainTranslationTest extends AnyWordSpec with Matchers {

  import com.descartes.bowling.domain.BowlingGame.dsl._

  val game1 = game(Some(99), Some("Peter"), Some("Lane 6"), Some(124L))(frame(roll(10)), frame(roll(3), roll(7)))

  val gameInfo = BowlingGameInfo(Some(99),List(FrameInfo(List(RollInfo(10,None,None))), FrameInfo(List(RollInfo(3,None,None), RollInfo(7,None,None)))),Some("Peter"),Some("Lane 6"),Some(124))

  "Domain model API model Conversions" should {
    "API model -> Domain model conversion" in {
      convert[BowlingGameInfo, BowlingGame](gameInfo) shouldBe (game1)
    }
    "Domain model -> API model conversion" in {
      convert[BowlingGame, BowlingGameInfo](game1) shouldBe (gameInfo)
    }
  }
  //TODO need more tests for Frame and Roll Level.
}

