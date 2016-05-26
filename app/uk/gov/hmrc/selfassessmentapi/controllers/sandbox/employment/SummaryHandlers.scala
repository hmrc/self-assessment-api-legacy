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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.employment

import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SummaryHandler
import uk.gov.hmrc.selfassessmentapi.domain.SummaryId
import uk.gov.hmrc.selfassessmentapi.domain.employment.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.domain.employment._

object IncomeSummaryHandler extends SummaryHandler[Income] {
  override implicit val reads = Income.reads
  override implicit val writes = Income.writes
  override def example(id: SummaryId) = Income.example.copy(id = Some(id))
  override val listName = Incomes.name
}

object BenefitsSummaryHandler extends SummaryHandler[Benefit] {
  override implicit val reads = Benefit.reads
  override implicit val writes = Benefit.writes
  override def example(id: SummaryId) = Benefit.example.copy(id = Some(id))
  override val listName = Benefits.name
}

object ExpenseSummaryHandler extends SummaryHandler[Expense] {
  override implicit val reads = Expense.reads
  override implicit val writes = Expense.writes
  override def example(id: SummaryId) = Expense.example.copy(id = Some(id))
  override val listName = SummaryTypes.Expenses.name
}
