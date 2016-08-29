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

import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType.SavingsIncomeType
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoUnearnedIncome, TaxBandAllocation}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{BasicTaxBand, HigherTaxBand, TaxBandRangeCheck}

object Savings {

  object TotalTaxPaid {
    def apply(selfAssessment: SelfAssessment): BigDecimal = {
      val totalTaxedInterest = Savings.TotalTaxedInterest(selfAssessment)
      RoundUpToPennies(RoundDown(totalTaxedInterest * 100 / 80) - totalTaxedInterest)
    }
  }

  object TotalTaxableIncome {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(TotalIncome(selfAssessment), Deductions.Total(selfAssessment),
      SelfEmployment.TotalProfit(selfAssessment))
    def apply(totalSavingsIncome: BigDecimal, totalDeduction: BigDecimal, totalProfitFromSelfEmployments: BigDecimal): BigDecimal = {
      PositiveOrZero(totalSavingsIncome - PositiveOrZero(totalDeduction - totalProfitFromSelfEmployments))
    }
  }

  object TotalIncome {
    def apply(selfAssessment: SelfAssessment) = Incomes(selfAssessment).map(_.totalInterest).sum
  }

  private object Interest {
    def apply(income: MongoUnearnedIncome, `type`: SavingsIncomeType.Value) = income.savings.filter(_.`type` == `type`).map(_.amount).sum
  }

  private object TaxedInterest {
    def apply(mongoUnearnedIncome: MongoUnearnedIncome) = Interest(mongoUnearnedIncome, SavingsIncomeType.InterestFromBanksTaxed)
  }

  private object UntaxedInterest {
    def apply(mongoUnearnedIncome: MongoUnearnedIncome) = Interest(mongoUnearnedIncome, SavingsIncomeType.InterestFromBanksUntaxed)
  }

  object TotalTaxedInterest {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.unearnedIncomes.map(TaxedInterest(_)).sum
  }

  object TotalUntaxedInterest {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.unearnedIncomes.map(UntaxedInterest(_)).sum
  }

  object Incomes {
    def apply(selfAssessment: SelfAssessment): Seq[InterestFromUKBanksAndBuildingSocieties] =
      selfAssessment.unearnedIncomes.map { income =>
        new InterestFromUKBanksAndBuildingSocieties(income.sourceId, RoundDown(TaxedInterest(income) * 100/80 + UntaxedInterest(income)))
      }
  }

  object StartingRate {
    private val startingRateLimit = BigDecimal(5000)
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(SelfEmployment.TotalProfit(selfAssessment), Deductions.Total(selfAssessment))
    def apply(profitFromSelfEmployments: BigDecimal, totalDeduction: BigDecimal): BigDecimal =
      PositiveOrZero(startingRateLimit - PositiveOrZero(profitFromSelfEmployments - totalDeduction))
  }

  object PersonalAllowance {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(Totals.TaxableIncome(selfAssessment))
    def apply(totalTaxableIncome: BigDecimal): BigDecimal = totalTaxableIncome match {
      case total if total < 1 => 0
      case total if total isWithin BasicTaxBand => 1000
      case total if total isWithin HigherTaxBand => 500
      case _ => 0
    }
  }

  object IncomeTaxBandSummary {
    def apply(selfAssessment: SelfAssessment): Seq[TaxBandSummary] = apply(Savings.TotalTaxableIncome(selfAssessment),
      Savings.StartingRate(selfAssessment), Savings.PersonalAllowance(selfAssessment), SelfEmployment.TotalTaxableProfit(selfAssessment))

    def apply(taxableSavingsIncome: BigDecimal, startingSavingsRate: BigDecimal, personalSavingsAllowance: BigDecimal,
              totalTaxableProfits: BigDecimal): Seq[TaxBandSummary] = {
      val startingTaxBand = TaxBands.SavingsStartingTaxBand(startingSavingsRate)
      val nilTaxBand = TaxBands.NilTaxBand(Some(startingTaxBand), personalSavingsAllowance)
      val basicTaxBand = TaxBands.BasicTaxBand(Some(nilTaxBand), totalTaxableProfits)
      val higherTaxBand = TaxBands.HigherTaxBand(basicTaxBand, totalTaxableProfits)

      Seq(startingTaxBand, nilTaxBand, basicTaxBand, higherTaxBand, TaxBands.AdditionalHigherTaxBand(higherTaxBand)).map { taxBand =>
        TaxBandAllocation(taxBand.allocate2(taxableSavingsIncome), taxBand).toTaxBandSummary
      }
    }
  }

  object IncomeTax extends IncomeTax {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(Savings.IncomeTaxBandSummary(selfAssessment))
    def apply(taxBandSummaries: Seq[TaxBandSummary]): BigDecimal = incomeTax(taxBandSummaries)
  }

}


