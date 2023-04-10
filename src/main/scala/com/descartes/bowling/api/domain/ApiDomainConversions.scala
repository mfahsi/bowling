package com.descartes.bowling.api.domain

import com.descartes.bowling.domain.{BowlingGame, Frame, Roll}

object ApiDomainConvertersions {

  trait Converter[A, B] {
    def convert(a: A): B
  }

  def convert[A, B](a: A)(implicit converter: Converter[A, B]): B = converter.convert(a)

  implicit val rollInfoToRoll: Converter[RollInfo, Roll] = (rollInfo: RollInfo) => Roll(rollInfo.pins, rollInfo.marker)

  implicit val frameInfoToFrame: Converter[FrameInfo, Frame] = (frameInfo: FrameInfo) => Frame(frameInfo.rolls.map(implicitly[Converter[RollInfo, Roll]].convert))

  implicit val bowlingGameInfoToBowlingGame: Converter[BowlingGameInfo, BowlingGame] = (bowlingGameInfo: BowlingGameInfo) =>
    BowlingGame(
      id = bowlingGameInfo.id,
      frames = bowlingGameInfo.frames.map(implicitly[Converter[FrameInfo, Frame]].convert),
      player = bowlingGameInfo.player,
      lane = bowlingGameInfo.lane,
      gameSet = bowlingGameInfo.gameSet
    )

  // Reverse converters
  implicit val rollToRollInfo: Converter[Roll, RollInfo] = (roll: Roll) => RollInfo(roll.pins, roll.marker)

  implicit val frameToFrameInfo: Converter[Frame, FrameInfo] = (frame: Frame) => FrameInfo(frame.rolls.map(implicitly[Converter[Roll, RollInfo]].convert))

  implicit val bowlingGameToBowlingGameInfo: Converter[BowlingGame, BowlingGameInfo] = (bowlingGame: BowlingGame) =>
    BowlingGameInfo(
      id = bowlingGame.id,
      frames = bowlingGame.frames.map(implicitly[Converter[Frame, FrameInfo]].convert),
      player = bowlingGame.player,
      lane = bowlingGame.lane,
      gameSet = bowlingGame.gameSet
    )
}



