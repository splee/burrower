# Burrower

A simple tool to calculate the lag of a given consumer group from [Burrow](http://github.com/linkedin/burrow)
and report it to InfluxDB.

**Works with:**
* InfluxDB 0.9
* Burrow latest (Commit SHA: [aff0e33](https://github.com/linkedin/Burrow/commit/aff0e3321fefcacd2bde9685e6bed813c96bcc7c))

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
1. `java -cp target/scala-2.11/burrower-0.2-SNAPSHOT.jar:conf/ com.github.splee.burrower.Main`

## Planned Features

* Tests!
* Filtering for clusters, topcis, and consumer groups (e.g. ignore all topcis prefixed with dev-*)
* Packaging for Debian with sane defaults and an Upstart configuration.
