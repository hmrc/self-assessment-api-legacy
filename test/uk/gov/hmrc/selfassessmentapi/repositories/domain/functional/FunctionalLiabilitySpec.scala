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

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.{UnitSpec, domain}

class FunctionalLiabilitySpec extends UnitSpec {

  "MongoLiability.toLiability" should {

    "map to liability" in {

      val liability = new FunctionalLiability(BSONObjectID.generate, "", saUtr = SaUtr(""), taxYear = TaxYear(""),
        employmentIncome = Seq(
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
        selfEmploymentIncome = Seq(
          SelfEmploymentIncome(sourceId = "seId1", taxableProfit = 10, profit = 20),
          SelfEmploymentIncome(sourceId = "seId2", taxableProfit = 20, profit = 40)
        ),
        ukPropertyIncome = Seq(UkPropertyIncome("property1", profit = 2000)),
        furnishedHolidayLettingsIncome = Seq(
          FurnishedHolidayLettingIncome(sourceId = "fhlId1", profit = 20),
          FurnishedHolidayLettingIncome(sourceId = "fhlId2", profit = 40)
        ),
        savingsIncome = Seq(
          InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId1", totalInterest = 20),
          InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId2", totalInterest = 40)
        ),
        ukDividendsIncome = Seq(DividendsFromUKSources("divId1", totalDividend = 100)),
        totalIncomeReceived = 1000,
        totalTaxableIncome = 4000,
        allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(3000), incomeTaxRelief = Some(2000), retirementAnnuityContract = Some(1000)),
        taxDeducted = MongoTaxDeducted(
          interestFromUk = 50,
          deductionFromUkProperties = Seq(TaxPaidForUkProperty(sourceId = "propTaxPaid", taxPaid = 30)),
          ukTaxesPaidForEmployments = Seq(UkTaxPaidForEmployment(sourceId = "empTaxPaid", taxPaid = 20))),
        nonSavingsTaxBandSummary = Seq(
          TaxBandSummary("basicRate", taxableAmount = 100, chargedAt = "20%", tax = 20),
          TaxBandSummary("higherRate", taxableAmount = 100, chargedAt = "40%", tax = 40),
          TaxBandSummary("additionalHigherRate", taxableAmount = 100, chargedAt = "45%", tax = 45)
        ),
        savingsTaxBandSummary = Seq(
          TaxBandSummary("basicRate", taxableAmount = 200, chargedAt = "20%", tax = 40),
          TaxBandSummary("higherRate", taxableAmount = 200, chargedAt = "40%", tax = 80),
          TaxBandSummary("additionalHigherRate", taxableAmount = 200, chargedAt = "45%", tax = 90)
        ),
        dividendTaxBandSummary = Seq(
          TaxBandSummary("basicRate", taxableAmount = 300, chargedAt = "20%", tax = 60),
          TaxBandSummary("higherRate", taxableAmount = 300, chargedAt = "40%", tax = 120),
          TaxBandSummary("additionalHigherRate", taxableAmount = 300, chargedAt = "45%", tax = 135)
        ),
        totalIncomeTax = 630,
        totalTaxDeducted = 100,
        totalTaxDue = 530,
        totalTaxOverPaid = 0
      )

      val liabilityDto = liability.toLiability

      liabilityDto.income.incomes.nonSavings.employment should contain theSameElementsAs Seq(EmploymentIncome(sourceId = "eId1",
                                                                                              pay = 100,
                                                                                              benefitsAndExpenses = 50,
                                                                                              allowableExpenses = 50,
                                                                                              total = 100),
                                                                                            EmploymentIncome(sourceId = "eId2",
                                                                                              pay = 200,
                                                                                              benefitsAndExpenses = 100,
                                                                                              allowableExpenses = 100,
                                                                                              total = 200))

      liabilityDto.income.incomes.nonSavings.selfEmployment should contain theSameElementsAs Seq(SelfEmploymentIncome("seId1", taxableProfit = 10, profit = 20),
                                                                      SelfEmploymentIncome("seId2", taxableProfit = 20, profit = 40))

      liabilityDto.income.incomes.nonSavings.ukProperties should contain theSameElementsAs Seq(UkPropertyIncome("property1", profit = 2000))

      liabilityDto.income.incomes.nonSavings.furnishedHolidayLettings should contain theSameElementsAs Seq(FurnishedHolidayLettingIncome("fhlId1", 20),
                                                                                                            FurnishedHolidayLettingIncome("fhlId2", 40))

      liabilityDto.income.incomes.savings shouldBe SavingsIncomes(fromUKBanksAndBuildingSocieties = Seq(
          InterestFromUKBanksAndBuildingSocieties("interestId1", totalInterest = 20),
          InterestFromUKBanksAndBuildingSocieties("interestId2", totalInterest = 40)
        )
      )

      liabilityDto.income.incomes.dividends shouldBe DividendsIncomes(fromUKSources = Seq(DividendsFromUKSources("divId1", totalDividend = 100)))

      liabilityDto.income.incomes.total shouldBe 1000

      liabilityDto.income.deductions.get shouldBe domain.Deductions(personalAllowance = 3000, incomeTaxRelief = 2000,
                                                        retirementAnnuityContract = 1000,
                                                        total = 6000)

      liabilityDto.income.totalIncomeOnWhichTaxIsDue shouldBe 4000

      liabilityDto.incomeTaxCalculations.nonSavings should contain theSameElementsAs Seq(
        TaxBandSummary("basicRate", taxableAmount = 100, chargedAt = "20%", tax = 20),
        TaxBandSummary("higherRate", taxableAmount = 100, chargedAt = "40%", tax = 40),
        TaxBandSummary("additionalHigherRate", taxableAmount = 100, chargedAt = "45%", tax = 45)
      )

      liabilityDto.incomeTaxCalculations.savings should contain theSameElementsAs Seq(
        TaxBandSummary("basicRate", taxableAmount = 200, chargedAt = "20%", tax = 40),
        TaxBandSummary("higherRate", taxableAmount = 200, chargedAt = "40%", tax = 80),
        TaxBandSummary("additionalHigherRate", taxableAmount = 200, chargedAt = "45%", tax = 90)
      )

      liabilityDto.incomeTaxCalculations.dividends should contain theSameElementsAs Seq(
        TaxBandSummary("basicRate", taxableAmount = 300, chargedAt = "20%", tax = 60),
        TaxBandSummary("higherRate", taxableAmount = 300, chargedAt = "40%", tax = 120),
        TaxBandSummary("additionalHigherRate", taxableAmount = 300, chargedAt = "45%", tax = 135)
      )

      liabilityDto.taxDeducted.interestFromUk shouldBe 50
      liabilityDto.taxDeducted.fromEmployments shouldBe Seq(UkTaxPaidForEmployment(sourceId = "empTaxPaid", taxPaid = 20))
      liabilityDto.taxDeducted.fromUkProperties shouldBe Seq(TaxPaidForUkProperty(sourceId = "propTaxPaid", taxPaid = 30))

      liabilityDto.taxDeducted.total shouldBe 100

      liabilityDto.totalTaxDue shouldBe 530

      liabilityDto.totalTaxOverpaid shouldBe 0

    }
  }
}
