scalacOptions.in(Global) += "-deprecation"

//addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.0.2")

addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings" % "2.1.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("org.scalanlp" % "sbt-breeze-expand-codegen" % "0.2.1")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")

addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.7.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-site" % "0.7.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-scalafix" % "0.7.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.16.0")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.5.2")
//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.12")
