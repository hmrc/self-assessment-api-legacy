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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import play.api.hal.HalLink
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.TaxYear
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.LiabilityService

import scala.concurrent.ExecutionContext.Implicits.global

object LiabilityController extends uk.gov.hmrc.selfassessmentapi.controllers.LiabilityController {

  override val context: String = AppContext.apiGatewayLinkContext

  private val liabilityService = LiabilityService()

  override def requestLiability(utr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    liabilityService.calculate(utr, taxYear) map { liabilityId =>
      val links = Set(
          HalLink("self", liabilityHref(utr, taxYear))
      )
      Accepted(halResource(JsObject(Nil), links))
    }
  }

  override def retrieveLiability(utr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    liabilityService.find(utr, taxYear) map {
      case Some(Left(error)) =>
        Forbidden(Json.toJson(error))
      case Some(Right(liability)) =>
        val links = Set(HalLink("self", liabilityHref(utr, taxYear)))
        Ok(halResource(Json.toJson(liability), links))
      case _ => notFound
    }
  }

}
