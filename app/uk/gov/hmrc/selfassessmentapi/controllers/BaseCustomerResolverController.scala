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

package uk.gov.hmrc.selfassessmentapi.controllers


import play.api.libs.json.JsObject
import play.api.mvc.Request
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.connectors.AuthConnector

import play.api.mvc.hal._

import scala.concurrent.ExecutionContext.Implicits.global

trait BaseCustomerResolverController extends BaseController with HeaderValidator {

  val authConnector: AuthConnector
  val confidenceLevel: ConfidenceLevel
  def hc(request: Request[Any]): HeaderCarrier

  final def resolve = validateAccept(acceptHeaderValidationRules).async { request =>
    authConnector.saUtr(confidenceLevel)(hc(request)).map {
      case Some(saUtr) =>
        val links = Seq(
          Link("self-assessment", selfAssessmentUrl(saUtr))
        )
        Ok(halResource(JsObject(Nil), links))
      case None =>
        Unauthorized
    }
  }

  def selfAssessmentUrl(saUtr: SaUtr): String

}

trait CustomerResolverControllerWithUrls extends BaseCustomerResolverController {
  def selfAssessmentUrl(saUtr: SaUtr): String = s"/self-assessment/$saUtr"
}
