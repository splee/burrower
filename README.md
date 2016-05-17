# Burrower

A simple tool to calculate the lag of a given consumer group from [Burrow](http://github.com/linkedin/burrow)
and report it to InfluxDB.

**Works with:**
* InfluxDB 0.9
* Burrow latest (Commit SHA: [7930a61](https://github.com/linkedin/Burrow/commit/7930a61a3e72df5df8a59ccdf3158585b785762f))

Burrower will traverse the Burrow API and retrieve lag metrics for all clusters and consumer groups that are available.

Metrics are sent to InfluxDB with the following values and tags:

**Values**
* offset
* lag

**Tags**
* cluster
* consumer_group
* topic
* partition

*NOTE: While this code seems stable, it has not been extensively tested.  If you have any problems please open an issue (or better yet, a pull request).*

## Build & Run

1. `git clone https://github.com/splee/burrower.git`
1. `cd burrower && sbt assembly`
1. Update configuration, saving it as `conf/application.conf` (example config file: `burrow/conf/application.conf.example`)
1. `java -cp target/scala-2.11/burrower-0.2-SNAPSHOT.jar:conf/ com.github.splee.burrower.OffsetMonitor`

## Planned Features

* Ability to specify any class implmenting `com.github.splee.burrower.write.Writer` to write metrics to custom back ends.
* Tests!
* Packaging for Debian with sane defaults and an Upstart configuration.
