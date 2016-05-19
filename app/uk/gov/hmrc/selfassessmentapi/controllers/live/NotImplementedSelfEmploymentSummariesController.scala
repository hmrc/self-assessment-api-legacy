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

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, ErrorNotImplemented, Links}
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future

object NotImplementedSelfEmploymentSummariesController extends BaseController with Links {

  override val context: String = AppContext.apiGatewayContext

  def create(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, summaryType: SummaryType.Value) = Action.async(parse.json) { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def findById(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, summaryType: SummaryType.Value, id: String) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def find(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, summaryType: SummaryType.Value) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, summaryType: SummaryType.Value, id: String) = Action.async(parse.json)  { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, summaryType: SummaryType.Value, id: String) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

}
