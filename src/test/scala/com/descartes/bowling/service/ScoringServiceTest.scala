package com.descartes.bowling.service

import com.descartes.bowling.domain.{BowlingGame, Frame, FrameScore, Roll, RollScore, SPLIT, STRIKE}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import BowlingGame.dsl._

class ScoringServiceTest extends AnyFlatSpec with should.Matchers {

  val strikeFrame = Frame(List(roll(10,STRIKE)))
  val TwotrikeFramePlusFive = Frame(List(roll(10,STRIKE),roll(10,STRIKE),roll(5)))
  val aFrame = rolls(3,3)
  /*** roll**/
  val three = Roll(3)
  val seven = Roll(7, None)
  val split = Roll(5, Some('S'))
  val full = Roll(10, Some('X'))
  /*** games **/
  val aGameWithNineFrames = BowlingGame(Some(1), BowlingGame.dsl.frameN(9)(aFrame))
  val g10Frames1roll = aGameWithNineFrames.addedRoll(three)
  val gameStrike = BowlingGame(Some(1), List[Frame](aFrame, strikeFrame) ++ List.fill(8)(aFrame))

  val gameSpareLast = BowlingGame(Some(1), List.fill(9)(rolls(1,1))++List(Frame(List(three,seven,split))))
  val gameSpare = BowlingGame(Some(1), List.fill(5)(rolls(1,1))++List(rolls(6,4))++List.fill(4)(rolls(1,1)))//18+10+2=2
  val game3StrikeEnd = BowlingGame(Some(1), List.fill(9)(rolls(10)) ++ List(Frame(List(full,full,full))))
  val gameAll1 = BowlingGame(Some(1), List.fill(10)(rolls(1,1)))

  "Score Vanilla case" should "20 x 1 rolls give 20" in {
    ScoringService.finalScore(gameAll1) shouldBe (Some(20))
  }
  it should "20 x 0 give 0" in {
    ScoringService.finalScore(BowlingGame(Some(1), List.fill(10)(rolls(0,0)))) shouldBe (Some(0))
  }


  "Frame Logic / Strike in middle" should "constitute new frame" in {
    val aGame = BowlingGame(Some(1)).addedRoll(three).toOption.get.addedRoll(three).toOption.get.addedRoll(full).toOption.get.addedRoll(seven).toOption.get
    aGame.frames.length shouldBe (3)
    ScoringService.frameScores(aGame.frames) shouldBe (List(FrameScore(6, List(RollScore(3, None), RollScore(3, None))), FrameScore(10, List(RollScore(10, STRIKE))), FrameScore(7, List(RollScore(7, None)))))
  }

  "Completion Rules/Bonus - score SPARE at End" should "Bonus roll required for completion and add Bonus" in {
    val game2 = aGameWithNineFrames.addedRollUnsafe(three).addedRollUnsafe(seven).addedRollUnsafe(three)
    game2.isComplete shouldBe (true)
    ScoringService.finalScore(game2) shouldBe (Some(67))
    ScoringService.finalScore(aGameWithNineFrames) shouldBe (empty) //incomplete game
  }

  "Display Rule - score normal and Split display" should "count spare bonus and show /" in {
    val game2 = aGameWithNineFrames.addedRollUnsafe(split).addedRollUnsafe(three)
    ScoringService.finalScore(game2) shouldBe (Some(62))
    ScoringService.frameScores(aGameWithNineFrames.addedRollUnsafe(split).addedRollUnsafe(three).frames).last.rolls.head.marker should be(SPLIT)

  }

  "Spare Rules" should "count bonus spare" in {
     ScoringService.finalScore(gameSpare) shouldBe (Some(29))
  }
  it should "count bonus spare at end" in {
    ScoringService.gameScoreWithoutBonus(gameSpareLast) shouldBe (28)
    gameSpareLast.frames.last.isSpare() should be (true)
    ScoringService.finalScore(gameSpareLast) shouldBe (Some(33))
  }
  "Strike Rules" should "count bonus Strike" in {
    ScoringService.finalScore(gameStrike) shouldBe (Some(70))
  }
  it should "count bonus for 2 consecutive strikes in middle" in {
    val consecutiveStrike = BowlingGame(Some(1), List.fill(4)(rolls(1, 1)) ++ frameN(2)(strikeFrame) ++ List.fill(4)(rolls(1, 1))) //18+10+2=2
    ScoringService.finalScore(consecutiveStrike) shouldBe (Some(49))
  }
  it should "handle strikes in 10th frame" in {
    val game2StrikeLast = BowlingGame(Some(1), frameN(9)(rolls(1,1))++List(Frame(List(full,full,split))))
    ScoringService.finalScore(game2StrikeLast) shouldBe (Some(43))
  }
  it should "handle 3 stikes at end" in {
    val game3StrikeEnd = BowlingGame(Some(1), List.fill(9)(rolls(1,1)) ++ List(frame(full,full,full)))
    ScoringService.finalScore(game3StrikeEnd) shouldBe (Some(48))
  }
  it should "handle best game" in {
    val allStrike = BowlingGame(Some(1), List.fill(9)(frame(full)) ++ List(frame(full,full,full)))
    ScoringService.finalScore(allStrike) shouldBe (Some(300))
  }


}
