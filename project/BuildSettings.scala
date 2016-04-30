import sbt._
import Keys._

object BuildSettings {
  lazy val basicSettings = Seq[Setting[_]](
    organization := "com.github.splee",
    version := "0.2-SNAPSHOT",
    description := "Monitors consumer group offset lag in Burrow using InfluxDB",
    resolvers ++= Dependencies.resolutionRepos
  )

  // sbt-assembly settings for building a fat jar
  import sbtassembly.Plugin._
  import AssemblyKeys._
  lazy val sbtAssemblySettings = assemblySettings ++ Seq(

    // Slightly cleaner jar name
    jarName in assembly := {
      name.value + "-" + version.value + ".jar"
    },

    // Drop these jars
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes = Set(
        "jsp-api-2.1-6.1.14.jar",
        "jsp-2.1-6.1.14.jar",
        "jasper-compiler-5.5.12.jar",
        "commons-beanutils-core-1.8.0.jar",
        "commons-beanutils-1.7.0.jar",
        "servlet-api-2.5-20081211.jar",
        "servlet-api-2.5.jar"
      )
      cp filter { jar => excludes(jar.data.getName) }
    },

    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        // case "project.clj" => MergeStrategy.discard // Leiningen build files
        case x if x.startsWith("META-INF") => MergeStrategy.discard // Bumf
        case x if x.endsWith(".html") => MergeStrategy.discard // More bumf
        case x if x.endsWith("UnusedStubClass.class") => MergeStrategy.first // really?
        case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last // For Log$Logger.class
        case x if x.endsWith("project.clj") => MergeStrategy.discard // throw it away.
        case x => old(x)
      }
    }
  )

  // Leave this here for later so we can add sbtAssemblySettings if we want.
  lazy val buildSettings = basicSettings ++ sbtAssemblySettings
}
