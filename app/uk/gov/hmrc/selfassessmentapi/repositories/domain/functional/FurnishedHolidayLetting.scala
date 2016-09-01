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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoFurnishedHolidayLettings

object FurnishedHolidayLetting {

  object AdjustedProfits {
    private def profitIncreases(furnishedHolidayLetting: MongoFurnishedHolidayLettings): BigDecimal = {
      Total(furnishedHolidayLetting.incomes) + Total(furnishedHolidayLetting.balancingCharges) +
        Total(furnishedHolidayLetting.privateUseAdjustment)
    }

    private def profitReductions(selfEmployment: MongoFurnishedHolidayLettings): BigDecimal = {
      Total(selfEmployment.expenses) + selfEmployment.capitalAllowance
    }

    def apply(furnishedHolidayLetting: MongoFurnishedHolidayLettings) =
      RoundDown(PositiveOrZero(profitIncreases(furnishedHolidayLetting) - profitReductions(furnishedHolidayLetting)))
  }

  object Incomes {
    def apply(selfAssessment: SelfAssessment): Seq[FurnishedHolidayLettingIncome] =
      selfAssessment.furnishedHolidayLettings.map { furnishedHolidayLetting =>
        FurnishedHolidayLettingIncome(sourceId = furnishedHolidayLetting.sourceId, profit = AdjustedProfits(furnishedHolidayLetting))
    }
  }

  object TotalProfit {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.furnishedHolidayLettings.map(AdjustedProfits(_)).sum
  }

  /*
  val furnishedHolidaysLettings =
        Seq(aFurnishedHolidayLetting().copy(incomes = Seq(fhlIncome(100)), adjustments = Some(fhlAdjustments(lossBroughtForward = 50))),
         aFurnishedHolidayLetting().copy(incomes = Seq(fhlIncome(350), fhlIncome(50), fhlIncome(100)), adjustments = Some(fhlAdjustments(lossBroughtForward = 200))),
         aFurnishedHolidayLetting(propertyLocation = EEA).copy(incomes = Seq(fhlIncome(50), fhlIncome(50)), adjustments = Some(fhlAdjustments(lossBroughtForward = 500))),
         aFurnishedHolidayLetting(propertyLocation = EEA).copy(incomes = Seq(fhlIncome(500)), adjustments = Some(fhlAdjustments(lossBroughtForward = 450)))
      )
   */

  object LossBroughtForward {
    def apply(furnishedHolidayLetting: MongoFurnishedHolidayLettings) = ValueOrZero(furnishedHolidayLetting.adjustments.flatMap(_.lossBroughtForward))
  }

  object TotalLossBroughtForward {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.furnishedHolidayLettings.groupBy(_.propertyLocation) map {
      case (location, furnishedHolidayLettings) =>
        CapAt(furnishedHolidayLettings.map(LossBroughtForward(_)).sum, furnishedHolidayLettings.map(AdjustedProfits(_)).sum)
    } sum
  }
}
