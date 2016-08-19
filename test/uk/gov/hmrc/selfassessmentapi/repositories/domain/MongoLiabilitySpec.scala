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

import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand, NilTaxBand, SavingsStartingTaxBand}
import uk.gov.hmrc.selfassessmentapi.{SelfAssessmentSugar, UnitSpec}

class MongoLiabilitySpec extends UnitSpec with SelfAssessmentSugar with JsonSpec {

  "MongoLiability.toLiability" should {

    "map to liability" in {

      val liability = aLiability().copy(
          incomeFromEmployments = Seq(
              EmploymentIncome(sourceId = "eId1",
                               pay = 100,
                               benefitsAndExpenses = 50,
                               allowableExpenses = 50,
                               total = 100),
              EmploymentIncome(sourceId = "eId2",
                               pay = 200,
                               benefitsAndExpenses = 100,
                               allowableExpenses = 100,
                               total = 200)
          ),
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(sourceId = "seId1", taxableProfit = 10, profit = 20),
              SelfEmploymentIncome(sourceId = "seId2", taxableProfit = 20, profit = 40)
          ),
          incomeFromFurnishedHolidayLettings = Seq(
              FurnishedHolidayLettingIncome(sourceId = "fhlId1", profit = 20),
              FurnishedHolidayLettingIncome(sourceId = "fhlId2", profit = 40)
          ),
          interestFromUKBanksAndBuildingSocieties = Seq(
              InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId1", totalInterest = 20),
              InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId2", totalInterest = 40)
          ),
          dividendsFromUKSources = Seq(
              DividendsFromUKSources("divId1", totalDividend = 100)
          ),
          profitFromUkProperties = Seq(
              UkPropertyIncome("property1", profit = 2000)
          ),
          totalAllowancesAndReliefs = Some(20),
          totalIncomeReceived = Some(1000),
          allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(3000),
                                                      incomeTaxRelief = Some(2000),
                                                      retirementAnnuityContract = Some(1000)),
          totalIncomeOnWhichTaxIsDue = Some(4000)
      )

