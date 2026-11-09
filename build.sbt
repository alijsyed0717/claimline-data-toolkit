ThisBuild / scalaVersion := "2.12.18"
ThisBuild / organization := "com.claimline"

lazy val sparkVersion = "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "claimline-data-toolkit",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
      "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    )
  )
