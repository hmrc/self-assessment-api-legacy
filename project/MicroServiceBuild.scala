import play.sbt.routes.RoutesKeys.{routesImport, _}
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.SbtArtifactory

object MicroServiceBuild extends Build with MicroService {

  val appName = "self-assessment-api-legacy"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override lazy val playSettings: Seq[Setting[_]] = Seq(
    routesImport += "uk.gov.hmrc.selfassessmentapi.resources.Binders._")
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val hmrcTestVersion = "3.9.0-play-25"

  val compile = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.20.0-play-25",
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "uk.gov.hmrc" %% "bootstrap-play-25" % "4.13.0",
    "uk.gov.hmrc" %% "auth-client" % "2.22.0-play-25",
    "uk.gov.hmrc" %% "domain" % "5.6.0-play-25",
    "uk.gov.hmrc" %% "play-hmrc-api" % "3.2.0",
    "ai.x" %% "play-json-extensions" % "0.8.0",
    "org.typelevel" %% "cats-core" % "1.6.1"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % "3.0.8" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
        "com.github.tomakehurst" % "wiremock" % "2.23.2" % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % "4.15.0-play-25" % scope,
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0" % scope,
        "org.mongodb" %% "casbah" % "3.1.1" % scope,
        "org.scalacheck" %% "scalacheck" % "1.14.0" % scope,
        "org.skyscreamer" % "jsonassert" % "1.5.0" % scope,
        "com.jayway.restassured" % "rest-assured" % "2.9.0" % scope,
        "org.mockito" % "mockito-core" % "1.10.19" % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "func"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % "3.0.8" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
        "com.github.tomakehurst" % "wiremock" % "2.23.2" % scope,
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0" % scope,
        "org.mongodb" %% "casbah" % "3.1.1" % scope,
        // this line is only needed for coverage
        "org.scoverage" %% "scalac-scoverage-runtime" % "1.2.0" % scope,
        "org.mockito" % "mockito-core" % "1.10.19" % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
