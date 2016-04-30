package com.github.splee.burrower.write

import com.github.splee.burrower.lag.LagGroup

trait Writer {
  def write(lagGroup: LagGroup)
}
