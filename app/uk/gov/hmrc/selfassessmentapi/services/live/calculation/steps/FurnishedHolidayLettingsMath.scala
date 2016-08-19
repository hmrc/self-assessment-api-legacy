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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoFurnishedHolidayLettings
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math.sum

trait FurnishedHolidayLettingsMath {

  protected def profitIncreases(furnishedHolidayLetting: MongoFurnishedHolidayLettings): BigDecimal = {
    val income = Some(furnishedHolidayLetting.incomes.map(_.amount).sum)
    val balancingCharges = Some(furnishedHolidayLetting.balancingCharges.map(_.amount).sum)
    val privateUseAdjustments = Some(furnishedHolidayLetting.privateUseAdjustment.map(_.amount).sum)
    sum(income, balancingCharges, privateUseAdjustments)
  }

  protected def profitReductions(selfEmployment: MongoFurnishedHolidayLettings): BigDecimal = {
    val expenses = Some(selfEmployment.expenses.map(_.amount).sum)
    val allowances = selfEmployment.allowances.flatMap(_.capitalAllowance)
    sum(expenses, allowances)
  }
}
