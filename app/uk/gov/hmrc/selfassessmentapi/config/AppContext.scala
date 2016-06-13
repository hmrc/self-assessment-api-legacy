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

import com.typesafe.config.ConfigObject
import play.api.Play._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.selfassessmentapi.domain.SourceType

object AppContext extends ServicesConfig {
  lazy val appName = current.configuration.getString("appName").getOrElse(throw new RuntimeException("appName is not configured"))
  lazy val appUrl = current.configuration.getString("appUrl").getOrElse(throw new RuntimeException("appUrl is not configured"))
  lazy val apiGatewayContext = current.configuration.getString("api.gateway.context").getOrElse(throw new RuntimeException("api.gateway.context is not configured"))
  lazy val apiStatus = current.configuration.getString("api.status").getOrElse(throw new RuntimeException("api.status is not configured"))
  lazy val serviceLocatorUrl: String = baseUrl("service-locator")
  lazy val authUrl: String = baseUrl("auth")
  lazy val desUrl: String = baseUrl("des")
  lazy val registrationEnabled: Boolean = current.configuration.getBoolean(s"$env.microservice.services.service-locator.enabled").getOrElse(true)
  lazy val featureSwitch = current.configuration.getObject(s"$env.feature-switch")

  val supportedTaxYears: Seq[String] = Seq("2016-17")
}



