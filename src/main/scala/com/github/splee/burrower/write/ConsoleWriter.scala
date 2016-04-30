package com.github.splee.burrower.write

import com.github.splee.burrower.lag.{Lag, LagGroup}

class ConsoleWriter extends Writer {
  def write(lagGroup: LagGroup): Unit = {
    System.out.println("=" * 80)
    System.out.println(f"Group Timestamp: ${lagGroup.timestamp}")
    System.out.println("Cluster\tGroup\tTopic\tPartition\tOffset\tLag\tTimestamp")
    lagGroup.lags.foreach(printLag)
  }

  def printLag(lag: Lag): Unit =
    System.out.println(f"${lag.timestamp}\t${lag.group}\t${lag.topic}\t${lag.partition}\t${lag.offset}\t${lag.lag}\t${lag.timestamp}")
}
