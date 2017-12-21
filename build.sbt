import sbt.Keys._
import sbt.Resolver
import de.heikoseeberger.sbtheader.FileType
import play.twirl.sbt.Import.TwirlKeys

lazy val commonSettings = Seq(
  organization := "org.combinators",

  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),

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
        "org.combinators" %% "cls-scala" % "2.0.0-RC1",
        "org.combinators" %% "templating" % "1.0.0-RC1",
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
        "org.eclipse.jgit" % "org.eclipse.jgit" % "4.9.0.201710071750-r",
        "org.webjars" %% "webjars-play" % "2.6.1",
        "org.webjars" % "bootstrap" % "3.3.7-1",
        "commons-io" % "commons-io" % "2.6"
      ),
      sourceDirectories in (Compile, TwirlKeys.compileTemplates) := Seq(sourceDirectory.value / "main" / "html-templates"),
      headerMappings := headerMappings.value ++ Seq(
        FileType("html") -> HeaderCommentStyle.twirlStyleBlockComment,
        FileType("js") -> HeaderCommentStyle.cStyleBlockComment,
        FileType("routes") -> HeaderCommentStyle.hashLineComment
      ),

      sources in (Test, play.sbt.routes.RoutesKeys.routes) ++= ((unmanagedResourceDirectories in Test).value * "routes").get,

      unmanagedSources.in(Compile, headerCreate) ++= sources.in(Compile, TwirlKeys.compileTemplates).value,
      unmanagedSources.in(Compile, headerCreate) ++= sources.in(Compile, resourceDirectories).value
    )


lazy val publishSettings = Seq(
  homepage := Some(url("https://combinators.org")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(url("https://www.github.com/combinators/cls-scala-presentation-play-git"), "scm:git:git@github.com:combinators/cls-scala-presentation-play-git.git")),
  developers := List(
    Developer("JanBessai", "Jan Bessai", "jan.bessai@tu-dortmund.de", url("http://janbessai.github.io")),
    Developer("heineman", "George T. Heineman", "heineman@wpi.edu", url("http://www.cs.wpi.edu/~heineman")),
    Developer("BorisDuedder", "Boris Düdder", "boris.d@di.ku.dk", url("http://duedder.net"))
  ),

  pgpPublicRing := file("travis/local.pubring.asc"),
  pgpSecretRing := file("travis/local.secring.asc"),
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
