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

import play.api.mvc.hal._
import play.api.hal.HalLink
import play.api.libs.json.Json._
import play.api.mvc.{Action, AnyContent}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future

trait SelfEmploymentsBalancingChargesController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  def create(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    withJsonBody[BalancingCharge] { _ =>
      val balancingChargeId = BSONObjectID.generate.stringify
      Future.successful(Created(halResource(obj(), Seq(HalLink("self", selfEmploymentBalancingChargeHref(saUtr, taxYear, seId, balancingChargeId))))))
    }
  }

  def findById(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, balancingChargeId: SelfEmploymentBalancingChargeId) = Action.async { implicit request =>
    val balancingCharge = BalancingCharge(Some(balancingChargeId), BalancingChargeType.Other, BigDecimal("1000.45"))
    Future.successful(Ok(halResource(toJson(balancingCharge), Seq(HalLink("self", selfEmploymentBalancingChargeHref(saUtr, taxYear, seId, balancingChargeId))))))
  }

  def find(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId): Action[AnyContent] = Action { request =>
    val balancingCharges = Seq(BalancingCharge(Some("1234"), BalancingChargeType.Other, BigDecimal("1000.45")),
      BalancingCharge(Some("5678"), BalancingChargeType.BPRA, BigDecimal("1000.45")))

    val balancingChargesJson = toJson(balancingCharges.map(balancingCharge => halResource(obj(),
      Seq(HalLink("self", selfEmploymentBalancingChargeHref(saUtr, taxYear, seId,  balancingCharge.id.get))))))

    Ok(halResourceList("balancing-charges", balancingChargesJson, selfEmploymentBalancingChargesHref(saUtr, taxYear, seId)))
  }


  def delete(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, balancingChargeId: SelfEmploymentBalancingChargeId) = Action.async { implicit request =>
    Future.successful(NoContent)
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, balancingChargeId: SelfEmploymentBalancingChargeId) = Action.async(parse.json) { implicit request =>
    withJsonBody[BalancingCharge] { _ =>
      Future.successful(Ok(halResource(obj(), Seq(HalLink("self", selfEmploymentBalancingChargeHref(saUtr, taxYear, seId, balancingChargeId))))))
    }
  }
}
