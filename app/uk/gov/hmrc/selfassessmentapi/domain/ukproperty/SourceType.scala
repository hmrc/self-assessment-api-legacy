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

    override def example(sourceId: Option[SourceId] = None): JsValue = toJson(UKProperty.example(sourceId))

    override val summaryTypes: Set[SummaryType] = Set(Incomes, Expenses, TaxesPaid, BalancingCharges, PrivateUseAdjustments)
    override val title = "Sample UK property"

    override def description(action: String) = s"$action a UK property"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "allowances", "Object", "", "Allowances claimed for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "annualInvestmentAllowance",
        """You can claim Annual Investment Allowance
          |(AIA) if you bought equipment (but not cars)
          |during the year. You can claim AIA up to
          |a maximum annual amount. You can’t claim AIA
          |for expenditure on equipment and other items for
          |use in a dwelling house""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "businessPremisesRenovationAllowance",
        """You may be able to claim 100% BPRA for the
          |cost of renovating or repairing business premises.
          |To qualify, it must be in an Assisted Area
          |and unused for at least 1 year before the work to
          |bring them back into business use started""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "otherCapitalAllowance",
        """The type of capital allowance and amount that
          |you can claim will depend on the cost, type of
          |asset and other circumstances. For example,
          |you can only claim capital allowances for
          |furniture and fixtures or other equipment for use
          |in a dwelling house if it qualifies as a Furnished
          |Holiday Letting""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "wearAndTearAllowance", "10% wear and tear allowance", optional = true),
      FullFieldDescription(name, "adjustments", "Object", "", "Adjustments for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward", "Loss brought forward used against this year’s profits"),
      PositiveMonetaryFieldDescription(name, "rentARoomRelief", "Rent a Room exempt amount", optional = true)
    )
  }

}
