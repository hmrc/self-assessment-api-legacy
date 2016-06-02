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

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SummaryTypes.SavingsIncomes
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.{UnearnedIncome, _}
import uk.gov.hmrc.selfassessmentapi.domain.{SourceTypes, SummaryType, _}

object UnearnedIncomeSourceHandler extends SourceHandler[UnearnedIncome] {
  override implicit val reads: Reads[UnearnedIncome] = UnearnedIncome.reads
  override implicit val writes: Writes[UnearnedIncome] = UnearnedIncome.writes

  override def example(id: SourceId) = UnearnedIncome.example.copy(id = Some(id))

  override val listName = SourceTypes.UnearnedIncome.name

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] =
    summaryType match {
      case SavingsIncomes => Some(SummaryHandler(SavingsIncomes.name, SavingsIncome))
      case _ => None
    }
}
