package com.descartes.bowling.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveEnumerationReader

sealed trait DbType
case object memory extends DbType
case object postgres extends DbType

object DbType {
  implicit val reader: ConfigReader[DbType] = deriveEnumerationReader[DbType]
}
