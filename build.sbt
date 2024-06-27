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

// scalafix currently OOMs in CI
ThisBuild / tlCiScalafixCheck := false

name := "breeze-parent"

val Scala213 = "2.13.13"
val Scala3 = "3.3.3"
ThisBuild / crossScalaVersions := Seq(Scala213) // Cannot run Scala 3 because of Spark-induced conflicts
ThisBuild / scalaVersion := Scala213 // the default Scala

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
