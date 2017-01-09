/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import uk.gov.hmrc.selfassessmentapi.controllers.api.{FurnishedHolidayLettingIncome, SelfAssessment}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.repositories.domain.FurnishedHolidayLettings

object FurnishedHolidayLetting {

  object UK {
    object CappedTotalLossBroughtForward {

      private def excessUKPropertyLBF(selfAssessment: SelfAssessment): BigDecimal =
        PositiveOrZero(UKProperty.TotalLossBroughtForward(selfAssessment) - UKProperty.TotalProfit(selfAssessment))

      def apply(selfAssessment: SelfAssessment): BigDecimal =
        CapAt(selfAssessment.ukFurnishedHolidayLettings.map(LossBroughtForward(_)).sum + excessUKPropertyLBF(selfAssessment),
          selfAssessment.ukFurnishedHolidayLettings.map(AdjustedProfits(_)).sum)
    }
  }

  object EEA {
    object CappedTotalLossBroughtForward {
      def apply(selfAssessment: SelfAssessment): BigDecimal =
        CapAt(selfAssessment.eeaFurnishedHolidayLettings.map(LossBroughtForward(_)).sum,
          selfAssessment.eeaFurnishedHolidayLettings.map(AdjustedProfits(_)).sum)
    }
  }

  object AdjustedProfits {
    private def profitIncreases(furnishedHolidayLetting: FurnishedHolidayLettings): BigDecimal = {
      Total(furnishedHolidayLetting.incomes) + Total(furnishedHolidayLetting.balancingCharges) +
        Total(furnishedHolidayLetting.privateUseAdjustment)
    }

    private def profitReductions(selfEmployment: FurnishedHolidayLettings): BigDecimal = {
      Total(selfEmployment.expenses) + selfEmployment.capitalAllowance
    }

    def apply(furnishedHolidayLetting: FurnishedHolidayLettings) =
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

  object LossBroughtForward {
    def apply(furnishedHolidayLetting: FurnishedHolidayLettings) = ValueOrZero(furnishedHolidayLetting.adjustments.flatMap(_.lossBroughtForward))
  }

  object CappedTotalLossBroughtForward {
    def apply(selfAssessment: SelfAssessment) = RoundUp(UK.CappedTotalLossBroughtForward(selfAssessment) + EEA.CappedTotalLossBroughtForward(selfAssessment))

  }
}
