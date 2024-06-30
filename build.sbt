//enablePlugins(GitVersioning)

Common.commonSettings

ThisBuild / licenses := Seq(License.Apache2)

// publish website from this branch
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

// dependency tracking
ThisBuild / tlCiDependencyGraphJob := false

// generate documentation
ThisBuild / tlCiDocCheck := true

// Not currently bothering to generate/check headers
ThisBuild / tlCiHeaderCheck := false

// For the time being, allow warnings (do not treat as error; -Werror can be re-enabled later by removing this line)
ThisBuild / tlFatalWarnings := false

ThisBuild / tlCiScalafmtCheck := false

name := "breeze-parent"

val Scala212 = "2.12.19"
val Scala213 = "2.13.13"
val Scala3 = "3.3.3"
val defaultScala = Scala213
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)
ThisBuild / scalaVersion := defaultScala // the default Scala

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

lazy val root = project
  .in(file("."))
  .aggregate(math, natives, viz, macros)
  .dependsOn(math, viz)

lazy val macros = project.in(file("macros"))

lazy val math = project.in(file("math")).dependsOn(macros)

lazy val natives = project.in(file("natives")).dependsOn(math)

lazy val viz = project.in(file("viz")).dependsOn(math)

lazy val benchmark = project.in(file("benchmark")).dependsOn(math, natives)

Global / onChangedBuildSource := ReloadOnSourceChanges

Test / fork := true
Test / javaOptions := Seq("-Xmx3G")

// CI

ThisBuild / githubWorkflowEnv ++= Map("SBT_OPTS" -> "-Xmx3G", "JAVA_OPTS" -> "-Xmx3G")
