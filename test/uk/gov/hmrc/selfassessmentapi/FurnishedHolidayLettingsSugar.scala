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

package uk.gov.hmrc.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.ExpenseType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.Adjustments
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._

object FurnishedHolidayLettingsSugar {

  def aFurnishedHolidayLetting(id: SourceId = BSONObjectID.generate.stringify, saUtr: SaUtr = generateSaUtr(), taxYear: TaxYear = taxYear, propertyLocation: PropertyLocationType = UK) = FurnishedHolidayLettings(BSONObjectID.generate, id, saUtr, taxYear, now, now, propertyLocation)

  def fhlIncome(amount: BigDecimal, summaryId: SummaryId = BSONObjectID.generate.stringify) = FurnishedHolidayLettingsIncomeSummary(summaryId, amount)

  def fhlExpense(amount: BigDecimal, `type`: ExpenseType) = FurnishedHolidayLettingsExpenseSummary(BSONObjectID.generate.stringify, `type`, amount)

  def fhlBalancingCharge(amount: BigDecimal) = FurnishedHolidayLettingsBalancingChargeSummary(BSONObjectID.generate.stringify, amount)

  def fhlPrivateUseAdjustment(amount: BigDecimal) = FurnishedHolidayLettingsPrivateUseAdjustmentSummary(BSONObjectID.generate.stringify, amount)

  def fhlAdjustments(lossBroughtForward: BigDecimal) = Adjustments(lossBroughtForward = Some(lossBroughtForward))
}
