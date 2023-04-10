package com.descartes.bowling.domain

import cats.Show
import com.descartes.bowling.domain.Roll.inferMarkers
import io.circe.Encoder
import io.circe.generic.JsonCodec
import io.circe.generic.semiauto.deriveEncoder
import cats.Show
import cats.syntax.show._

object Roll {
  implicit val encoder: Encoder[Roll] = deriveEncoder[Roll].mapJson(_.dropNullValues)

  implicit val rollShow: Show[Roll] = Show.show(_.showRoll())

  def inferMarkers(roll: Roll): Roll = {
    if (roll.marker.isEmpty) {
      roll.pins match {
        case 0 => roll.withMark(MISS.get)
        case 10 => roll.withMark(STRIKE.get)
        // F, S markers must be provided
        // Spare marker / is inferred in score functions only in this version
        case _ => roll
      }
    } else {
      roll
    }
  }
}

@JsonCodec case class Roll(pins: Int, marker: Option[Char] = None) {
  def isPerfectRoll: Boolean = pins == 10

  def rollScore() = marker match {
    case Some(m) => m
    case None => if (pins == 0) '-' else pins.toString.charAt(0)
  }

  def withMark(newMarker: Char): Roll = this.copy(marker = Some(newMarker))

  //used by cats Show
  def showRoll(): String = inferMarkers(this).marker.map(m => m match {
    case 'X' | '/' | '-' | 'F' => m.toString
    case m: Char => s"${pins.toString}$m"
  }).getOrElse(pins.toString)
}
