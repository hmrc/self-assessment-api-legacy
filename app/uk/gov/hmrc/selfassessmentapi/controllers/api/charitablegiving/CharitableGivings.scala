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

package uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving

import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case object CharitableGivings extends TaxYearPropertyType {

  override val name = "charitableGivings"

  override val title = "Sample charitable givings"

  override val example = toJson(CharitableGiving.example())

  override def description(action: String): String = s"$action a charitableGivings"

  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, "giftAidPayments.totalInTaxYear", "Total of Gift Aid payments made in the tax year", optional = true),
    PositiveMonetaryFieldDescription(name, "giftAidPayments.oneOff", "Total of any ‘one-off’ payments in the tax year", optional = true),
    PositiveMonetaryFieldDescription(name, "giftAidPayments.toNonUkCharities", "Total of Gift Aid payments to non-UK charities", optional = true),
    PositiveMonetaryFieldDescription(name, "giftAidPayments.carriedBackToPreviousTaxYear", "Total of Gift Aid payments made in the tax year, but treated as if made in the previous tax year", optional = true),
    PositiveMonetaryFieldDescription(name, "giftAidPayments.carriedFromNextTaxYear", "Total of Gift Aid payments made after the end of the tax year, but to be treated as if made in the tax year", optional = true),
    PositiveMonetaryFieldDescription(name, "sharesSecurities.totalInTaxYear", "Value of qualifying shares or securities gifted to charities", optional = true),
    PositiveMonetaryFieldDescription(name, "sharesSecurities.toNonUkCharities", "Value of qualifying shares or securities gifted to non-UK charities", optional = true),
    PositiveMonetaryFieldDescription(name, "landProperties.totalInTaxYear", "Value of qualifying land and buildings gifted to charities", optional = true),
    PositiveMonetaryFieldDescription(name, "landProperties.toNonUkCharities", "Value of qualifying land and buildings gifted to non-UK charities", optional = true)
  )
}
