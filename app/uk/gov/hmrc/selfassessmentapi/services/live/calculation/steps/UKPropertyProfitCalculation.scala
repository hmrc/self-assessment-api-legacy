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

import uk.gov.hmrc.selfassessmentapi.domain.Total
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

object UKPropertyProfitCalculation extends CalculationStep with Math {
  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {
    liability.copy(profitFromUkProperties = ukPropertyIncomes(selfAssessment))
  }

  def ukPropertyIncomes(selfAssessment: SelfAssessment): Seq[UkPropertyIncome] = {
    selfAssessment.ukProperties.map { property =>
      UkPropertyIncome(property.sourceId, profit = roundDown(adjustedProfit(property)),
        taxableProfit = positiveOrZero(roundDown(adjustedProfit(property) - property.lossBroughtForward)))
    }
  }

  def adjustedProfit(property: MongoUKProperties): BigDecimal = {
    positiveOrZero(Total(property.incomes) + Total(property.balancingCharges) + Total(property.privateUseAdjustment) -
      Total(property.expenses) - property.allowancesTotal - property.rentARoomReliefAmount)
  }

}
