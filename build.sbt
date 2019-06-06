name := "IndexJMapHisto"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % "6.5.1",
  "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.5.1",
  "com.sksamuel.elastic4s" %% "elastic4s-circe" % "6.5.1",
  "com.sksamuel.elastic4s" %% "elastic4s-cats-effect" % "6.5.1",
  "io.circe" %% "circe-core" % "0.12.0-M2",
  "io.circe" %% "circe-generic" % "0.12.0-M2",
  "io.circe" %% "circe-parser" % "0.12.0-M2",
  "org.typelevel" %% "cats-core" % "2.0.0-M1",
  "org.typelevel" %% "cats-effect" % "1.3.0"
)