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

package uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._

object SummaryTypes {

  case object Incomes extends SummaryType {
    override val name = "incomes"
    override val documentationName = "Incomes"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Income.example(id))
    override val title = "Sample furnished holiday lettings income"
    override def description(action: String) = s"$action a furnished holiday lettings income summary for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount", "The total amount of income from all your Furnished Holiday Lettings " +
        "(including income from services you provide to tenants) for the given location (UK / EEA)")
    )
  }

  case object Expenses extends SummaryType {
    override val name = "expenses"
    override val documentationName = "Expenses"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Expense.example(id))
    override val title = "Sample expenses"
    override def description(action: String) = s"$action an expense for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("furnished-holiday-lettings", "type", "Enum", s"Type of expense (one of the following: ${ExpenseType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount", "Business costs related to the Furnished Holiday Letting, split out by expense type")
    )
  }

  case object PrivateUseAdjustments extends SummaryType {
    override val name = "private-use-adjustments"
    override val documentationName = "Private Use Adjustments"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(PrivateUseAdjustment.example(id))
    override val title = "Sample furnished holiday lettings private use adjustment"
    override def description(action: String) = s"$action a private use adjustment summary for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount", "The private use (non-business) amount of any expenses that weren’t solely for the business")
    )
  }

  case object BalancingCharges extends SummaryType {
    override val name = "balancing-charges"
    override val documentationName = "Balancing Charges"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(BalancingCharge.example(id))
    override val title = "Sample furnished holiday lettings balancing charge"
    override def description(action: String) = s"$action a furnished holiday lettings balancing charge summary for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount", "For items where capital allowances were claimed that have been sold, given away or stopped being used," +
        " a balancing charge may be needed")
    )
  }

}
