# Burrower

A simple tool to calculate the lag of a given consumer group from [Burrow](http://github.com/linkedin/burrow)
and report it to InfluxDB.

*NOTE: While this code seems stable, it has not been extensively tested.  If you have any problems please open an issue (or better yet, a pull request).*

## Usage

1. `git clone https://github.com/splee/burrow.git`
1. Create a package jar for [metrics-influxdb](https://github.com/davidB/metrics-influxdb) and place in `<project_root>/lib/`
1. `cd burrow && sbt assembly`
1. Update configuration (example: `burrow/conf/application.conf.example`)
1. `java -cp target/scala-2.11/burrower-0.1.jar:conf/ com.github.splee.burrower.OffsetMonitor`
