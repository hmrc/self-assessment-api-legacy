/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppContext @Inject()(
                            servicesConfig: ServicesConfig,
                            config: Configuration,
                            environment: Environment
                          )  {
  lazy val env: String = Environment.simple().mode.toString

  lazy val selfAssessmentContextRoute: String = config.getOptional[String](s"$env.contextPrefix").getOrElse("")
  lazy val desEnv: String = servicesConfig.getString(s"microservice.services.des.env")
  lazy val desToken: String = servicesConfig.getString(s"microservice.services.des.token")
  lazy val appName: String = servicesConfig.getString("appName")
  lazy val appUrl: String = servicesConfig.getString("appUrl")
  lazy val apiGatewayContext: Option[String] = config.getOptional[String]("api.gateway.context")
  lazy val apiGatewayRegistrationContext: String = apiGatewayContext.getOrElse(throw new RuntimeException("api.gateway.context is not configured"))
  lazy val apiGatewayLinkContext: String = apiGatewayContext.map(x => if (x.isEmpty) x else s"/$x").getOrElse("")
  lazy val apiStatus: String = servicesConfig.getString("api.status")
  lazy val desUrl: String = servicesConfig.baseUrl("des")
  lazy val featureSwitch: Option[Configuration] = config.getOptional[Configuration](s"$env.feature-switch")
  lazy val auditEnabled: Boolean = config.getOptional[Boolean](s"auditing.enabled").getOrElse(true)
  lazy val authEnabled: Boolean = config.getOptional[Boolean](s"microservice.services.auth.enabled").getOrElse(true)
  lazy val sandboxMode: Boolean = config.getOptional[Boolean](s"sandbox-mode").getOrElse(false)
  lazy val mtdDate: String = servicesConfig.getString(s"$env.mtd-date")
}
