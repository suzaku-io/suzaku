import sbt._
import Keys._
import com.typesafe.sbt.pgp.PgpKeys._
import Dependencies._

crossScalaVersions := Seq("2.11.11", "2.12.2")

val commonSettings = Seq(
  organization := "io.suzaku",
  version := Version.library,
  scalaVersion := "2.12.2",
  scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8", "-Ypatmat-exhaust-depth", "40"),
  libraryDependencies ++= Seq(
    scalaTest.value,
    scalaMock.value
  ),
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

val publishSettings = Seq(
  scmInfo := Some(
    ScmInfo(url("https://github.com/suzaku-io/suzaku"),
            "scm:git:git@github.com:suzaku-io/suzaku.git",
            Some("scm:git:git@github.com:suzaku-io/suzaku.git"))),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomExtra :=
    <url>https://github.com/suzaku-io/suzaku</url>
      <licenses>
        <license>
          <name>Apache 2.0 license</name>
          <url>http://www.opensource.org/licenses/Apache-2.0</url>
        </license>
      </licenses>
      <developers>
        <developer>
          <id>ochrons</id>
          <name>Otto Chrons</name>
          <url>https://github.com/ochrons</url>
        </developer>
      </developers>,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
)

val sourceMapSetting =
  Def.setting(
    if (isSnapshot.value) Seq.empty
    else
      Seq({
        val a = baseDirectory.value.toURI.toString.replaceFirst("[^/]+/?$", "")
        val g = "https://raw.githubusercontent.com/suzaku-io/suzaku"
        s"-P:scalajs:mapSourceURI:$a->$g/v${version.value}/${name.value}/"
      })
  )

def preventPublication(p: Project) =
  p.settings(
    publish := (),
    publishLocal := (),
    publishSigned := (),
    publishLocalSigned := (),
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
    packagedArtifacts := Map.empty
  )

/**
  * Suzaku core module
  */
lazy val core = crossProject
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core",
    libraryDependencies ++= Seq(
      arteria.value
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      scalaJSDOM.value,
      scalaTags.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalaJSStage in Global := FastOptStage
  )
  .jvmSettings()

lazy val coreJS  = core.js
lazy val coreJVM = core.jvm

/**
  * Suzaku base widgets
  */
lazy val baseWidgets = crossProject
  .in(file("base-widgets"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-widgets",
    libraryDependencies ++= Seq()
  )
  .dependsOn(core)

lazy val baseWidgetsJS  = baseWidgets.js
lazy val baseWidgetsJVM = baseWidgets.jvm

/**
  * Suzaku base web core
  */
lazy val webCore = project
  .in(file("platform/web/core"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core-web",
    libraryDependencies ++= Seq(
      scalaJSDOM.value,
      scalaTags.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalaJSStage in Global := FastOptStage
  )
  .dependsOn(coreJS)

/**
  * Suzaku base web widgets
  */
lazy val webWidgets = project
  .in(file("platform/web/base-widgets"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-widgets-web",
    scalacOptions ++= sourceMapSetting.value,
    scalaJSStage in Global := FastOptStage,
    libraryDependencies ++= Seq()
  )
  .dependsOn(webCore, baseWidgetsJS)

/**
  * Suzaku web example project
  */
lazy val webDemo = preventPublication(project.in(file("webdemo")))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "suzaku-webdemo",
    libraryDependencies ++= Seq(),
    workbenchSettings,
    bootSnippet := "WebDemoUI().main();"
  )
  .dependsOn(webWidgets)

lazy val root = preventPublication(project.in(file(".")))
  .settings()
  .aggregate(coreJVM, coreJS, webCore, baseWidgetsJVM, baseWidgetsJS, webWidgets, webDemo)
