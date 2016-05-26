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
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.UKProperty
import uk.gov.hmrc.selfassessmentapi.domain.{SourceTypes, SummaryType, _}

object UKPropertySourceHandler extends SourceHandler[UKProperty] {
  override implicit val reads: Reads[UKProperty] = UKProperty.reads
  override implicit val writes: Writes[UKProperty] = UKProperty.writes
  override def example(id: SourceId) = UKProperty.example.copy(id = Some(id))
  override val listName = SourceTypes.UKProperty.name

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(IncomeSummaryHandler)
      case Expenses => Some(ExpenseSummaryHandler)
      case TaxPaid => Some(TaxPaidSummaryHandler)
      case BalancingCharges => Some(BalancingChargesSummaryHandler)
      case PrivateUseAdjustments => Some(PrivateUseAdjustmentsSummaryHandler)
      case _ => None
    }
  }
}
