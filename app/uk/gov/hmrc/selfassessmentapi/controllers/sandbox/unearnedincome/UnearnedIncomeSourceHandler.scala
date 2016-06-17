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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.unearnedincome

import uk.gov.hmrc.selfassessmentapi.controllers.SourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SummaryHandler
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes.UnearnedIncomes
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SummaryTypes.{Benefits, Dividends, SavingsIncomes}
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.{UnearnedIncome, _}
import uk.gov.hmrc.selfassessmentapi.domain.{SummaryType, _}
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.SandboxSourceRepository

object UnearnedIncomeSourceHandler extends SourceHandler(UnearnedIncome, UnearnedIncomes.name) {
  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] =
    summaryType match {
      case SavingsIncomes => Some(SummaryHandler(SavingsIncomes.name, SavingsIncome))
      case Dividends => Some(SummaryHandler(Dividends.name, Dividend))
      case Benefits => Some(SummaryHandler(Benefits.name, Benefit))
      case _ => None
    }

  override val repository = new SandboxSourceRepository[UnearnedIncome] {
    override implicit val writes = UnearnedIncome.writes
    override def example(id: SourceId) = UnearnedIncome.example().copy(id = Some(id))
  }
}
