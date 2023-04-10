package com.descartes.bowling.service

import cats.implicits.toShow
import com.descartes.bowling.domain.{BowlingGame, Frame, FrameScore, MISS, Roll, RollScore, SPARE, STRIKE}
import org.slf4j.LoggerFactory

object ScoringService {
  val logger = LoggerFactory.getLogger(getClass)

  def finalScore(game: BowlingGame): Option[Int] = {
    val scoreNoBonus: Option[Int] = if (game.isComplete) Some(frameScores(game.frames).map(_.total).sum) else None
    val strikesBonus = calculateStrikeBonus(game.frames)
    val spareBonus = calculateSpareBonus(game.frames)
    val bonus = strikesBonus + spareBonus
    val result: Option[Int] = scoreNoBonus.map(_ + bonus)
    logger.info(s"\n        ${game.show} =SCORE=> {} (include bonuses={})",result, strikesBonus + spareBonus)
    result
  }

  def frameScores(frames: List[Frame]): List[FrameScore] = {
    val scoreWithoutBonus = frames.map(ScoringService.scoreWithoutBonus(_))
    scoreWithoutBonus
  }

  def gameScoreWithoutBonus(game: BowlingGame): Int = game.frames.map(scoreWithoutBonus(_)).map(_.total).reduce(_ + _)

  def scoreWithoutBonus(frame: Frame): FrameScore = {
    import com.descartes.bowling.domain.BowlingGame._
    def scoreExcludeBonus(f: Frame): List[RollScore] = {
      val rolls: List[Roll] = f.rolls
      rolls match {
        case Nil => List()
        case a :: Roll(10, _) :: c :: Nil => RollScore(a.pins, a.marker) :: RollScore(0, a.marker) :: RollScore(0, c.marker) :: Nil
        case a :: b :: c :: Nil => RollScore(a.pins, a.marker) :: RollScore(b.pins, c.marker) :: RollScore(0, c.marker) :: Nil
        case a :: b :: Nil => RollScore(a.pins, a.marker) :: RollScore(b.pins, b.marker) :: Nil
        case Roll(10, m) :: Nil => RollScore(10, m) :: Nil
        case a :: Nil => RollScore(a.pins, a.marker) :: Nil
        case _ => List()
      }
    }

    val frameM = Frame(frame.rolls.map(Roll.inferMarkers(_)))
    val scores = scoreExcludeBonus(frameM)

    val total = scores.map(_.pins).sum
    if (!frame.isStrike() && frame.isSpare()) {
      val head = scores.head
      val second = scores.tail.head.copy(marker = SPARE)
      val maybeThird = scores.tail.tail.headOption
      val result = maybeThird.map(head :: second :: _ :: Nil).getOrElse(head :: second :: Nil)
      FrameScore(total, result)
    } else {
      FrameScore(total, scores)
    }
  }

  def calculateStrikeBonus(frames: List[Frame]): Int = {
    val lastOpt = frames.lastOption.map(last => {
      val ajustCaseLastFrameXX = if (last.isStrike() && last.roll(1).isDefined && last.roll(1).get.isPerfectRoll) {
        last.roll(2).get.pins
      } else {
        0
      }

      var rollsReversed = frames.map(_.rolls).flatten.reverse

      def addBonusRollsReversed(rolls: List[Roll], bonus: Int): Int = {
        rolls match {
          case Nil => 0
          case prev :: Nil => bonus
          case prev2 :: prev1 :: prev :: tail if prev1.isPerfectRoll && prev.isPerfectRoll => {
            val bonusPrev2 = 2 * prev2.pins
            addBonusRollsReversed(rolls.tail, bonusPrev2 + bonus)
          }
          case prev2 :: prev1 :: prev :: tail if prev1.isPerfectRoll || prev.isPerfectRoll => {
            val bonusPrev2 = prev2.pins
            addBonusRollsReversed(rolls.tail, bonusPrev2 + bonus)
          }
          case prev2 :: prev1 :: prev :: tail => {
            val bonusPrev2 = 0
            addBonusRollsReversed(rolls.tail, bonusPrev2 + bonus)
          }
          case prev2 :: prev1 :: Nil => {
            val bonusPrev2 = (if (prev1.isPerfectRoll) prev2.pins else 0)
            addBonusRollsReversed(rolls.tail, bonusPrev2 + bonus)
          }
        }
      }
      addBonusRollsReversed(rollsReversed, 0) - ajustCaseLastFrameXX
    }
    )
    lastOpt.getOrElse(0)
  }


  def calculateSpareBonus(frames: List[Frame]): Int = {
    val framesReversed = frames.reverse
    val headOpt = framesReversed.headOption
    val bonus = headOpt.map(head => {
      val spare = head.isSpare()

      val sbonus = if (spare) {
        framesReversed.head.roll(2).map(_.pins).getOrElse(0)
      } else {
        0
      }
      sbonus
    }).getOrElse(0)

    def addSpareBonusFramesReversed(frames: List[Frame], bonus: Int): Int = {

      frames match {
        case Nil => 0
        case prev :: Nil => bonus
        case prev1 :: prev :: tail if prev.isSpare() => {
          val bonusPrev = prev1.rolls.head.pins
          addSpareBonusFramesReversed(frames.tail, bonusPrev + bonus)
        }
        case _ :: prev :: tail => addSpareBonusFramesReversed(frames.tail, bonus)
      }
    }

    addSpareBonusFramesReversed(framesReversed, bonus)
  }


}
