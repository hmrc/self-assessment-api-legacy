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
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentIncomeService
import uk.gov.hmrc.selfassessmentapi.services.live.SelfEmploymentIncomeService

import scala.concurrent.Future

object SelfEmploymentsIncomeController extends uk.gov.hmrc.selfassessmentapi.controllers.SelfEmploymentsIncomeController {

  override val selfEmploymentIncomeService: SelfEmploymentIncomeService = SelfEmploymentIncomeService

  override def create(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async(parse.json) { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, seIncomeId: SelfEmploymentIncomeId) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def find(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def update(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, seIncomeId: SelfEmploymentIncomeId) = Action.async(parse.json)  { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def delete(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, seIncomeId: SelfEmploymentIncomeId) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

}
