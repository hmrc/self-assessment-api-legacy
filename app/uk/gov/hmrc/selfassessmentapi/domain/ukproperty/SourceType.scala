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

package uk.gov.hmrc.selfassessmentapi.domain.ukproperty

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SummaryTypes._

object SourceType {

  case object UKProperties extends SourceType {
    override val name = "uk-properties"
    override lazy val example: JsValue = toJson(UKProperty.example())
    override val summaryTypes: Seq[SummaryType] = Seq(Incomes, Expenses, TaxesPaid, BalancingCharges, PrivateUseAdjustments)
    override val title = "Sample UK property"

    override def description(action: String) = s"$action a UK property"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "allowances", "Object", "", "Allowances claimed for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "annualInvestmentAllowance", optional = true),
      PositiveMonetaryFieldDescription(name, "businessPremisesRenovationAllowance", optional = true),
      PositiveMonetaryFieldDescription(name, "otherCapitalAllowance", optional = true),
      PositiveMonetaryFieldDescription(name, "wearAndTearAllowance", optional = true),
      FullFieldDescription(name, "adjustments", "Object", "", "Adjustments for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward"),
      PositiveMonetaryFieldDescription(name, "rentARoomRelief", optional = true)
    )
  }

}
