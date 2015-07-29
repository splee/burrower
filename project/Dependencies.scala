import sbt._

object Dependencies {
  val resolutionRepos = Seq(
    "Spray Repository" at "http://repo.spray.cc/"
  )

  object V {
    val playJson = "2.3.9"
    val metrics = "3.0.2"
  }

  object Libraries {
    val playJson        = "com.typesafe.play"           %% "play-json"              % V.playJson
    val metrics         = "com.codahale.metrics"        % "metrics-core"            % V.metrics
    val metricsInflux   = "net.alchim31"                % "metrics-influxdb"        % "0.7.1-SNAPSHOT"
    val scalaLogging    = "com.typesafe.scala-logging"  %% "scala-logging"          % "3.1.0"
    val dispatch        = "net.databinder.dispatch"     %% "dispatch-core"          % "0.11.2"
  }
}