      liability.toLiability shouldBe Liability(
          income = IncomeSummary(
              incomes =
                IncomeFromSources(
                    nonSavings = NonSavingsIncomes(
                        employment =
                          Seq(
                              EmploymentIncome(sourceId = "eId1",
                                               pay = 100,
                                               benefitsAndExpenses = 50,
                                               allowableExpenses = 50,
                                               total = 100),
                              EmploymentIncome(sourceId = "eId2",
                                               pay = 200,
                                               benefitsAndExpenses = 100,
                                               allowableExpenses = 100,
                                               total = 200)
                          ),
                        selfEmployment = Seq(
                            SelfEmploymentIncome("seId1", taxableProfit = 10, profit = 20),
                            SelfEmploymentIncome("seId2", taxableProfit = 20, profit = 40)
                        ),
                        ukProperties = Seq(
                            UkPropertyIncome("property1", profit = 2000)
                        ),
                        furnishedHolidayLettings = Seq(
                            FurnishedHolidayLettingIncome("fhlId1", 20),
                            FurnishedHolidayLettingIncome("fhlId2", 40)
                        )
                    ),
                    savings =
                      SavingsIncomes(
                          fromUKBanksAndBuildingSocieties = Seq(
                              InterestFromUKBanksAndBuildingSocieties("interestId1", totalInterest = 20),
                              InterestFromUKBanksAndBuildingSocieties("interestId2", totalInterest = 40)
                          )
                      ),
                    dividends = DividendsIncomes(
                        fromUKSources = Seq(
                            DividendsFromUKSources("divId1", totalDividend = 100)
                        )
                    ),
                    total = 1000
                ),
              deductions =
                Some(Deductions(personalAllowance = 3000,
                                incomeTaxRelief = 2000,
                                retirementAnnuityContract = 1000,
                                total = 6000)),
              totalIncomeOnWhichTaxIsDue = 4000
          ),
          incomeTaxCalculations = IncomeTaxCalculations(Nil, Nil, Nil, 0),
          taxDeducted = TaxDeducted(0, Nil, Nil, 0),
          totalTaxDue = 0,
          totalTaxOverpaid = 0
      )
    }

    "map to liability and calculate the income tax charged" in {

      val liability = aLiability().copy(
          nonSavingsIncome = Seq(
              aTaxBandAllocation(1000, BasicTaxBand),
              aTaxBandAllocation(2000, HigherTaxBand),
              aTaxBandAllocation(2000, AdditionalHigherTaxBand)
          ),
          savingsIncome = Seq(
              aTaxBandAllocation(1000, SavingsStartingTaxBand),
              aTaxBandAllocation(1000, NilTaxBand),
              aTaxBandAllocation(1000, BasicTaxBand),
              aTaxBandAllocation(1000, HigherTaxBand),
              aTaxBandAllocation(1000, AdditionalHigherTaxBand)
          ),
          dividendsIncome = Seq(
              aTaxBandAllocation(1000, NilTaxBand),
              aTaxBandAllocation(1000, BasicTaxBand),
              aTaxBandAllocation(1000, HigherTaxBand),
              aTaxBandAllocation(1000, AdditionalHigherTaxBand)
          )
      )

      val result = liability.toLiability

      result.incomeTaxCalculations shouldBe IncomeTaxCalculations(
          nonSavings = Seq(
              aTaxBandSummary(BasicTaxBand.name, 1000, "20%", 200),
              aTaxBandSummary(HigherTaxBand.name, 2000, "40%", 800),
              aTaxBandSummary(AdditionalHigherTaxBand.name, 2000, "45%", 900)
          ),
          savings = Seq(
              aTaxBandSummary(SavingsStartingTaxBand.name, 1000, "0%", 0),
              aTaxBandSummary(NilTaxBand.name, 1000, "0%", 0),
              aTaxBandSummary(BasicTaxBand.name, 1000, "20%", 200),
              aTaxBandSummary(HigherTaxBand.name, 1000, "40%", 400),
              aTaxBandSummary(AdditionalHigherTaxBand.name, 1000, "45%", 450)
          ),
          dividends = Seq(
              aTaxBandSummary(NilTaxBand.name, 1000, "0%", 0),
              aTaxBandSummary(BasicTaxBand.name, 1000, "7.5%", 75),
              aTaxBandSummary(HigherTaxBand.name, 1000, "32.5%", 325),
              aTaxBandSummary(AdditionalHigherTaxBand.name, 1000, "38.1%", 381)
          ),
          total = 3731
      )
      result.totalTaxDue shouldBe 3731
      result.totalTaxOverpaid shouldBe 0
    }

    "map to liability and calculate the income tax overpaid if total tax is negative" in {

      val liability = aLiability().copy(
          savingsIncome = Seq(
              aTaxBandAllocation(1000, NilTaxBand)
          ),
          taxDeducted = Some(
              MongoTaxDeducted(
                  interestFromUk = 1000,
                  deductionFromUkProperties = Seq(TaxPaid(Some("property-1"), 500)),
                  totalDeductionFromUkProperties = 500,
                  ukTaxPaid = 0,
                  ukTaxesPaidForEmployments = Nil
              ))
      )
      val result = liability.toLiability
      result.totalTaxDue shouldBe 0
      result.totalTaxOverpaid shouldBe 1500
    }
  }

  "TaxBandSummary.+" should {

    "return a sum of taxable amounts from both band allocations" in {

      aTaxBandAllocation(500, BasicTaxBand) + aTaxBandAllocation(500, BasicTaxBand) shouldBe aTaxBandAllocation(
          1000,
          BasicTaxBand)
    }

    "throw IllegalStateException if the other band allocation is for a different band" in {

      intercept[IllegalArgumentException] {
        aTaxBandAllocation(100, BasicTaxBand) + aTaxBandAllocation(1000, HigherTaxBand)
      }
    }
  }

  private def aTaxBandSummary(taxBand: String, taxableAmount: BigDecimal, chargedAt: String, tax: BigDecimal) =
    TaxBandSummary(taxBand, taxableAmount, chargedAt, tax)

  "format LiabilityResult" should {

    "round trip MongoLiabilityCalculationError json" in {
      val calculationError: LiabilityResult = MongoLiabilityCalculationErrors
        .create(generateSaUtr(), taxYear, Seq(MongoLiabilityCalculationError(ErrorCode.INVALID_EMPLOYMENT_TAX_PAID, "Some error message")))
      roundTripJson(calculationError)

    }

    "round trip MongoLiability json" in {
      val mongoLiability: LiabilityResult = MongoLiability.create(generateSaUtr(), taxYear)
      roundTripJson(mongoLiability)

    }
  }
}
