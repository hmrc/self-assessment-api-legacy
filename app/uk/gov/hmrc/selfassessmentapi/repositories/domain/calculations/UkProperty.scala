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

import uk.gov.hmrc.selfassessmentapi.controllers.api.{SelfAssessment, TaxPaidForUkProperty}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.UKProperties

object UKProperty {

  object TaxesPaid {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.ukProperties.map { property =>
      TaxPaidForUkProperty(property.sourceId, TaxPaid(property))
    }
  }

  object TaxPaid {
    def apply(ukProperty: UKProperties) = RoundUpToPennies(Total(ukProperty.taxesPaid))
  }

  object TotalTaxPaid {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.ukProperties.map(TaxPaid(_)).sum
  }

  object LossBroughtForward {
    def apply(ukProperty: UKProperties) = ValueOrZero(ukProperty.adjustments.flatMap(_.lossBroughtForward))
  }

  object CappedTotalLossBroughtForward {
    def apply(selfAssessment: SelfAssessment) = RoundUp(CapAt(selfAssessment.ukProperties.map(LossBroughtForward(_)).sum,
        selfAssessment.ukProperties.map(AdjustedProfits(_)).sum))
  }

  object AdjustedProfits {
    def apply(ukProperty: UKProperties) = {
      PositiveOrZero(Total(ukProperty.incomes) + Total(ukProperty.balancingCharges) + Total(ukProperty.privateUseAdjustment) -
        Total(ukProperty.expenses) - ukProperty.allowancesTotal - ukProperty.rentARoomReliefAmount)
    }
  }

  object Incomes {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.ukProperties.map { ukProperty =>
      UkPropertyIncome(ukProperty.sourceId, profit = RoundDown(AdjustedProfits(ukProperty)))
    }
  }

  object TotalProfit {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.ukProperties.map(AdjustedProfits(_)).sum
  }

  object TotalLossBroughtForward {
    def apply(selfAssessment: SelfAssessment) = RoundUp(selfAssessment.ukProperties.map(LossBroughtForward(_)).sum)
  }

}
