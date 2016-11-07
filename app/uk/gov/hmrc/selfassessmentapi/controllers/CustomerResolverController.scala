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


import play.api.hal.HalLink
import play.api.libs.json.JsObject
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CustomerResolverController extends BaseController with Links {

  val confidenceLevel: ConfidenceLevel

  def nino(confidenceLevel: ConfidenceLevel)(implicit hc: HeaderCarrier): Future[Option[Nino]] = {
      Future.successful(Some(NinoGenerator().nextNino()))
  }

  final def resolve = Action.async { request =>
    nino(confidenceLevel)(hc(request)).map {
      case Some(nino) =>
        val links = Set(
          HalLink("self-assessment", discoverTaxYearsHref(nino))
        )
        Ok(halResource(JsObject(Nil), links))
      case None =>
        Unauthorized
    }
  }

}
