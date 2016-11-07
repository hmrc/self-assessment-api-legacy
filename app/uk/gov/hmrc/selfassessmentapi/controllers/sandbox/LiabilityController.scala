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

import play.api.hal.HalLink
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.hal._
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.Liability
import uk.gov.hmrc.selfassessmentapi.controllers.api._

import scala.concurrent.Future

object LiabilityController extends uk.gov.hmrc.selfassessmentapi.controllers.LiabilityController {

  override val context: String = AppContext.apiGatewayLinkContext

  override def requestLiability(nino: Nino, taxYear: TaxYear) = Action.async { request =>
      val links = Set(
        HalLink("self", liabilityHref(nino, taxYear))
      )
    Future.successful(Accepted(halResource(JsObject(Nil), links)))
  }

  override def retrieveLiability(nino: Nino, taxYear: TaxYear) = Action.async { request =>
    val liability = createLiability
    val links = Set(
      HalLink("self", liabilityHref(nino, taxYear))
    )
    Future.successful(Ok(halResource(Json.toJson(liability), links)))
  }

  def createLiability: Liability = Liability.example


}
