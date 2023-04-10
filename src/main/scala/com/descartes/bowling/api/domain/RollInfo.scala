package com.descartes.bowling.api.domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

/**
 * RollInfo contain rolled pins information
 * @param pins  pins hit in the roll
 * @param marker 'F' and 'S' are provided by client. others can be provided or inferred by the system.
 *               such as Miss, Strike or Spare are not required by clients of API.
 * @param uid  if client does not provide a unique identifier of a roll, there will be a risk of duplication.
 *             we recommend client to provide some identifier.
 */
case class RollInfo(pins: Int, marker: Option[Char] = None, uid: Option[String] = None)

object RollInfo {
  implicit val decoder: Decoder[RollInfo] = deriveDecoder[RollInfo]
  implicit val encoder: Encoder[RollInfo] = deriveEncoder[RollInfo].mapJson(_.dropNullValues)

}