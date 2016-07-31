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

package uk.gov.hmrc.selfassessmentapi.domain.employment

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain._

object SummaryTypes {

  case object Incomes extends SummaryType {
    override val name = "incomes"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Income.example(id))
    override val title = "Sample Employments incomes"
    override def description(action: String) = s"$action an income for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("employments", "type", "Enum", IncomeType.values.mkString(", "), "Type of income"),
      PositiveMonetaryFieldDescription("employments", "amount")
    )
  }

  case object Benefits extends SummaryType {
    override val name = "benefits"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Benefit.example(id))
    override val title = "Sample employment benefits"
    override def description(action: String) = s"$action a benefit for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("employments", "type", "Enum", BenefitType.values.mkString(", "), "Type of benefit"),
      PositiveMonetaryFieldDescription("employments", "amount")
    )
  }

  case object Expenses extends SummaryType {
    override val name = "expenses"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Expense.example(id))
    override val title = "Sample Employments expenses"
    override def description(action: String) = s"$action an expense for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("employments", "type", "Enum", ExpenseType.values.mkString(", "), "Type of expense"),
      PositiveMonetaryFieldDescription("employments", "amount")
    )
  }

  case object UkTaxesPaid extends SummaryType {
    override val name = "uk-taxes-paid"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(UkTaxPaid.example(id))
    override val title = "Sample UK Taxes paid"
    override def description(action: String) = s"$action an UK Tax paid for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("employments", "amount")
    )
  }
}
