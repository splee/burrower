package com.github.splee.burrower

import java.io.{StringWriter, PrintWriter}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.codahale.metrics.{MetricFilter, Gauge, MetricRegistry}
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.LazyLogging
import metrics_influxdb.{InfluxdbHttp, InfluxdbReporter}
import play.api.libs.json._
import dispatch._, Defaults._
import scala.collection.JavaConverters._

case class MonitorState(
  currentLag: Long,
  topicOffsets: List[Long],
  consumerOffsets: List[Long])

object OffsetMonitor extends LazyLogging {
  def createReporter(metrics: MetricRegistry, conf: Config): InfluxdbReporter = {
    val client = new InfluxdbHttp(
      conf.getString("burrower.influx.host"),
      conf.getInt("burrower.influx.port"),
      conf.getString("burrower.influx.db"),
      conf.getString("burrower.influx.user"),
      conf.getString("burrower.influx.passwd"))

    InfluxdbReporter.forRegistry(metrics)
      .prefixedWith(conf.getString("burrower.influx.prefix"))
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .filter(MetricFilter.ALL)
      .skipIdleMetrics(false)
      .build(client)
  }

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()

    val bHost = conf.getString("burrower.burrow.host")
    val bPort = conf.getInt("burrower.burrow.port")
    val bCluster = conf.getString("burrower.cluster")
    val groupId = conf.getString("burrower.groupId")
    val topics = conf.getStringList("burrower.topics").asScala

    val metrics = new MetricRegistry()
    val reporter = createReporter(metrics, conf)
    reporter.start(10, TimeUnit.SECONDS)

    val running = new AtomicBoolean(true)

    logger.info("Creating monitors for %s topics:".format(topics.size))
    for (t <- topics) logger.info(f"Cluster: $bCluster%s, Group: $groupId%s, Topic: $t%s")

    def _createRunnable(t: String) =
      (t, new OffsetMonitor(running, bHost, bPort, metrics, bCluster, t, groupId))

    val monitorThreads = topics.map(_createRunnable)
      .map(r => (r._1, new Thread(r._2)))
      .toMap

    logger.info("Starting.")
    monitorThreads.values.foreach(_.start())

    while (monitorThreads.values.map(_.isAlive).reduce(_ && _)) {
      Thread.sleep(1000)
    }

    logger.warn("A thread died, shut it down!")
    // If we got here a thread died. Stop everything.
    running.set(false)

    // Wait for the rest of the threads to finish.
    logger.info("waiting for threads to terminate...")
    monitorThreads.values.foreach(_.join())
    logger.info("Done.")
    sys.exit()
  }
}

class OffsetMonitor (
  running: AtomicBoolean,
  burrowHost: String,
  burrowPort: Number,
  metrics: MetricRegistry,
  cluster: String,
  topic: String,
  groupId: String) extends Runnable with LazyLogging {

  private var state = MonitorState(0, List(), List())
  def currentLag = state.synchronized { state.currentLag }
  def topicOffsets = state.synchronized { state.topicOffsets }
  def consumerOffsets = state.synchronized { state.consumerOffsets }

  private val gauge = new Gauge[Long]() {
    override def getValue = currentLag
  }

  def run(): Unit = {
    metrics.register(MetricRegistry.name(
      "OffsetMonitor",
      groupId,
      topic,
      "lag"), gauge)

    while (running.get) {
      updateCurrentLag()
      Thread.sleep(1000 * 5)
    }
  }

  def burrowBase = host(burrowHost + ":" + burrowPort.toString) / "v2" / "kafka" / cluster

  def topicOffsetRequest() = Http(burrowBase / "consumer" / groupId / "topic" / topic OK as.String)

  def consumerOffsetRequest() = Http(burrowBase / "topic" / topic OK as.String)

  def calculateTotalLag(topicOffsets: List[Long], consumerOffsets: List[Long]): Long = {
    val lag = (consumerOffsets zip topicOffsets).map(p => p._1 - p._2).sum
    // We don't ever want our lag to be negative. Let's assume we didn't ingest future messages.
    if (lag < 0) 0 else lag
  }

  def updateCurrentLag() {
    // Launch the requests asynchronously
    val consumerResp = consumerOffsetRequest()
    val topicResp = topicOffsetRequest()

    // Get the responses and parse out the offsets.
    try {
      val newConsumerOffsets = (Json.parse(consumerResp()) \ "offsets").as[List[Long]]
      val newTopicOffsets = (Json.parse(topicResp()) \ "offsets").as[List[Long]]
      val newLag = calculateTotalLag(newTopicOffsets, newConsumerOffsets)

      val newState = MonitorState(newLag, newTopicOffsets, newConsumerOffsets)

      // Lock these as we update them as we'll be used in a threaded environment.
      state.synchronized {
        state = newState
      }
    } catch {
      case e: Exception =>
        val sw = new StringWriter()
        e.printStackTrace(new PrintWriter(sw))
        logger.error(sw.toString)
    }
  }
}
