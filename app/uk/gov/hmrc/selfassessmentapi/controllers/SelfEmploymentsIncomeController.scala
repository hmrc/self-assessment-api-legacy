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
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentIncomeService

import scala.concurrent.ExecutionContext.Implicits.global

trait SelfEmploymentsIncomeController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  val selfEmploymentIncomeService: SelfEmploymentIncomeService

  def findById(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, seIncomeId: SelfEmploymentIncomeId): Action[AnyContent] = Action.async { request =>
    selfEmploymentIncomeService.findBySelfEmploymentIncomeId(saUtr, seId, seIncomeId) map {
      case Some(selfEmploymentIncome) => Ok(halResource(toJson(selfEmploymentIncome),
        Seq(HalLink("self",  selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.incomes, seIncomeId)))))
      case None => NotFound(toJson(ErrorNotFound))
    }
  }

  def find(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId): Action[AnyContent] = Action.async { request =>
    selfEmploymentIncomeService.find(saUtr) map { selfEmploymentIncomes =>
      val selfEmploymentIncomesJson = toJson(selfEmploymentIncomes.map(income => halResource(obj(),
        Seq(HalLink("self", selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.incomes, income.id.get))))))

      Ok(halResourceList("incomes", selfEmploymentIncomesJson, selfEmploymentSummaryTypeHref(saUtr, taxYear, seId, SummaryType.incomes)))
    }
  }

  def create(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    withJsonBody[SelfEmploymentIncome] { selfEmploymentIncome =>
      selfEmploymentIncomeService.create(selfEmploymentIncome) map { seIncomeId =>
        Created(halResource(obj(), Seq(HalLink("self", selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.incomes, seIncomeId)))))
      }
    }
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, seIncomeId: SelfEmploymentIncomeId): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[SelfEmploymentIncome] { selfEmploymentIncome =>
      selfEmploymentIncomeService.update(selfEmploymentIncome, saUtr, seId, seIncomeId) map { _ =>
        Ok(halResource(obj(), Seq(HalLink("self", selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.incomes, seIncomeId)))))
      }
    }
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, seIncomeId: SelfEmploymentIncomeId): Action[AnyContent] = Action.async { request =>
    selfEmploymentIncomeService.delete(saUtr, seId, seIncomeId) map { isDeleted =>
      if (isDeleted) NoContent else NotFound(toJson(ErrorNotFound))
    }
  }
}