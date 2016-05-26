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
import uk.gov.hmrc.selfassessmentapi.domain.{FullFieldDescription, PositiveMonetaryFieldDescription}

object SummaryTypes {

  case object Incomes extends uk.gov.hmrc.selfassessmentapi.domain.SummaryType {
    override val name = "incomes"
    override lazy val example: JsValue = toJson(uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Income.example)
    override val title = "Sample UK property incomes"
    override def description(action: String) = s"$action an income for the specified UK Property"
    override val fieldDescriptions = Seq(
      FullFieldDescription("uk-property", "type", "Enum", IncomeType.values.mkString(", "), "Type of income"),
      PositiveMonetaryFieldDescription("uk-property", "amount")
    )
  }

  case object Expenses extends uk.gov.hmrc.selfassessmentapi.domain.SummaryType {
    override val name = "expenses"
    override lazy val example: JsValue = toJson(uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Expenses.example)
    override val title = "Sample UK property expenses"
    override def description(action: String) = s"$action an expense for the specified UK Property"
    override val fieldDescriptions = Seq(
      FullFieldDescription("uk-property", "type", "Enum", ExpenseType.values.mkString(", "), "Type of expense"),
      PositiveMonetaryFieldDescription("uk-property", "amount")
    )
  }

  case object TaxPaid extends uk.gov.hmrc.selfassessmentapi.domain.SummaryType {
    override val name = "tax-paid"
    override lazy val example: JsValue = toJson(uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid.example)
    override val title = "Sample UK property tax paid"
    override def description(action: String) = s"$action a tax paid for the specified UK Property"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("uk-property", "amount")
    )
  }

  case object BalancingCharges extends uk.gov.hmrc.selfassessmentapi.domain.SummaryType {
    override val name = "balancing-charges"
    override lazy val example: JsValue = toJson(uk.gov.hmrc.selfassessmentapi.domain.ukproperty.BalancingCharge.example)
    override val title = "Sample UK property balancing charge"
    override def description(action: String) = s"$action a balancing charge for the specified UK Property"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("uk-property", "amount")
    )
  }

}
