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

import play.api.mvc.Action
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentId, TaxYear}

trait SelfEmploymentsGoodsAndServicesController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  def find(utr: SaUtr, taxYear: TaxYear, selfEmploymentId: SelfEmploymentId) = Action {
    Ok
  }

  def createOrUpdate(utr: SaUtr, taxYear: TaxYear, selfEmploymentId: SelfEmploymentId) = Action {
    Ok
  }
  def delete(utr: SaUtr, taxYear: TaxYear, selfEmploymentId: SelfEmploymentId) = Action {
    Ok
  }
}
