package com.github.splee.burrower.lag

case class Lag (
  cluster: String,
  group: String,
  topic: String,
  partition: Int,
  offset: Long,
  timestamp: Long,
  lag: Long
)
