/*
 * Copyright 2021 HM Revenue & Customs
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
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),

    "uk.gov.hmrc"       %% "simple-reactivemongo" % "7.31.0-play-26",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-26" % "3.2.0",
    "uk.gov.hmrc"       %% "domain"               % "5.10.0-play-26",
    "com.typesafe.play" %% "play-json-joda"       % "2.6.14",
    "uk.gov.hmrc"       %% "play-hmrc-api"        % "4.1.0-play-26",
    "org.typelevel"     %% "cats-core"            % "2.3.1",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.6.0" cross CrossVersion.full),
    "com.github.ghik"   % "silencer-lib"          % "1.6.0" % Provided cross CrossVersion.full
  )

  def test(scope: String = "test, func"): Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                 % "3.2.3"             % scope,
    "com.typesafe.play"      %% "play-test"                 % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"        % "3.1.3"             % scope,
    "com.github.tomakehurst" % "wiremock-jre8"              % "2.27.2"            % scope,
    "de.flapdoodle.embed"    %  "de.flapdoodle.embed.mongo" % "2.2.0"             % scope,
    "org.mongodb"            %% "casbah"                    % "3.1.1"             % scope,
    "org.scalacheck"         %% "scalacheck"                % "1.15.2"            % scope,
    "org.scalatestplus"      %% "scalatestplus-mockito"     % "1.0.0-M2"          % scope,
    "com.vladsch.flexmark"   %  "flexmark-all"              % "0.36.8"            % scope,
    "org.skyscreamer"        %  "jsonassert"                % "1.5.0"             % scope,
    "com.jayway.restassured" %  "rest-assured"              % "2.9.0"             % scope,
    "org.mockito"            %  "mockito-core"              % "3.7.0"             % scope
  )
}
