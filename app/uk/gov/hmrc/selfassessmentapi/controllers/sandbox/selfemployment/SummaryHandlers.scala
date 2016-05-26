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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.selfemployment

import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SummaryHandler
import uk.gov.hmrc.selfassessmentapi.domain.SummaryId
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment._

object IncomesSummaryHandler extends SummaryHandler[Income] {
  override implicit val reads = Income.reads
  override implicit val writes = Income.writes
  override def example(id: SummaryId) = Income.example.copy(id = Some(id))
  override val listName = SummaryTypes.Incomes.name
}

object ExpensesSummaryHandler extends SummaryHandler[Expense] {
  override implicit val reads = Expense.reads
  override implicit val writes = Expense.writes
  override def example(id: SummaryId) = Expense.example.copy(id = Some(id))
  override val listName = SummaryTypes.Expenses.name
}

object BalancingChargesSummaryHandler extends SummaryHandler[BalancingCharge] {
  override implicit val reads = BalancingCharge.reads
  override implicit val writes = BalancingCharge.writes
  override def example(id: SummaryId) = BalancingCharge.example.copy(id = Some(id))
  override val listName = SummaryTypes.BalancingCharges.name
}

object GoodsAndServiceOwnUseSummaryHandler extends SummaryHandler[GoodsAndServicesOwnUse] {
  override implicit val reads = GoodsAndServicesOwnUse.reads
  override implicit val writes = GoodsAndServicesOwnUse.writes
  override def example(id: SummaryId) = GoodsAndServicesOwnUse.example.copy(id = Some(id))
  override val listName = SummaryTypes.GoodsAndServicesOwnUse.name
}
