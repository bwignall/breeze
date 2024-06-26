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

name := "breeze-parent"

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
