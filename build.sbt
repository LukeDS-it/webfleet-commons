val akkaVersion = "2.6.5"
val akkaHttpVersion = "10.1.12"
val akkaHttpCirceVersion = "1.31.0"
val pureconfigVersion = "0.12.3"
val scalaLoggingVersion = "3.9.2"
val logbackVersion = "1.2.3"
val logstashLogbackEncoderVersion = "5.2"
val scalatestVersion = "3.1.1"
val scalatestMockitoVersion = "1.0.0-M2"
val testcontainersVersion = "1.14.2"
val testcontainersScalaVersion = "0.37.0"
val circeVersion = "0.13.0"
val janinoVersion = "3.1.0"
val amqpClientVersion = "5.9.0"
val jwksVersion = "0.8.2"
val jwtVersion = "3.8.1"

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test,
)

val dependencies = Seq(
  "com.auth0" % "jwks-rsa" % jwksVersion,
  "com.auth0" % "java-jwt" % jwtVersion,
  "com.rabbitmq" % "amqp-client" % amqpClientVersion,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,

  "org.codehaus.janino" % "janino" % janinoVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % logstashLogbackEncoderVersion,

  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.testcontainers" % "testcontainers" % testcontainersVersion % Test,
  "com.dimafeng" %% "testcontainers-scala" % testcontainersScalaVersion % Test,
  "org.scalatestplus" %% "scalatestplus-mockito" % scalatestMockitoVersion % Test,
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin)
  .settings(Defaults.itSettings)
  .settings(CommonSettings.settings)
  .settings(ReleaseSettings.settings)
  .settings(
    organization := "it.ldsoftware",
    name := "webfleet-commons",
    scalaVersion := "2.13.1",
    fork in IntegrationTest := true,
    envVars in IntegrationTest := Map(
      "APP_VERSION" -> git.gitDescribedVersion.value.getOrElse((version in ThisBuild).value)
    ),
    libraryDependencies ++= akkaDependencies ++ dependencies
  )