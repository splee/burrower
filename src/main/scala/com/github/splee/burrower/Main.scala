package com.github.splee.burrower

import com.github.splee.burrower.lag.{BurrowConsumerStatus, BurrowPartitionLag, Lag, LagGroup}
import com.github.splee.burrower.write.{ConsoleWriter, InfluxWriter, Writer}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import scalaj.http._
import play.api.libs.json._

object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()

    val bHost = conf.getString("burrower.burrow.host")
    val bPort = conf.getInt("burrower.burrow.port")

    val writer = buildWriter(conf)

    logger.info("Creating monitor...")

    val monitorThread = new Thread(new Main(bHost, bPort, writer))

    logger.info("Starting.")
    monitorThread.start()
    monitorThread.join()

    logger.info("Done.")
    sys.exit()
  }

  def buildWriter(conf: Config): Writer = {
    val writerType = conf.getString("burrower.writer")
    logger.info(f"Creating $writerType writer...")

    if (writerType == "console")
      buildConsoleWriter(conf)
    else if (writerType == "influxdb")
      buildInfluxWriter(conf)
    else
      throw new RuntimeException(f"Writer of type '$writerType' is unknown")
  }

  def buildConsoleWriter(conf: Config): ConsoleWriter =
    new ConsoleWriter()

  def buildInfluxWriter(conf: Config): InfluxWriter =
    new InfluxWriter(
      conf.getString("burrower.influx.host"),
      conf.getInt("burrower.influx.port"),
      conf.getString("burrower.influx.database"),
      conf.getString("burrower.influx.series")
    )
}

class Main (
  burrowHost: String,
  burrowPort: Number,
  writer: Writer
) extends Runnable with LazyLogging {

  val burrowBaseUrl = f"http://$burrowHost:$burrowPort/v2/kafka"

  def run(): Unit = {
    while (true) {
      val lag = getLag
      if (lag.isDefined) {
        writer.write(LagGroup(System.currentTimeMillis(), lag.get))
      } else {
        logger.info("No lag values to write.")
      }
      Thread.sleep(1000 * 5)
    }
  }

  def buildUrl(endpoint: String) = f"$burrowBaseUrl/$endpoint"

  def getClusters: Option[List[String]] = {
    val resp = Http(burrowBaseUrl).asString
    val respJson = Json.parse(resp.body)
    if ((respJson \ "error").as[Boolean]) {
      val errorMsg = (respJson \ "message").as[String]
      logger.error(f"Error retrieving clusters: '$errorMsg'")
      return None
    }

    Option((respJson \ "clusters").as[List[String]])
  }

  def getConsumers(cluster: String): Option[List[String]] = {
    val resp = Http(buildUrl(f"$cluster/consumer")).asString
    val respJson = Json.parse(resp.body)
    if ((respJson \ "error").as[Boolean]) {
      val errorMsg = (respJson \ "message").as[String]
      logger.error(f"Error retrieving consumers: '$errorMsg'")
      return None
    }

    Option((respJson \ "consumers").as[List[String]])
  }

  def getLag(cluster: String, consumer: String): Option[List[Lag]] = {
    val resp = Http(buildUrl(f"$cluster/consumer/$consumer/lag")).asString
    val respJson = Json.parse(resp.body)
    if ((respJson \ "error").as[Boolean]) {
      val errorMsg = (respJson \ "message").as[String]
      logger.error(f"Error retrieving lag: '$errorMsg")
      return None
    }
    val statusResponse = (respJson \ "status").as[BurrowConsumerStatus]
    Option(statusResponse.partitions.map((item: BurrowPartitionLag) => {
      Lag(
        cluster,
        consumer,
        item.topic,
        item.partition,
        item.end.offset,
        item.end.timestamp,
        item.end.lag
      )
    }))
  }

  def getLag(cluster: String): Option[List[Lag]] = {
    val consumers = getConsumers(cluster)
    if (consumers.isEmpty || consumers.get.isEmpty) {
      logger.error("No consumers. Skipping lag check.")
      return None
    }

    Option(consumers.get
      .map(getLag(cluster, _))
      .filter(_.isDefined)
      .map(_.get)
      .flatMap(_.toList))
  }

  def getLag: Option[List[Lag]] = {
    val clusters = getClusters
    if (clusters.isEmpty || clusters.get.isEmpty) {
      logger.error("No clusters. Skipping lag check.")
      return None
    }

    Option(clusters.get
      .map(getLag(_))
      .filter(_.isDefined)
      .map(_.get)
      .flatMap(_.toList))
  }
}
