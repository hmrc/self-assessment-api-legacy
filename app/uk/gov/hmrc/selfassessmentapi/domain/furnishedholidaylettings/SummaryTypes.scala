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

package uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain._

object SummaryTypes {

  case object Incomes extends SummaryType {
    override val name = "incomes"
    override lazy val example: JsValue = toJson(furnishedholidaylettings.Income.example)
    override val title = "Sample furnished holiday lettings income"
    override def description(action: String) = s"$action a furnished holiday lettings income summary for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount")
    )
  }

  case object Expenses extends SummaryType {
    override val name = "expenses"
    override lazy val example: JsValue = toJson(furnishedholidaylettings.Expense.example)
    override val title = "Sample expenses"
    override def description(action: String) = s"$action an expense for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("furnished-holiday-lettings", "type", "Enum", ExpenseType.values.mkString(", "), "Type of expense"),
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount")
    )
  }

  case object PrivateUseAdjustments extends SummaryType {
    override val name = "private-use-adjustments"
    override lazy val example: JsValue = toJson(furnishedholidaylettings.PrivateUseAdjustment.example)
    override val title = "Sample furnished holiday lettings private use adjustment"
    override def description(action: String) = s"$action a private use adjustment summary for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount")
    )
  }

  case object BalancingCharges extends SummaryType {
    override val name = "balancing-charges"
    override lazy val example: JsValue = toJson(furnishedholidaylettings.BalancingCharge.example)
    override val title = "Sample furnished holiday lettings balancing charge"
    override def description(action: String) = s"$action a furnished holiday lettings balancing charge summary for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("furnished-holiday-lettings", "amount")
    )
  }

}
