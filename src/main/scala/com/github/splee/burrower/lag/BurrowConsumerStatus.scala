package com.github.splee.burrower.lag

import play.api.libs.json.Json

object BurrowConsumerStatus {
  implicit val burrowConsumerStatusReader = Json.reads[BurrowConsumerStatus]
}

case class BurrowConsumerStatus(
  cluster: String,
  group: String,
  status: String,
  complete: Boolean,
  partition_count: Int,
  maxlag: Option[BurrowLagValue],
  partitions: List[BurrowPartitionLag]
)

