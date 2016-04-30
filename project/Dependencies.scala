import sbt._

object Dependencies {
  val resolutionRepos = Seq(
    "Spray Repository" at "http://repo.spray.cc/"
  )

  object V {
    val playJson = "2.5.2"
  }

  object Libraries {
    val playJson        = "com.typesafe.play"           %% "play-json"              % V.playJson
    val scalaLogging    = "com.typesafe.scala-logging"  %% "scala-logging"          % "3.1.0"
    val influxDb        = "com.paulgoldbaum"            %% "scala-influxdb-client"  % "0.4.5"
    val scalaJ          = "org.scalaj"                  %% "scalaj-http"            % "2.3.0"
    val logbackClassic  = "ch.qos.logback"              %  "logback-classic"        % "1.1.7"
  }
}
