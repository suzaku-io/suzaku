import sbt._
import Keys._
import com.typesafe.sbt.pgp.PgpKeys._
import Dependencies._

crossScalaVersions := Seq("2.11.11", "2.12.4")

scalafmtOnCompile in ThisBuild := true
scalafmtVersion in ThisBuild := "1.3.0"

val commonSettings = Seq(
  organization := "io.suzaku",
  version := Version.library,
  scalaVersion := "2.12.4",
  scalacOptions ++= Seq("-unchecked",
                        "-feature",
                        "-deprecation",
                        "-encoding",
                        "utf8",
                        "-Ypatmat-exhaust-depth",
                        "40",
                        "-Xfuture",
                        "-Ywarn-unused:imports",
                        "-Ywarn-unused:implicits"),
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
    publish := ((): Unit),
    publishLocal := ((): Unit),
    publishSigned := ((): Unit),
    publishLocalSigned := ((): Unit),
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
    packagedArtifacts := Map.empty
  )

/**
  * Suzaku core modules
  */
lazy val coreShared = crossProject
  .in(file("core-shared"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core-shared",
    libraryDependencies ++= Seq(
      arteria.value
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      scalaJSDOM.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSStage in Global := FastOptStage
  )
  .jvmSettings()

lazy val coreSharedJS  = coreShared.js
lazy val coreSharedJVM = coreShared.jvm

lazy val coreUI = crossProject
  .in(file("core-ui"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core-ui",
    libraryDependencies ++= Seq(
      arteria.value
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      scalaJSDOM.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSStage in Global := FastOptStage
  )
  .jvmSettings()
  .dependsOn(coreShared % "compile->compile;test->test")

lazy val coreUIJS  = coreUI.js
lazy val coreUIJVM = coreUI.jvm

lazy val coreApp = crossProject
  .in(file("core-app"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core-app",
    libraryDependencies ++= Seq(
      arteria.value
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      scalaJSDOM.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSStage in Global := FastOptStage
  )
  .jvmSettings()
  .dependsOn(coreShared % "compile->compile;test->test")

lazy val coreAppJS  = coreApp.js
lazy val coreAppJVM = coreApp.jvm

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
  .dependsOn(coreShared % "compile->compile;test->test")

lazy val baseWidgetsJS  = baseWidgets.js
lazy val baseWidgetsJVM = baseWidgets.jvm

lazy val baseWidgetsApp = crossProject
  .in(file("base-widgets-app"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-widgets-app",
    libraryDependencies ++= Seq()
  )
  .dependsOn(baseWidgets % "compile->compile;test->test", coreApp)

lazy val baseWidgetsAppJS  = baseWidgetsApp.js
lazy val baseWidgetsAppJVM = baseWidgetsApp.jvm

/**
  * Suzaku base web core
  */
lazy val webCoreShared = project
  .in(file("platform/web/core-shared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core-web",
    libraryDependencies ++= Seq(
      scalaJSDOM.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSStage in Global := FastOptStage
  )
  .dependsOn(coreSharedJS % "compile->compile;test->test")

lazy val webCoreUI = project
  .in(file("platform/web/core-ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core-ui-web",
    libraryDependencies ++= Seq(
      scalaJSDOM.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSStage in Global := FastOptStage
  )
  .dependsOn(webCoreShared % "compile->compile;test->test", coreUIJS)

lazy val webCoreApp = project
  .in(file("platform/web/core-app"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "suzaku-core-app-web",
    libraryDependencies ++= Seq(
      scalaJSDOM.value
    ),
    scalacOptions ++= sourceMapSetting.value,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSStage in Global := FastOptStage
  )
  .dependsOn(webCoreShared % "compile->compile;test->test", coreAppJS)

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
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSStage in Global := FastOptStage,
    libraryDependencies ++= Seq()
  )
  .dependsOn(webCoreUI, baseWidgetsJS)

/**
  * Suzaku web example project
  */
lazy val webDemo = preventPublication(project.in(file("webdemo")))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "suzaku-webdemo",
    libraryDependencies ++= Seq()
  )
  .dependsOn(webWidgets, baseWidgetsAppJS, webCoreApp)

lazy val suzaku = preventPublication(project.in(file(".")))
  .settings()
  .aggregate(
    coreSharedJVM,
    coreSharedJS,
    coreUIJVM,
    coreUIJS,
    coreAppJVM,
    coreAppJS,
    baseWidgetsJVM,
    baseWidgetsJS,
    baseWidgetsAppJVM,
    baseWidgetsAppJS,
    webCoreShared,
    webCoreUI,
    webCoreApp,
    webWidgets,
    webDemo
  )
