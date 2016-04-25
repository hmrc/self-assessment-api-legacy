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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import uk.gov.hmrc.domain.{SaUtr, SaUtrGenerator}
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, MicroserviceAuthFilter}
import uk.gov.hmrc.selfassessmentapi.controllers.BaseCustomerResolverController

import scala.concurrent.Future

case object CustomerResolverController extends BaseCustomerResolverController {
  override val confidenceLevel: ConfidenceLevel = MicroserviceAuthFilter.authParamsConfig.authConfig(this.productPrefix).confidenceLevel
  override val context: String = AppContext.apiGatewayContext

  override def saUtr(confidenceLevel: ConfidenceLevel)(implicit hc: HeaderCarrier): Future[Option[SaUtr]] = {
    val utrGenerator = new SaUtrGenerator()
    Future.successful(Some(utrGenerator.nextSaUtr))
  }
}
