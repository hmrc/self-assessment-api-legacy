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

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppContext @Inject()(servicesConfig: ServicesConfig, config: Configuration) {

  lazy val desEnv: String   = servicesConfig.getString("microservice.services.des.env")
  lazy val desToken: String = servicesConfig.getString("microservice.services.des.token")
  lazy val desUrl: String   = servicesConfig.baseUrl("des")
  lazy val desEnvironmentHeaders: Option[Seq[String]] = config.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  lazy val featureSwitch: Option[Configuration] = config.getOptional[Configuration](s"feature-switch")

  lazy val authEnabled: Boolean = config.getOptional[Boolean]("microservice.services.auth.enabled").getOrElse(true)

  lazy val confidenceLevelDefinitionConfig: Boolean = servicesConfig.getBoolean(s"api.confidence-level-check.auth-validation.enabled")
}
