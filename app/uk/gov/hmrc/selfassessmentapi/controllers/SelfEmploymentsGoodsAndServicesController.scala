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
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentExpense, GoodsAndServicesOwnUse, SelfEmploymentId, TaxYear}
import scala.concurrent.Future

trait SelfEmploymentsGoodsAndServicesController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  def find(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action {
    val gs = GoodsAndServicesOwnUse(amount = BigDecimal("1000"))
    Ok(halResource(toJson(gs), Seq(HalLink("self", selfEmploymentGoodsAndServicesHref(saUtr, taxYear, seId)))))
  }

  def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    withJsonBody[GoodsAndServicesOwnUse] { gs =>
      Future.successful(Ok(halResource(toJson(gs), Seq(HalLink("self", selfEmploymentGoodsAndServicesHref(saUtr, taxYear, seId))))))
    }
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, selfEmploymentId: SelfEmploymentId) = Action {
    NoContent
  }
}
