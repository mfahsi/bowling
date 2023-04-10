package com.descartes.bowling.domain

import cats.implicits.toShow
import com.descartes.bowling.domain.RollScore.RS_STRIKE
import com.descartes.bowling.service.ScoringService
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

/**
 * this class tests the interpretation of Frame in isolation of its position in a game.
 * scores do not include updates from other frames.
 */
class FrameSpec extends AnyFlatSpec with should.Matchers {
  val miss = Roll(0, None) //'-' not entered
  val miss_marked = Roll(0, Some('-')) //'-' not entered
  val full = Roll(10, None)
  val foul = Roll(0, Some('F'))
  val three = Roll(3, None)
  val seven = Roll(7, None)
  val split = Roll(5, Some('S'))


  "Strike show & score" should "show X and score 10" in {
    Frame(List(full)).show should be ("X")
    ScoringService.scoreWithoutBonus(Frame(List(full))) should be(FrameScore(10, List(RollScore(10, STRIKE))))
  }

  "Spare show & score" should "show / and score 10" in {
    val frame = Frame(List(three, seven))
    frame.show should be ("3 /")
    ScoringService.scoreWithoutBonus(frame) should be(FrameScore(10, List(RollScore(3, None), RollScore(7, SPARE))))
  }

  "Miss show & score" should "show -" in {
    val frame = Frame(List(three, miss)) // miss marker not provided but still added by logic
    frame.showFrame() should be("3 -")
    ScoringService.scoreWithoutBonus(frame) should be(FrameScore(3, List(RollScore(3, None), RollScore(0, MISS))))
  }

  "Foul show & score" should "show -" in {
    val frame = Frame(List(three, foul))
    frame.show should be("3 F")
    ScoringService.scoreWithoutBonus(frame) should be(FrameScore(3, List(RollScore(3, None), RollScore(0, FOUL))))
    val foul2 = Frame(List(foul, seven))
    ScoringService.scoreWithoutBonus(foul2) should be(FrameScore(7, List(RollScore(0, FOUL), RollScore(7, None))))
  }

  "Split" should "display the S with pins" in {
    Frame(List(split, foul)).show should be("5S F")
    Frame(List(split, three)).show should be("5S 3")
  }

  /** * Last frame Bonus cases  ** */
  "last frame/ 2 strike must be last frame " should "show X X" in {
    val frame = Frame(List(full, full, three))
    frame.show should be("X X 3")
    ScoringService.scoreWithoutBonus(frame) should be(FrameScore(10, List(RS_STRIKE, RollScore(0,Some('X')), RollScore(0,None))))
    val frame2 = Frame(List(full, full, full))
    ScoringService.scoreWithoutBonus(frame2) should be(FrameScore(10, List(RS_STRIKE, RollScore(0,Some('X')), RollScore(0,Some('X')))))
    frame2.show should be("X X X")
    val frame3 = Frame(List(full, three, three))
    frame3.show should be("X 3 3")
    ScoringService.scoreWithoutBonus(frame3) should be(FrameScore(13, List(RS_STRIKE, RollScore(3, None), RollScore(0, None))))
  }

  "last frame/  spare" should "show /  " in {
    Frame(List(three, seven, three)).show should be("3 / 3")
  }
  "last frame/  spare followed by strike" should "show / and X " in {
    Frame(List(three, seven, full)).show should be("3 / X")
    ScoringService.scoreWithoutBonus(Frame(List(three, seven, full))) should be(FrameScore(10, List(RollScore(3, None), RollScore(7, SPARE), RollScore(0, STRIKE))))
  }

}
