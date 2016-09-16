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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

case class FurnishedHolidayLettingBuilder(capitalAllowance: BigDecimal = 0,
                                          location: PropertyLocationType = PropertyLocationType.UK) {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._

  private val objectID = BSONObjectID.generate

  private var furnishedHolidayLetting: FurnishedHolidayLettings =
    FurnishedHolidayLettings(objectID, objectID.stringify, generateSaUtr(), taxYear, now, now, location, allowances = Some(Allowances(Some(capitalAllowance))))

  def lossBroughtForward(amount: BigDecimal) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(adjustments = Some(Adjustments(lossBroughtForward = Some(amount))))
    this
  }

  def incomes(incomes: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(incomes = incomes.map (FurnishedHolidayLettingsIncomeSummary("", _)))
    this
  }

  def expenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(expenses = expenses.map (expense => FurnishedHolidayLettingsExpenseSummary("", expense._1, expense._2)))
    this
  }

  def balancingCharges(amounts: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(balancingCharges = amounts.map (FurnishedHolidayLettingsBalancingChargeSummary("", _)))
    this
  }

  def privateUseAdjustments(amounts: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(privateUseAdjustment = amounts.map (FurnishedHolidayLettingsPrivateUseAdjustmentSummary("", _)))
    this
  }

  def create() = furnishedHolidayLetting
}
