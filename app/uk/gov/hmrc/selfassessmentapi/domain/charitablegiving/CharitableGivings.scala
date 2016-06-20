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

package uk.gov.hmrc.selfassessmentapi.domain.charitablegiving

import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain.{ObjectFieldDescription, PositiveMonetaryFieldDescription, TaxYearPropertyType}

case object CharitableGivings extends TaxYearPropertyType {

  override val name = "charitableGivings"

  override val title = "Sample charitable givings"

  override val example = toJson(CharitableGiving.example())

  override def description(action: String): String = s"$action a charitableGivings"

  override val fieldDescriptions = Seq(
    ObjectFieldDescription(name, "giftAidPayments", toJson(GiftAidPayments.example()), optional = true),
    PositiveMonetaryFieldDescription(name, "sharesSecurities", optional = true),
    PositiveMonetaryFieldDescription(name, "landProperties", optional = true),
    PositiveMonetaryFieldDescription(name, "qualifyingInvestmentsToNonUkCharities", optional = true)
  )
}
