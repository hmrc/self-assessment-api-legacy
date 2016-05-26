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

package uk.gov.hmrc.selfassessmentapi.domain

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment

sealed trait SourceType extends Documentable {
  val name: String
  val example: JsValue
  val summaryTypes: Seq[SummaryType]
}

object SourceTypes {
  val types = Seq(SelfEmployments, FurnishedHolidayLettings, UKProperty)
  private val typesByName = types.map(x => x.name -> x).toMap

  def fromName(name: String): Option[SourceType] = typesByName.get(name)

  case object SelfEmployments extends SourceType {
    override val name = "self-employments"
    override lazy val example: JsValue = toJson(SelfEmployment.example)
    override val summaryTypes = Seq(selfemployment.SummaryTypes.Incomes, selfemployment.SummaryTypes.Expenses, selfemployment.SummaryTypes.GoodsAndServicesOwnUse, selfemployment.SummaryTypes.BalancingCharges)
    override val title = "Sample self-employments"

    override def description(action: String) = s"$action a self-employment"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "name", "String", "Painter", "Name of the self-employment"),
      FullFieldDescription(name, "commencementDate", "Date", "2016-01-01", "Date in yyyy-dd-mm format"),
      FullFieldDescription(name, "allowances", "Object", "", "Allowances claimed for this self-employment", optional = true),
      PositiveMonetaryFieldDescription(name, "annualInvestmentAllowance", optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowanceMainPool", optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowanceSpecialRatePool", optional = true),
      PositiveMonetaryFieldDescription(name, "restrictedCapitalAllowance", optional = true),
      PositiveMonetaryFieldDescription(name, "businessPremisesRenovationAllowance", optional = true),
      PositiveMonetaryFieldDescription(name, "enhancedCapitalAllowance", optional = true),
      PositiveMonetaryFieldDescription(name, "allowancesOnSales", optional = true),
      FullFieldDescription(name, "adjustments", "Object", "", "Adjustments for this self-employment", optional = true),
      PositiveMonetaryFieldDescription(name, "includedNonTaxableProfits", optional = true),
      MonetaryFieldDescription(name, "basisAdjustment", optional = true),
      PositiveMonetaryFieldDescription(name, "overlapReliefUsed", optional = true),
      PositiveMonetaryFieldDescription(name, "accountingAdjustment", optional = true),
      MonetaryFieldDescription(name, "averagingAdjustment", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward", optional = true),
      PositiveMonetaryFieldDescription(name, "outstandingBusinessIncome", optional = true)
    )
  }

  case object FurnishedHolidayLettings extends SourceType {
    override val name = "furnished-holiday-lettings"
    override lazy val example: JsValue = toJson(furnishedholidaylettings.FurnishedHolidayLettings.example)
    override val summaryTypes = Seq(furnishedholidaylettings.SummaryTypes.Incomes, furnishedholidaylettings.SummaryTypes.Expenses, furnishedholidaylettings.SummaryTypes.PrivateUseAdjustments, furnishedholidaylettings.SummaryTypes.BalancingCharges)
    override val title = "Sample furnished holiday lettings"

    override def description(action: String) = s"$action a furnished holiday letting"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "name", "String", "Holiday Cottage", "Identifier for the property"),
      FullFieldDescription(name, "propertyLocation", "Enum", PropertyLocationType.values.mkString(", "), "The location of the property"),
      FullFieldDescription(name, "allowances", "Object", "", "Allowances claimed for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowance"),
      FullFieldDescription(name, "adjustments", "Object", "", "Adjustments for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward")
    )
  }


  case object UKProperty extends SourceType {
    override val name = "uk-property"
    override lazy val example: JsValue = toJson(ukproperty.UKProperty.example)
    override val summaryTypes = Seq(ukproperty.SummaryTypes.Incomes, ukproperty.SummaryTypes.Expenses, ukproperty.SummaryTypes.TaxPaid, ukproperty.SummaryTypes.BalancingCharges)
    override val title = "Sample UK property"

    override def description(action: String) = s"$action a UK property"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "name", "String", "London Apartment", "Identifier for the property"),
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

