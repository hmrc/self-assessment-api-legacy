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
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.ExpenseType._
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PropertyLocationType.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

trait FurnishedHolidayLettingsSugar extends SelfAssessmentSugar {

  this: UnitSpec =>

  def aFurnishedHolidayLetting(id: SourceId = BSONObjectID.generate.stringify, saUtr: SaUtr = generateSaUtr(), taxYear: TaxYear = taxYear, propertyLocation: PropertyLocationType = UK) = MongoFurnishedHolidayLettings(BSONObjectID.generate, id, saUtr, taxYear, now, now, propertyLocation)

  def income(amount: BigDecimal, summaryId: SummaryId = BSONObjectID.generate.stringify) = MongoFurnishedHolidayLettingsIncomeSummary(summaryId, amount)

  def expense(amount: BigDecimal, `type`: ExpenseType) = MongoFurnishedHolidayLettingsExpenseSummary(BSONObjectID.generate.stringify, `type`, amount)

  def balancingCharge(amount: BigDecimal) = MongoFurnishedHolidayLettingsBalancingChargeSummary(BSONObjectID.generate.stringify, amount)

  def privateUseAdjustment(amount: BigDecimal) = MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary(BSONObjectID.generate.stringify, amount)
}
