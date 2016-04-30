package com.github.splee.burrower.lag

import play.api.libs.json.Json

object BurrowLagValue {
  implicit val burrowLagValueReader = Json.reads[BurrowLagValue]
}

case class BurrowLagValue (
  offset: Long,
  timestamp: Long,
  lag: Long
)
