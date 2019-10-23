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

import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.{SbtArtifactory, SbtAutoBuildPlugin}

val appName = "self-assessment-api-legacy"

lazy val FuncTest = config("func") extend Test

lazy val scoverageSettings: Seq[Def.Setting[_]] = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*(Reverse|BuildInfo|Routes).*;" +
      "uk.gov.hmrc.r2.selfassessmentapi.config.*; uk.gov.hmrc.r2.selfassessmentapi.domain.*;" +
      "uk.gov.hmrc.r2.selfassessmentapi.services.*;" +
      "uk.gov.hmrc.selfassessmentapi.domain.*; uk.gov.hmrc.kenshoo.monitoring.*;",
    ScoverageKeys.coverageMinimum := 85,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    play.sbt.PlayScala,
    SbtAutoBuildPlugin,
    SbtGitVersioning,
    SbtDistributablesPlugin,
    SbtArtifactory
  )
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    dependencyOverrides ++= AppDependencies.overrides,
    routesImport ++= Seq("uk.gov.hmrc.selfassessmentapi.resources.Binders._"
    ),
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default
      .withWarnScalaVersionEviction(false),
    scalaVersion := "2.11.12"
  )
  .settings(
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"
  )
  .settings(majorVersion := 0)
  .settings(publishingSettings: _*)
  .settings(scoverageSettings: _*)
  .settings(defaultSettings(): _*)
  .configs(FuncTest)
  .settings(inConfig(FuncTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in FuncTest := true,
    unmanagedSourceDirectories in FuncTest := (baseDirectory in FuncTest)(
      base => Seq(base / "func", base / "test")
    ).value,
    javaOptions in FuncTest += "-Dlogger.resource=logback-test.xml",
    parallelExecution in FuncTest := true,
    addTestReportOption(FuncTest, "int-test-reports")
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort := 9778)
