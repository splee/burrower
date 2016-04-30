package com.github.splee.burrower.lag

import play.api.libs.json.Json

object BurrowPartitionLag {
  implicit val burrowPartitionLagReader = Json.reads[BurrowPartitionLag]
}

case class BurrowPartitionLag(
  topic: String,
  partition: Int,
  status: String,
  start: BurrowLagValue,
  end: BurrowLagValue
)

