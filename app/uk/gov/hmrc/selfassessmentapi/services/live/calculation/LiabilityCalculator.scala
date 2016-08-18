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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.selfassessmentapi.repositories.domain.{CalculationError, LiabilityResult, MongoLiability}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps._

class LiabilityCalculator {

  private val calculationSteps = Seq(
    EmploymentIncomeCalculation,
    SelfEmploymentProfitCalculation,
    FurnishedHolidayLettingsProfitCalculation,
    UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation,
    DividendsFromUKSourcesCalculation,
    UKPropertyProfitCalculation,
    TotalIncomeCalculation,
    IncomeTaxReliefCalculation,
    PersonalAllowanceCalculation,
    RetirementAnnuityContractCalculation,
    TotalAllowancesAndReliefsCalculation,
    TotalIncomeOnWhichTaxIsDueCalculation,
    PersonalSavingsAllowanceCalculation,
    SavingsStartingRateCalculation,
    NonSavingsIncomeTaxCalculation,
    SavingsIncomeTaxCalculation,
    DividendsTaxCalculation,
    TaxDeductedCalculation
  )

  def calculate(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {
    val (successLiabilities, errorLiabilities) = runSteps(selfAssessment, liability)

    if (errorLiabilities.isEmpty) successLiabilities.last else errorLiabilities.head
  }

  private[calculation] def runSteps(selfAssessment: SelfAssessment,
                                    liability: MongoLiability): (Stream[LiabilityResult], Stream[LiabilityResult]) = {
    val (successLiabilities, errorLiabilities) =
      calculationSteps.toStream
        .scanLeft(liability: LiabilityResult)((accLiability, step) =>
          accLiability match {
            case calculationError: CalculationError => calculationError
            case liability: MongoLiability => step.run(selfAssessment, liability)
          })
        .span {
          case _: MongoLiability => true
          case _: CalculationError => false
        }
    (successLiabilities, errorLiabilities)
  }
}

object LiabilityCalculator {

  def apply() = new LiabilityCalculator()
}
