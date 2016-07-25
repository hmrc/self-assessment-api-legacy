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

package uk.gov.hmrc.selfassessmentapi.domain.selfemployment

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes.{BalancingCharges, Expenses, GoodsAndServicesOwnUses, Incomes}

object SourceType {

  case object SelfEmployments extends SourceType {
    override val name = "self-employments"
    override def example(sourceId: Option[SourceId] = None): JsValue = toJson(SelfEmployment.example(sourceId))
    override val summaryTypes : Set[SummaryType] = Set(Incomes, Expenses, GoodsAndServicesOwnUses, BalancingCharges)
    override val title = "Sample self-employments"

    override def description(action: String) = s"$action a self-employment"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "commencementDate", "Date", "2016-01-01", "Date in yyyy-dd-mm format"),
      FullFieldDescription(name, "allowances", "Object", "", "Allowances claimed for this self-employment", optional = true),
      PositiveMonetaryFieldDescription(name, "annualInvestmentAllowance",
        """If you bought equipment (but not cars) on or after 6 April 2014, you can claim Annual Investment Allowance (AIA). Up to 31 December 2015 the maximum annual amount of AIA was £500,000.
          |From 1 January 2016 the maximum annual amount of AIA is £200,000
          |If you use the equipment for both business and private use, you’ll need to reduce the Annual Investment Allowance (AIA) you claim by the private use proportion""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowanceMainPool", "Capital allowances at 18% on equipment, including cars with lower CO2 emissions", optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowanceSpecialRatePool", "Capital allowances at 8% on equipment, including cars with higher CO2 emissions", optional = true),
      PositiveMonetaryFieldDescription(name, "restrictedCapitalAllowance", "Restricted capital allowances for cars costing more than £12,000 – if bought before 6 April 2009", optional = true),
      PositiveMonetaryFieldDescription(name, "businessPremisesRenovationAllowance", "Business Premises Renovation Allowance (BPRA) (Assisted Areas only)", optional = true),
      PositiveMonetaryFieldDescription(name, "enhancedCapitalAllowance", "100% and other enhanced capital allowances", optional = true),
      PositiveMonetaryFieldDescription(name, "allowancesOnSales", "Allowances on sale or cessation of business use (where you have disposed of assets for less than their tax value)", optional = true),
      FullFieldDescription(name, "adjustments", "Object", "", "Adjustments for this self-employment", optional = true),
      PositiveMonetaryFieldDescription(name, "includedNonTaxableProfits", "Income, receipts and other profits included in business income or expenses but not taxable as business profits", optional = true),
      MonetaryFieldDescription(name, "basisAdjustment",
        """You pay tax on the profits of your basis period for the tax year.
          |When you’ve been in business for a couple of years, the basis period is usually the 12-month accounting period.
          |Different rules apply when you start or cease a business or if you change accounting date""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "overlapReliefUsed", "Overlap relief used this year", optional = true),
      PositiveMonetaryFieldDescription(name, "accountingAdjustment", "Adjustment for change of accounting practice", optional = true),
      MonetaryFieldDescription(name, "averagingAdjustment", "Averaging adjustment (only for farmers, market gardeners and creators of literary or artistic works)", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward", "Loss brought forward from earlier years set off against\n\nthis year’s profits", optional = true),
      PositiveMonetaryFieldDescription(name, "outstandingBusinessIncome", "Any other business income, such as rebates received, and non arm’s length reverse premiums", optional = true)
    )
  }

}
