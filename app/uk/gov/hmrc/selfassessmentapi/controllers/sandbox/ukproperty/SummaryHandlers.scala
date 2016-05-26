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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.ukproperty

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SummaryHandler
import uk.gov.hmrc.selfassessmentapi.domain.SummaryId
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty._

object IncomeSummaryHandler extends SummaryHandler[Income] {
  override implicit val reads = Income.reads
  override implicit val writes = Income.writes
  override def example(id: SummaryId) = Income.example.copy(id = Some(id))
  override val listName = SummaryTypes.Incomes.name
}

object ExpenseSummaryHandler extends SummaryHandler[Expenses] {
  override implicit val reads = Expenses.reads
  override implicit val writes = Expenses.writes
  override def example(id: SummaryId) = Expenses.example.copy(id = Some(id))
  override val listName = SummaryTypes.Expenses.name
}

object TaxPaidSummaryHandler extends SummaryHandler[TaxPaid] {
  override implicit val reads = TaxPaid.reads
  override implicit val writes = TaxPaid.writes
  override def example(id: SummaryId) = TaxPaid.example.copy(id = Some(id))
  override val listName = SummaryTypes.TaxPaid.name
}

object BalancingChargesSummaryHandler extends SummaryHandler[BalancingCharge] {
  override implicit val reads = BalancingCharge.reads
  override implicit val writes = BalancingCharge.writes
  override def example(id: SummaryId) = BalancingCharge.example.copy(id = Some(id))
  override val listName = SummaryTypes.BalancingCharges.name
}

object PrivateUseAdjustmentsSummaryHandler extends SummaryHandler[PrivateUseAdjustment] {
  override implicit val reads = PrivateUseAdjustment.reads
  override implicit val writes = PrivateUseAdjustment.writes
  override def example(id: SummaryId) = PrivateUseAdjustment.example.copy(id = Some(id))
  override val listName = SummaryTypes.PrivateUseAdjustments.name
}
