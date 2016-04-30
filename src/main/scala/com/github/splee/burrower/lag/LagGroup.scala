package com.github.splee.burrower.lag

case class LagGroup (
  timestamp: Long,
  lags: List[Lag]
)
