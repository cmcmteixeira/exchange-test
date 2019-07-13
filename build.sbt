val doobieVersion     = "0.6.0"
val http4sVersion     = "0.20.0"
val circeVersion      = "0.11.1"
val buckyVersion      = "2.0.0-M11"
val serviceBoxVersion = "0.3.3"
val monocleVersion    = "1.5.0"
val fs2Version        = "1.0.3"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, JavaAgent)
  .settings(
    Defaults.itSettings,
    organization := "cmcmteixeira",
    mainClass := Some("lenses.exchange.Main"),
    scalaVersion := "2.12.8",
    name := "exchange",
    scalacOptions := Seq(
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
      "-deprecation",
      "-feature",
      "-language:higherKinds",
      "-Ywarn-value-discard",
      "-Yno-adapted-args",
      "-Ypartial-unification",
      "-Xfatal-warnings",
      "-Xmax-classfile-name",
      "100"
    ),
    libraryDependencies := Seq(
      "org.scalatest"              %% "scalatest"               % "3.0.1" % "test",
      "org.aspectj"                % "aspectjweaver"            % "1.9.2",
      "org.mockito"                % "mockito-core"             % "2.28.2" % "test",
      "org.http4s"                 %% "http4s-circe"            % http4sVersion,
      "org.http4s"                 %% "http4s-dsl"              % http4sVersion,
      "org.http4s"                 %% "http4s-circe"            % http4sVersion,
      "org.http4s"                 %% "http4s-blaze-client"     % http4sVersion,
      "io.circe"                   %% "circe-generic"           % circeVersion,
      "io.circe"                   %% "circe-parser"            % circeVersion,
      "io.circe"                   %% "circe-generic-extras"    % circeVersion,
      "io.circe"                   %% "circe-java8"             % circeVersion,
      "org.http4s"                 %% "http4s-blaze-server"     % http4sVersion,
      "ch.qos.logback"             % "logback-classic"          % "1.2.3",
      "net.logstash.logback"       % "logstash-logback-encoder" % "5.2",
      "com.typesafe.scala-logging" %% "scala-logging"           % "3.5.0",
      "com.iheart"                 %% "ficus"                   % "1.4.1",
      "io.kamon"                   %% "kamon-executors"         % "1.0.2",
      "io.kamon"                   %% "kamon-http4s"            % "1.0.12",
      "io.kamon"                   %% "kamon-system-metrics"    % "1.0.1",
      "io.kamon"                   %% "kamon-logback"           % "1.0.5",
      "io.kamon"                   %% "kamon-influxdb"          % "1.0.2",
      "io.kamon"                   %% "kamon-core"              % "1.1.3",
      "io.kamon"                   %% "kamon-zipkin"            % "1.0.0"
    ),
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.9.2",
    javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default"
  )
