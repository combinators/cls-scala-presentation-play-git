import sbt.Keys._
import sbt.Resolver
import de.heikoseeberger.sbtheader.FileType
import play.twirl.sbt.Import.TwirlKeys

lazy val commonSettings = Seq(
  organization := "org.combinators",
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  releaseIgnoreUntrackedFiles := true,

  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),
  releaseCrossBuild := true,

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.typesafeRepo("snapshots")
  ),

  headerLicense := Some(HeaderLicense.ALv2("2017", "Jan Bessai")),

  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions"
  )
) ++ publishSettings

lazy val root = (Project(id = "cls-scala-presentation-play-git", base = file(".")))
  .settings(commonSettings: _*)
  .enablePlugins(SbtTwirl)
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
      moduleName := "cls-scala-presentation-play-git",

      libraryDependencies ++= Seq(
        "org.combinators" %% "cls-scala" % "2.0.0",
        "org.combinators" %% "templating" % "1.0.0",
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
        "org.eclipse.jgit" % "org.eclipse.jgit" % "4.9.0.201710071750-r",
        "org.webjars" %% "webjars-play" % "2.6.1",
        "org.webjars" % "bootstrap" % "3.3.7-1"
      ),
      sourceDirectories in (Compile, TwirlKeys.compileTemplates) := Seq(sourceDirectory.value / "main" / "html-templates"),
      headerMappings := headerMappings.value ++ Seq(
        FileType("html") -> HeaderCommentStyle.twirlStyleBlockComment,
        FileType("js") -> HeaderCommentStyle.cStyleBlockComment,
        FileType("routes") -> HeaderCommentStyle.hashLineComment
      ),
      unmanagedSources.in(Compile, headerCreate) ++= sources.in(Compile, TwirlKeys.compileTemplates).value,
      unmanagedSources.in(Compile, headerCreate) ++= sources.in(Compile, resourceDirectories).value
    )


lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishTo := { version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }.value },
  homepage := Some(url("https://combinators.org")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(url("https://www.github.com/combinators/cls-scala-presentation-play-git"), "scm:git:git@github.com:combinators/cls-scala-presentation-play-git.git")),
  pomExtra := (
    <developers>
      <developer>
        <id>JanBessai</id>
        <name>Jan Bessai</name>
        <url>http://janbessai.github.io/</url>
      </developer>
      <developer>
        <id>BorisDuedder</id>
        <name>Boris Düdder</name>
        <url>http://duedder.net/</url>
      </developer>
      <developer>
        <id>heineman</id>
        <name>George T. Heineman</name>
        <url>http://www.cs.wpi.edu/~heineman</url>
      </developer>
    </developers>
    )
)

lazy val noPublishSettings = Seq(
  publish := Seq.empty,
  publishLocal := Seq.empty,
  publishArtifact := false
)

credentials in ThisBuild ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
