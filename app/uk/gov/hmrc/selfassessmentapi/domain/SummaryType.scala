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

sealed trait SummaryType {
  val name: String
  val example: JsValue
}

object SummaryTypes {
  val types = Seq(IncomesSummaryType, ExpensesSummaryType, BalancingChargesSummaryType, GoodsAndServicesOwnUseSummaryType)
  private val typesByName = types.map(x => x.name -> x).toMap

  def fromName(name: String): Option[SummaryType] = typesByName.get(name)
}
case object IncomesSummaryType extends SummaryType {
  override val name = "incomes"
  override lazy val example: JsValue = toJson(SelfEmploymentIncome.example)
}

case object ExpensesSummaryType extends SummaryType {
  override val name = "expenses"
  override lazy val example: JsValue = toJson(SelfEmploymentExpense.example)
}

case object BalancingChargesSummaryType extends SummaryType {
  override val name = "balancing-charges"
  override lazy val example: JsValue = toJson(BalancingCharge.example)
}

case object GoodsAndServicesOwnUseSummaryType extends SummaryType {
  override val name = "goods-and-services-own-use"
  override lazy val example: JsValue = toJson(GoodsAndServicesOwnUse.example)
}
