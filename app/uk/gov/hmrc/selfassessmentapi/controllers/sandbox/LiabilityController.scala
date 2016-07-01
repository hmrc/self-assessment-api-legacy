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

import java.util.UUID

import play.api.hal.HalLink
import play.api.libs.json.Json._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future

object LiabilityController extends uk.gov.hmrc.selfassessmentapi.controllers.LiabilityController {

  override val context: String = AppContext.apiGatewayContext

  override def requestLiability(utr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    val liabilityId = UUID.randomUUID().toString
      val links = Set(
        HalLink("self", liabilityHref(utr, taxYear, liabilityId))
      )
    Future.successful(Accepted(halResource(JsObject(Nil), links)))
  }

  override def retrieveLiability(utr: SaUtr, taxYear: TaxYear, liabilityId: String) = Action.async { request =>
    val liability = createLiability(liabilityId)
    val links = Set(
      HalLink("self", liabilityHref(utr, taxYear, liabilityId))
    )
    Future.successful(Ok(halResource(Json.toJson(liability), links)))
  }

  def createLiability(id: LiabilityId): Liability = Liability.example(id)


  override def deleteLiability(utr: SaUtr, taxYear: TaxYear, liabilityId: String) = Action.async { request =>
    Future.successful(NoContent)
  }

  override def find(saUtr: SaUtr, taxYear: TaxYear): Action[AnyContent] = Action.async { request =>
    val result= Seq(createLiability("1234"), createLiability("4321"), createLiability("7777"))
    val liabilities = toJson(
      result.map(liability => halResource(Json.toJson(liability), Set(HalLink("self", liabilityHref(saUtr, taxYear, liability.id.get)))))
    )
    Future.successful(Ok(halResourceList("liabilities", liabilities, liabilitiesHref(saUtr, taxYear))))
  }
}
