/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.config

import java.util

import com.typesafe.config.ConfigObject
import uk.gov.hmrc.selfassessmentapi.domain.SourceType

case class FeatureSwitch(value: Option[ConfigObject]) {
  def isEnabled(sourceType: SourceType, summary: String): Boolean = value match {
    case Some(config) =>
      FeatureConfig(config).isSourceEnabled(sourceType.name) &&
        (if (summary.isEmpty) true else FeatureConfig(config).isSummaryEnabled(sourceType.name, summary))
    case None => true
  }
}

case class FeatureConfig(config: ConfigObject) {
  val configMap = config.unwrapped()

  def isSummaryEnabled(source: String, summary: String): Boolean = {
    if (configMap.containsKey(source)) {
      val sourceConfig = configMap.get(source).asInstanceOf[util.Map[String, Object]]
      if (sourceConfig.containsKey("enabled")) sourceConfig.get("enabled").asInstanceOf[Boolean]
      else if (sourceConfig.containsKey(summary)) sourceConfig.get(summary).asInstanceOf[util.Map[String, Boolean]].get("enabled")
      else true
    }
    else true
  }

  def isSourceEnabled(source: String): Boolean = {
    if (configMap.containsKey(source)) {
      val sourceConfig = configMap.get(source).asInstanceOf[util.Map[String, Object]]
      if (sourceConfig.containsKey("enabled")) sourceConfig.get("enabled").asInstanceOf[Boolean]
      else true
    }
    else true
  }
}
