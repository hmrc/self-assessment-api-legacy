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
    ws,
    "uk.gov.hmrc"       %% "simple-reactivemongo"      % "8.0.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % "4.2.0",
    "com.typesafe.play" %% "play-json-joda"            % "2.6.0",
    "uk.gov.hmrc"       %% "play-hmrc-api"             % "6.2.0-play-28",
    "org.typelevel"     %% "cats-core"                 % "2.6.0"
  )

  def test(scope: String = "test, func"): Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                % "3.2.7"             % scope,
    "com.typesafe.play"      %% "play-test"                % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"             % scope,
    "com.github.tomakehurst" % "wiremock-jre8"             % "2.27.2"            % scope,
    "de.flapdoodle.embed"    % "de.flapdoodle.embed.mongo" % "2.2.0"             % scope,
    "org.mongodb"            %% "casbah"                   % "3.1.1"             % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.15.3"            % scope,
    "org.scalatestplus"      %% "scalatestplus-mockito"    % "1.0.0-M2"          % scope,
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.36.8"            % scope,
    "org.skyscreamer"        % "jsonassert"                % "1.5.0"             % scope,
    "com.jayway.restassured" % "rest-assured"              % "2.9.0"             % scope,
    "org.mockito"            % "mockito-core"              % "3.5.13"            % scope
  )
}
