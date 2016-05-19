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
import play.api.libs.json.Json.{obj, toJson}
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmployment, SelfEmploymentId, SummaryType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentService

import scala.concurrent.ExecutionContext.Implicits.global

trait SelfEmploymentsController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  val selfEmploymentService: SelfEmploymentService

  def findById(utr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async { request =>
    selfEmploymentService.findBySelfEmploymentId(utr, seId) map {
      case Some(selfEmployment) => Ok(halResource(toJson(selfEmployment), selfEmploymentLinks(utr, taxYear, seId)))
      case None => NotFound(toJson(ErrorNotFound))
    }
  }

  def selfEmploymentLinks(utr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId): Seq[HalLink] = {
    Seq(
      HalLink("self", selfEmploymentHref(utr, taxYear, seId)),
      HalLink("incomes", selfEmploymentSummaryTypeHref(utr, taxYear, seId, SummaryType.incomes)),
      HalLink("expenses", selfEmploymentSummaryTypeHref(utr, taxYear, seId, SummaryType.expenses))
    )
  }

  def find(saUtr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    selfEmploymentService.find(saUtr) map { selfEmployments =>
      val selfEmploymentsJson = toJson(selfEmployments.map(res => halResource(obj(),
        Seq(HalLink("self", selfEmploymentHref(saUtr, taxYear, res.id.get))))))

      Ok(halResourceList("selfEmployments", selfEmploymentsJson, selfEmploymentsHref(saUtr, taxYear)))
    }
  }

  def create(saUtr: SaUtr, taxYear: TaxYear) = Action.async(parse.json) { implicit request =>
    withJsonBody[SelfEmployment] { selfEmployment =>
      selfEmploymentService.create(selfEmployment) map { seId =>
        Created(halResource(obj(), selfEmploymentLinks(saUtr, taxYear, seId)))
      }
    }
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    withJsonBody[SelfEmployment] { selfEmployment =>
      selfEmploymentService.update(selfEmployment, saUtr, seId) map { _ =>
        Ok(halResource(obj(), selfEmploymentLinks(saUtr, taxYear, seId)))
      }
    }
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async { request =>
    selfEmploymentService.delete(saUtr, seId).map { isDeleted =>
      if (isDeleted) NoContent else NotFound(toJson(ErrorNotFound))
    }
  }
}