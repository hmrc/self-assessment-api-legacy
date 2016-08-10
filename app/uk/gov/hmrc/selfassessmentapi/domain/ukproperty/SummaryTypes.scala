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

object SummaryTypes {

  case object Incomes extends SummaryType {
    override val name = "incomes"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Income.example(id))
    override val title = "Sample UK property incomes"
    override def description(action: String) = s"$action an income for the specified UK Property"
    override val fieldDescriptions = Seq(
      FullFieldDescription("uk-property", "type", "Enum", s"Type of income (one of the following: ${IncomeType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("uk-properties", "amount", "Income from all UK property & land (does not include any income/profits from FHL). Split by income type")
    )
  }

  case object Expenses extends SummaryType {
    override val name = "expenses"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Expense.example(id))
    override val title = "Sample UK property expenses"
    override def description(action: String) = s"$action an expense for the specified UK Property"
    override val fieldDescriptions = Seq(
      FullFieldDescription("uk-property", "type", "Enum", s"Type of expense (one of the following: ${ExpenseType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("uk-properties", "amount", "Running costs associated with the rental business, split by expense type")
    )
  }

  case object TaxesPaid extends SummaryType {
    override val name = "taxes-paid"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(TaxPaid.example(id))
    override val title = "Sample UK property taxes paid"
    override def description(action: String) = s"$action a tax paid for the specified UK Property"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("uk-properties", "amount", "For non-resident landlords - amount of tax taken off any property rental income.")
    )
  }

  case object BalancingCharges extends SummaryType {
    override val name = "balancing-charges"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(BalancingCharge.example(id))
    override val title = "Sample UK property balancing charges"
    override def description(action: String) = s"$action a balancing charge for the specified UK Property"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("uk-properties", "amount", "For items where capital allowances were claimed that have been sold," +
        " given away or stopped being used, a balancing charge may be needed.")
    )
  }

  case object PrivateUseAdjustments extends SummaryType {
    override val name = "private-use-adjustments"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(PrivateUseAdjustment.example(id))
    override val title = "Sample UK property private use adjustment"
    override def description(action: String) = s"$action a private use adjustment for the specified UK Property"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("uk-properties", "amount", "The private use (non-business) amount of any expenses " +
        "that weren’t solely for the business.")
    )
  }

}
