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

package uk.gov.hmrc.r2.selfassessmentapi.connectors

import javax.inject.Inject
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.{AuthConnector, PlayAuthConnector}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient


class MicroserviceAuthConnector @Inject()(
                                           servicesConfig: ServicesConfig,
                                           configuration: Configuration,
                                           environment: Environment,
                                           override val http: DefaultHttpClient
                                         ) extends AuthConnector with PlayAuthConnector {
                                           override val serviceUrl: String = servicesConfig.baseUrl("auth")
                                           val authBaseUrl: String = servicesConfig.baseUrl("auth")
                                         }
