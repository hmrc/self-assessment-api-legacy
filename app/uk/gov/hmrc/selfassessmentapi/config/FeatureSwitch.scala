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

case class FeatureSwitch(value: Option[ConfigObject]) {
  def isSummaryEnabled(source: String, summary: String): Boolean = {
    isSourceEnabled(source, summary)
  }

  def isSourceEnabled(source: String, summary: String = ""): Boolean = value match {
    case Some(config) =>
      val configValue = config.get(source)
      if(configValue == null) true
      else {
        if (summary.isEmpty) configValue.unwrapped().asInstanceOf[util.HashMap[String, Boolean]].get("enabled")
        else {
          val summarySwitch = configValue.unwrapped().asInstanceOf[util.HashMap[String, util.HashMap[String, Boolean]]].get(summary)
          if(summarySwitch == null) true else summarySwitch.get("enabled")
        }
      }
    case None => true
  }
}
