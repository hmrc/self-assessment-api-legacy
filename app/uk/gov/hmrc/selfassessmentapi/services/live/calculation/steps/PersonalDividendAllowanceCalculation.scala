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

import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.DividendsNilTaxBand
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoLiability, TaxBandAllocation}

import scala.math.BigDecimal

object PersonalDividendAllowanceCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {
    val personalDividendAllowance = capAt(calculateTaxableDividendIncome(liability), 5000)
    liability.copy(dividends = Seq(TaxBandAllocation(personalDividendAllowance, DividendsNilTaxBand)))
  }


  private def calculateTaxableDividendIncome(implicit liability: MongoLiability) = {
    profitFromSelfEmployments match {
      case profit if profit < (personalAllowance + incomeTaxRelief) =>
        savingsIncome match {
          case saving if saving == 0 => positiveOrZero(taxableDividendIncome)
          case saving if (profit + saving) < (personalAllowance + incomeTaxRelief) => positiveOrZero(taxableDividendIncome + saving)
          case _ => dividendsIncome
        }
      case _ => dividendsIncome
    }
  }

  private def savingsIncome(implicit liability: MongoLiability): BigDecimal = {
    liability.interestFromUKBanksAndBuildingSocieties.map(_.totalInterest).sum
  }

  private def dividendsIncome(implicit liability: MongoLiability): BigDecimal = {
    liability.dividendsFromUKSources.map(_.totalDividend).sum
  }

  private def profitFromSelfEmployments(implicit liability: MongoLiability): BigDecimal = {
    liability.profitFromSelfEmployments.map(_.profit).sum
  }

  private def incomeTaxRelief(implicit liability: MongoLiability): BigDecimal = {
    liability.deductions.map(_.incomeTaxRelief).getOrElse(BigDecimal(0))
  }

  private def remainingPersonalAllowance(implicit liability: MongoLiability) = {
    personalAllowance + incomeTaxRelief - profitFromSelfEmployments
  }

  private def taxableDividendIncome(implicit liability: MongoLiability) = {
    dividendsIncome - remainingPersonalAllowance
  }

  private def personalAllowance(implicit liability: MongoLiability): BigDecimal = {
    liability.allowancesAndReliefs.personalAllowance.getOrElse(BigDecimal(0))
  }

}
