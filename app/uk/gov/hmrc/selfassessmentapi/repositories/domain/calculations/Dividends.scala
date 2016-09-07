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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxBandSummary, DividendsFromUKSources, SelfAssessment}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{IncomeTax, MongoUnearnedIncome, TaxBand}

object Dividends {

  object TotalTaxableIncome {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(TotalIncome(selfAssessment),
      SelfEmployment.TotalProfit(selfAssessment), Savings.TotalIncome(selfAssessment), Deductions.Total(selfAssessment))

    def apply(totalDividendIncome: BigDecimal, totalProfits: BigDecimal, totalSavingsIncome: BigDecimal,
              totalDeduction: BigDecimal): BigDecimal =
      PositiveOrZero(totalDividendIncome - PositiveOrZero(totalDeduction - (totalProfits + totalSavingsIncome)))
  }

  object TotalIncome {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.unearnedIncomes.map(Income(_)).sum
  }

  private object Income {
    def apply(unearnedIncome: MongoUnearnedIncome) = RoundDown(unearnedIncome.dividends.map(_.amount).sum)
  }

  object FromUK {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.unearnedIncomes.map { unearnedIncome =>
      new DividendsFromUKSources(unearnedIncome.sourceId, Income(unearnedIncome))
    }
  }

  object PersonalAllowance {
    def apply(implicit selfAssessment: SelfAssessment): BigDecimal = apply(TotalTaxableIncome(selfAssessment))
    def apply(taxableDividend: BigDecimal): BigDecimal = CapAt(taxableDividend, 5000)
  }

  object IncomeTaxBandSummary  {

    def apply(taxableNonSavingsIncome: BigDecimal, taxableSavingsIncome: BigDecimal, taxableDividendIncome: BigDecimal,
              personalDividendAllowance: BigDecimal): Seq[TaxBandSummary] = {
      val nilTaxBand = TaxBand.NilTaxBand(bandWidth = personalDividendAllowance)
      val basicTaxBand = TaxBand.BasicTaxBand(Some(nilTaxBand), taxableNonSavingsIncome + taxableSavingsIncome, chargedAt = 7.5)
      val higherTaxBand = TaxBand.HigherTaxBand(basicTaxBand, taxableNonSavingsIncome + taxableSavingsIncome, chargedAt = 32.5)

      Seq(nilTaxBand, basicTaxBand, higherTaxBand, TaxBand.AdditionalHigherTaxBand(higherTaxBand, chargedAt = 38.1)).map { taxBand =>
        TaxBandAllocation(taxBand.allocate2(taxableDividendIncome), taxBand).toTaxBandSummary
      }
    }

    def apply(selfAssessment: SelfAssessment): Seq[TaxBandSummary] = apply(NonSavings.TotalTaxableIncome(selfAssessment), Savings.TotalTaxableIncome(selfAssessment),
      Dividends.TotalTaxableIncome(selfAssessment), Dividends.PersonalAllowance(selfAssessment))
  }

  object IncomeTax extends IncomeTax {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(IncomeTaxBandSummary(selfAssessment))
    def apply(taxBandSummaries: Seq[TaxBandSummary]): BigDecimal = incomeTax(taxBandSummaries)
  }

}




