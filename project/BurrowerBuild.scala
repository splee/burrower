import sbt._
import Keys._

object BurrowerBuild extends Build {

  import Dependencies._
  import BuildSettings._

  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  lazy val project = Project("burrower", file("."))
    .settings(buildSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        Libraries.playJson,
        Libraries.metrics,
        Libraries.scalaLogging,
        Libraries.dispatch
      )
    )
    .settings(scalaVersion := "2.11.6")
}
