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

package uk.gov.hmrc.r2.selfassessmentapi.config

import play.api.Configuration
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceType.SourceType

case class FeatureSwitch(value: Option[Configuration]) {
  val DEFAULT_VALUE = true

  def isEnabled(sourceType: SourceType, summary: Option[String]): Boolean = value match {
    case Some(config) =>
      summary match {
        case None | Some("") => FeatureConfig(config).isSourceEnabled(sourceType.toString)
        case Some(_summary) => FeatureConfig(config).isSummaryEnabled(sourceType.toString, _summary)
      }
    case None => DEFAULT_VALUE
  }
}

sealed case class FeatureConfig(config: Configuration) {

  def isSummaryEnabled(source: String, summary: String): Boolean = {
    isSourceEnabled(source) && config.getOptional[Boolean](s"$source.$summary.enabled").getOrElse(true)
  }

  def isSourceEnabled(source: String): Boolean = {
    config.getOptional[Boolean](s"$source.enabled").getOrElse(true)
  }
}
