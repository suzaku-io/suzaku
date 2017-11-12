// repository for Typesafe plugins
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC13")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.21")

addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.13")
