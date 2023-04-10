package com.descartes.bowling.api.domain

import com.descartes.bowling.api.domain.FrameInfo.RollPosition
import com.descartes.bowling.domain.{Frame, Roll}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class BowlingGameInfo(id: Option[Long], frames: List[FrameInfo] = List(), player: Option[String] = None, lane: Option[String] = None, gameSet: Option[Long] = None) {
  def pins(framePos: Int, rollPost: RollPosition): Option[Int] = frames.lift(framePos).flatMap(_.pins(rollPost))

  def marker(framePos: Int, rollPost: RollPosition): Option[Option[Char]] = frames.lift(framePos).flatMap(_.marker(rollPost))

  def roll(framePos: Int, rollPost: RollPosition): Option[RollInfo] = frame(framePos).flatMap(_.roll(rollPost))

  def frame(framePos: Int): Option[FrameInfo] = frames.lift(framePos)
}

object BowlingGameInfo {
  implicit val decoder: Decoder[BowlingGameInfo] = deriveDecoder[BowlingGameInfo]
  implicit val encoder: Encoder[BowlingGameInfo] = deriveEncoder[BowlingGameInfo].mapJson(_.dropNullValues)

  def frameInfoFromFrame(f: Frame) = FrameInfo(f.rolls.map(rollInfoFromRoll(_)))

  def rollInfoFromRoll(r: Roll) = RollInfo(r.pins, r.marker)
}