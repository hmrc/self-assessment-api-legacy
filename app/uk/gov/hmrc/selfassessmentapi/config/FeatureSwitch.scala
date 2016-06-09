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

import scala.collection.JavaConverters._

case class FeatureSwitch(value: Option[ConfigObject]) {
  implicit val DEFAULT_VALUE = false

  def isSourceEnabled(source: SourceType): Boolean = value match {
    case Some(config) => FeatureConfig(config).isSourceEnabled(source.name)
    case None => DEFAULT_VALUE
  }

  def isSummaryEnabled(sourceType: SourceType, summary: String): Boolean = value match {
    case Some(config) => FeatureConfig(config).isSummaryEnabled(sourceType.name, summary)
    case None => DEFAULT_VALUE
  }
}

case class FeatureConfig(config: ConfigObject)(implicit val defaultValue: Boolean) {
  val configMap = config.unwrapped()

  def isSummaryEnabled(source: String, summary: String): Boolean = {
    val sourceConfig = configMap.getOrDefault(source, Map("enabled" -> defaultValue).asJava).asInstanceOf[util.Map[String, AnyRef]]
    sourceConfig.getOrDefault("enabled", Boolean.box(defaultValue)).asInstanceOf[Boolean] ||
      sourceConfig.getOrDefault(summary, Map("enabled" -> defaultValue).asJava).asInstanceOf[util.Map[String, Boolean]]
        .getOrDefault("enabled", defaultValue)
  }

  def isSourceEnabled(source: String): Boolean = {
    configMap.getOrDefault(source, Map("enabled" -> false).asJava).asInstanceOf[util.Map[String, AnyRef]]
      .getOrDefault("enabled", Boolean.box(false)).asInstanceOf[Boolean]
  }
}
