/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.20.0-play-26",
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.1.0",
    "uk.gov.hmrc" %% "domain" % "5.6.0-play-26",
    "com.typesafe.play" %% "play-json-joda" % "2.6.0",
    "uk.gov.hmrc" %% "play-hmrc-api" % "3.2.0",
    "org.typelevel" %% "cats-core" % "1.6.1"
  )

  def test(scope: String = "test, func"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
    "org.scalatest" %% "scalatest" % "3.0.8" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
    "com.github.tomakehurst" % "wiremock" % "2.23.2" % scope,
    "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0" % scope,
    "org.mongodb" %% "casbah" % "3.1.1" % scope,
    "org.scalacheck" %% "scalacheck" % "1.14.0" % scope,
    "org.skyscreamer" % "jsonassert" % "1.5.0" % scope,
    "com.jayway.restassured" % "rest-assured" % "2.9.0" % scope,
    "org.mockito" % "mockito-core" % "1.10.19" % scope,
    "org.scoverage" %% "scalac-scoverage-runtime" % "1.3.1" % scope
  )

  // Fixes a transitive dependency clash between wiremock and scalatestplus-play
  val overrides: Set[ModuleID] = {
    val jettyFromWiremockVersion = "9.2.24.v20180105"
    Set(
      "org.eclipse.jetty" % "jetty-client" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-continuation" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-http" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-io" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-security" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-server" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-servlet" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-servlets" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-util" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-webapp" % jettyFromWiremockVersion,
      "org.eclipse.jetty" % "jetty-xml" % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-api" % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-client" % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-common" % jettyFromWiremockVersion
    )
  }
}