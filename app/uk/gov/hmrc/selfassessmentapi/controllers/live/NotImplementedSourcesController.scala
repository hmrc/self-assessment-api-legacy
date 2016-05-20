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
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.{Links, BaseController, ErrorNotImplemented}
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SourceType, TaxYear, SelfEmploymentId}
import uk.gov.hmrc.selfassessmentapi.services.live.SelfEmploymentService
import play.api.mvc.Action

import scala.concurrent.Future

object NotImplementedSourcesController extends BaseController with Links {

  override val context: String = AppContext.apiGatewayContext

  def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = Action.async(parse.json) { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def findById(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def find(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = Action.async(parse.json)  { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  def delete(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }
}
