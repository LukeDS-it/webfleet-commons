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

val baseDependencies = Seq(
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.codehaus.janino" % "janino" % janinoVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % logstashLogbackEncoderVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalatestplus" %% "scalatestplus-mockito" % scalatestMockitoVersion % Test,
)

val customDependencies = Seq(
  "com.auth0" % "jwks-rsa" % "0.8.2",
  "com.auth0" % "java-jwt" % "3.8.1",
  "org.liquibase" % "liquibase-core" % "3.9.0",
  "com.rabbitmq" % "amqp-client" % "5.9.0",
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
    libraryDependencies ++= akkaDependencies ++ baseDependencies ++ customDependencies
  )