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

object SummaryTypes {

  case object Incomes extends SummaryType {
    override val name = "incomes"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Income.example(id))
    override val title = "Sample self-employment incomes"
    override def description(action: String) = s"$action an income for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("self-employments", "type", "Enum", "Type of income"),
      PositiveMonetaryFieldDescription("self-employments", "amount")
    )
  }

  case object Expenses extends SummaryType {
    override val name = "expenses"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Expense.example(id))
    override val title = "Sample self-employment expenses"
    override def description(action: String) = s"$action an expense for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("self-employments", "type", "Enum", "Type of expense"),
      PositiveMonetaryFieldDescription("self-employments", "amount")
    )
  }

  case object BalancingCharges extends SummaryType {
    override val name = "balancing-charges"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(BalancingCharge.example(id))
    override val title = "Sample self-employment balancing charges"
    override def description(action: String) = s"$action a balancing charge for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("self-employments", "type", "Enum", "Type of balancing charge"),
      PositiveMonetaryFieldDescription("self-employments", "amount")
    )
  }

  case object GoodsAndServicesOwnUses extends SummaryType {
    override val name = "goods-and-services-own-uses"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(GoodsAndServicesOwnUse.example(id))
    override val title = "Sample self-employment goods and service for own use"
    override def description(action: String) = s"$action a goods and service for own use summary for the specified source"
    override val fieldDescriptions = Seq(PositiveMonetaryFieldDescription("self-employments", "amount"))
  }

}
