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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.furnishedholidaylettings

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.{Income, _}
import uk.gov.hmrc.selfassessmentapi.domain.{SourceTypes, SummaryType, _}

object FurnishedHolidayLettingsSourceHandler extends SourceHandler[FurnishedHolidayLetting] {
  override implicit val reads: Reads[FurnishedHolidayLetting] = FurnishedHolidayLetting.reads
  override implicit val writes: Writes[FurnishedHolidayLetting] = FurnishedHolidayLetting.writes
  override def example(id: SourceId) = FurnishedHolidayLetting.example.copy(id = Some(id))
  override val listName = SourceTypes.FurnishedHolidayLettings.name

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case PrivateUseAdjustments => Some(SummaryHandler(PrivateUseAdjustments.name, PrivateUseAdjustment))
      case Incomes => Some(SummaryHandler(Incomes.name, Income))
      case Expenses => Some(SummaryHandler(Expenses.name, Expense))
      case BalancingCharges => Some(SummaryHandler(BalancingCharges.name, BalancingCharge))
      case _ => None
    }
  }
}
