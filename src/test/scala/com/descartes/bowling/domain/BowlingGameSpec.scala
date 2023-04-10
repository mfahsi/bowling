package com.descartes.bowling.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

/**
 * this class tests the interpretation of Frame in isolation of its position in a game.
 * scores do not include updates from other frames.
 */
class BowlingGameSpec extends AnyFlatSpec with should.Matchers {
  val miss = Roll(0, None)
  val full = Roll(10, None)
  val strikeFrame = Frame(List(Roll(10, None)))
  val foul = Roll(0, Some('F'))
  val three = Roll(3, None)
  val seven = Roll(7, None)
  val split = Roll(5, Some('S'))
  val aFrame = Frame(List(three, three))

  val aGameWithTwoFrames = BowlingGame(Some(1)).addedRoll(three).addedRoll(three)
  //score 54
  val aGameWithNineFrames = BowlingGame(Some(1), BowlingGame.dsl.frameN(9)(aFrame))

  val g10Frames1roll = aGameWithNineFrames.addedRoll(three)

  val gameStrike = BowlingGame(Some(1), List[Frame](aFrame, strikeFrame) ++ List.fill(8)(aFrame))
  val gameSpare = BowlingGame(Some(1), List[Frame](aFrame, Frame(List(seven))) ++ List.fill(8)(aFrame))

  "Frame Logic" should "empty game have 0 frames" in {
    BowlingGame(Some(1)).frames shouldBe empty
  }

  it should "throw illegal state roll is sent on already complete game" in {
    val complete = g10Frames1roll.addedRoll(three)
    assertThrows[IllegalStateException] {
      complete.addedRoll(three)
    }
  }

  it should "create new frame when rolling on complete frame" in {
    aGameWithTwoFrames.isComplete shouldBe (false)
    val rolled = aGameWithTwoFrames.addedRoll(seven)
    rolled.frames.size shouldBe (2)
    rolled.isComplete shouldBe (false)
   }

  it should "A Strike makes a frame if no bonus case" in {
    aGameWithTwoFrames.addedRoll(full).frames.last.isComplete(false) shouldBe (true)
    aGameWithTwoFrames.addedRoll(three).frames.last.isComplete(false) shouldBe (false)
  }

  /** * Last frame Bonus cases  ** */
  "Bonus and Termination Rules" should "Game with last frame with STRIKE is incomplete game" in {
    aGameWithNineFrames.addedRoll(full).addedRoll(full).isComplete shouldBe (false)
    aGameWithNineFrames.addedRoll(full).addedRoll(full).addedRoll(full).isComplete shouldBe (true)
  }

  it should "Game with last frame SPARE is incomplete game" in {
    aGameWithNineFrames.addedRoll(full).addedRoll(full).isComplete shouldBe (false)
    aGameWithNineFrames.addedRoll(three).addedRoll(seven).isComplete shouldBe (false)
    aGameWithNineFrames.addedRoll(three).addedRoll(seven).addedRoll(three).isComplete shouldBe (true)
  }

  it should "Spare on 10th frame have a bonus roll" in {
    g10Frames1roll.addedRoll(seven).isComplete shouldBe (false)
    g10Frames1roll.addedRoll(seven).addedRoll(three).isComplete shouldBe (true)
  }

  it should "game still incomplete if 10th frame is incomplete" in {
    g10Frames1roll.isComplete shouldBe (false)
  }


}
