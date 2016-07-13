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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand}

class MongoLiabilitySpec extends UnitSpec {

  "MongoLiability.toLiability" should {

    "map to liability" in {

      val liability = MongoLiability.create(generateSaUtr(), taxYear).copy(
        profitFromSelfEmployments = Seq(
          SelfEmploymentIncome(sourceId = "seId1", taxableProfit = 10, profit = 20, lossBroughtForward = 15),
          SelfEmploymentIncome(sourceId = "seId2", taxableProfit = 20, profit = 40, lossBroughtForward = 30)
        ),
        interestFromUKBanksAndBuildingSocieties = Seq(
          InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId1", totalInterest = 20),
          InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId2", totalInterest = 40)
        ),
        dividendsFromUKSources = Seq(
          DividendsFromUKSources("divId1", totalDividend = 100)
        ),
        deductions = Some(Deductions(incomeTaxRelief = 20, totalDeductions = 20)),
        totalIncomeReceived = Some(1000),
        totalTaxableIncome = Some(2000),
        personalAllowance = Some(3000),
        totalIncomeOnWhichTaxIsDue = Some(4000)
      )

      liability.toLiability shouldBe Liability(
        id = Some(liability.liabilityId),
        income = IncomeSummary(
          incomes = IncomeFromSources(
            selfEmployment = Seq(
              Income("seId1", taxableProfit = 10, profit = 20),
              Income("seId2", taxableProfit = 20, profit = 40)
            ),
            interestFromUKBanksAndBuildingSocieties = Seq(
              InterestFromUKBanksAndBuildingSocieties("interestId1", totalInterest = 20),
              InterestFromUKBanksAndBuildingSocieties("interestId2", totalInterest = 40)
            ),
            dividendsFromUKSources = Seq(
              DividendsFromUKSources("divId1", totalDividend = 100)
            ),
            employment = Nil
          ),
          deductions = Some(Deductions(incomeTaxRelief = 20, totalDeductions = 20)),
          totalIncomeReceived = 1000,
          totalTaxableIncome = 2000,
          personalAllowance = 3000,
          totalIncomeOnWhichTaxIsDue = 4000
        ),
        incomeTaxCalculations = IncomeTaxCalculations(Nil, Nil, Nil, 0),
        credits = Nil,
        class4Nic = CalculatedAmount(Nil, 0),
        totalTaxDue = 0
      )
    }

    "map to liability and calculate the income tax charged" in {

      val liability = MongoLiability.create(generateSaUtr(), taxYear).copy(
        payPensionsProfits = Seq(
          aTaxBandSummary(1000, BasicTaxBand),
          aTaxBandSummary(2000, HigherTaxBand),
          aTaxBandSummary(2000, AdditionalHigherTaxBand)
        ),
        savingsInterest = Seq(
          aTaxBandSummary(1000, BasicTaxBand)
        ),
        dividends = Seq(
          aTaxBandSummary(1000, BasicTaxBand)
        )
      )

      liability.toLiability.incomeTaxCalculations shouldBe IncomeTaxCalculations(
        payPensionsProfits = Seq(
          aTaxBandSummary(BasicTaxBand.name, 1000, "20%", 200),
          aTaxBandSummary(HigherTaxBand.name, 2000, "40%", 800),
          aTaxBandSummary(AdditionalHigherTaxBand.name, 2000, "45%", 900)
        ),
        savingsInterest = Seq(
          aTaxBandSummary(BasicTaxBand.name, 1000, "20%", 200)
        ),
        dividends = Seq(
          aTaxBandSummary(BasicTaxBand.name, 1000, "20%", 200)
        ),
        incomeTaxCharged = 2300
      )
    }
  }

  "TaxBandSummary.tax" should {

    "return correct tax for all tax bands" in {
      aTaxBandSummary(1000, BasicTaxBand).tax shouldBe 200
      aTaxBandSummary(1000, HigherTaxBand).tax shouldBe 400
      aTaxBandSummary(1000, AdditionalHigherTaxBand).tax shouldBe 450
    }

    "return tax for given tax band and round down to the nearest pound" in {
      aTaxBandSummary(999, BasicTaxBand).tax shouldBe 199
      aTaxBandSummary(4, BasicTaxBand).tax shouldBe 0
    }
  }

  private def aTaxBandSummary(taxableAmount: BigDecimal, taxBand: TaxBand) = uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandSummary(taxableAmount, taxBand)

  private def aTaxBandSummary(taxBand: String, taxableAmount: BigDecimal, chargedAt: String, tax: BigDecimal) = uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary(taxBand, taxableAmount, chargedAt, tax)
}
