/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources.models.calculation

import play.api.libs.json.{Reads, _}
import uk.gov.hmrc.selfassessmentapi.resources.models.TaxYear

case class CalculationRequest(taxYear: TaxYear)

object CalculationRequest {

  implicit val reads: Reads[CalculationRequest] =
    (__ \ "taxYear").read[TaxYear].map(taxYear => CalculationRequest(taxYear))

  implicit val writes :Writes[CalculationRequest] = Json.writes[CalculationRequest]
}
