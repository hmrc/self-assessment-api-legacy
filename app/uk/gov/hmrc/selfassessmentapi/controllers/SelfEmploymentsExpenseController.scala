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
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentExpenseCategory._
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future

trait SelfEmploymentsExpenseController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  def findById(saUtr: SaUtr, seId: SelfEmploymentId, seExpenseId: SelfEmploymentExpenseId) = Action { request =>
    val seExpense = SelfEmploymentExpense(id = Some(seExpenseId), taxYear = "2016-17", category = CISPayments,
                                          amount= BigDecimal("1000.45"))
    Ok(halResource(toJson(seExpense), Seq(HalLink("self", selfEmploymentExpenseHref(saUtr, seId, seExpenseId)))))
  }

  def find(saUtr: SaUtr, seId: SelfEmploymentId): Action[AnyContent] = Action { request =>
    val seq = Seq(SelfEmploymentExpense(id = Some("1234"), taxYear = "2016-17", category = CISPayments,
                                                  amount = BigDecimal("1000.45")),
                            SelfEmploymentExpense(id = Some("5678"), taxYear = "2016-17", category = CoGBought,
                                                  amount = BigDecimal("2000.50")),
                            SelfEmploymentExpense(id = Some("4321"), taxYear = "2016-17", category = StaffCosts,
                                                  amount = BigDecimal("3000.50")))
    val seExpensesJson = toJson(seq.map(res => halResource(obj(),
      Seq(HalLink("self", selfEmploymentExpenseHref(saUtr, seId,  res.id.get))))))

    Ok(halResourceList("selfEmployments", seExpensesJson, selfEmploymentExpensesHref(saUtr, seId)))
  }

  def create(saUtr: SaUtr, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    withJsonBody[SelfEmploymentExpense] { selfEmploymentExpense =>
      val seExpenseId = BSONObjectID.generate.stringify
      Future.successful(Created(halResource(obj(), Seq(HalLink("self", selfEmploymentExpenseHref(saUtr, seId, seExpenseId))))))
    }
  }

  def update(saUtr: SaUtr, seId: SelfEmploymentId, seExpenseId: SelfEmploymentExpenseId) = Action.async(parse.json)  {
    implicit request =>
    withJsonBody[SelfEmploymentExpense] { seExpense =>
     Future.successful(Ok(halResource(obj(), Seq(HalLink("self", selfEmploymentExpenseHref(saUtr, seId, seExpense.id.get))))))
    }
  }

  def delete(saUtr: SaUtr, seId: SelfEmploymentId, seExpenseId: SelfEmploymentExpenseId) = Action {
    if(seExpenseId == "1234") NotFound else  NoContent
  }
}