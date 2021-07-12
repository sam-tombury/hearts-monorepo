ThisBuild / organization := "uk.co.sgjbryan"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.0.1"

lazy val versions = new {
  val zio = new {
    val core = "1.0.6"
    val json = "0.1.4"
    val http = "1.0.0.0-RC15+14-6f2b83ca-SNAPSHOT"
  }
  val laminar    = "0.12.2"
  val laminext   = "0.12.3"
  val jsJavaTime = "2.2.1"
}

lazy val sharedSettings = Seq(
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  scalacOptions ++= Seq("-Ymacro-annotations", "-Xfatal-warnings"),
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
  ),
  libraryDependencies ++= Seq(
    "dev.zio" %%% "zio"         % versions.zio.core,
    "dev.zio" %%% "zio-streams" % versions.zio.core,
    "dev.zio" %%% "zio-json"    % versions.zio.json
  ),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)

lazy val root = project
  .in(file("."))
  .aggregate(
    backend,
    frontend
  )

lazy val backend = project
  .settings(
    sharedSettings,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies ++= Seq(
      "io.d11"  %% "zhttp"    % versions.zio.http,
      "dev.zio" %% "zio-test" % versions.zio.core % Test
    )
  )
  .dependsOn(common)

lazy val frontend = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    sharedSettings,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.raquo"         %%% "laminar"            % versions.laminar,
      "io.laminext"       %%% "websocket"          % versions.laminext,
      "io.laminext"       %%% "websocket-zio-json" % versions.laminext,
      "io.laminext"       %%% "fetch"              % versions.laminext,
      "io.laminext"       %%% "core"               % versions.laminext,
      "io.github.cquiroz" %%% "scala-java-time"    % versions.jsJavaTime
    )
  )
  .dependsOn(common)

lazy val common = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    sharedSettings,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) }
  )
