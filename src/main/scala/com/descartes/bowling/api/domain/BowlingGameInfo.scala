package com.descartes.bowling.api.domain

import com.descartes.bowling.api.domain.FrameInfo.RollPosition
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class BowlingGameInfo(id: Option[Long], frames: List[FrameInfo] = List(), player: Option[String] = None, lane: Option[String] = None, gameSet: Option[Long] = None) {

  def roll(framePos: Int, rollPost: RollPosition): Option[RollInfo] = frame(framePos).flatMap(_.roll(rollPost))

  def frame(framePos: Int): Option[FrameInfo] = frames.lift(framePos)
}

object BowlingGameInfo {
  implicit val decoder: Decoder[BowlingGameInfo] = deriveDecoder[BowlingGameInfo]
  implicit val encoder: Encoder[BowlingGameInfo] = deriveEncoder[BowlingGameInfo].mapJson(_.dropNullValues)

}