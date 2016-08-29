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

import org.scalacheck.Gen
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.Tables.Table
import uk.gov.hmrc.selfassessmentapi.UnearnedIncomesSugar._
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoUnearnedIncomesSavingsIncomeSummary
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.{UnitSpec, domain}

class SavingsSpec extends UnitSpec {

  "Interest from UK banks and building societies" should {
    def taxedInterest(amount: BigDecimal) = MongoUnearnedIncomesSavingsIncomeSummary("", InterestFromBanksTaxed, amount)
    def unTaxedInterest(amount: BigDecimal) = MongoUnearnedIncomesSavingsIncomeSummary("", InterestFromBanksUntaxed, amount)

    "calculate rounded down interest when there are multiple interest of both taxed and unTaxed from uk banks and building societies from" +
      " multiple unearned income source" in {

      val unearnedIncomes1 = anIncome().copy(savings = Seq(taxedInterest(100.50), unTaxedInterest(200.50)))
      val unearnedIncomes2 = anIncome().copy(savings = Seq(taxedInterest(300.99), unTaxedInterest(400.99)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes1, unearnedIncomes2))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes1.sourceId, BigDecimal(326)),
          domain.InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes2.sourceId, BigDecimal(777)))
    }

    "calculate interest when there is one taxed interest from uk banks and building societies from a single unearned income source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(taxedInterest(100)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(125)))

    }

    "calculate interest when there are multiple taxed interest from uk banks and building societies from a single unearned income source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(taxedInterest(100), taxedInterest(200)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(375)))

    }

    "calculate round down interest when there is one taxed interest from uk banks and building societies from a single unearned income " +
      "source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(taxedInterest(100.50)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(125)))
    }

    "calculate round down interest when there are multiple taxed interest from uk banks and building societies from a single unearned " +
      "income source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(taxedInterest(100.90), taxedInterest(200.99)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(377)))

    }

    "calculate interest when there is one unTaxed interest from uk banks and building societies from a single unearned income source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(unTaxedInterest(100)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(100)))

    }

    "calculate interest when there are multiple unTaxed interest from uk banks and building societies from a single unearned income " +
      "source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(unTaxedInterest(100), unTaxedInterest(200)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(300)))
    }


    "calculate rounded down interest when there is one unTaxed interest from uk banks and building societies from a single unearned " +
      "income source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(unTaxedInterest(100.50)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(100)))
    }

    "calculate rounded down interest when there are multiple unTaxed interest from uk banks and building societies from a single unearned" +
      " income source" in {
      val unearnedIncomes = anIncome().copy(savings = Seq(unTaxedInterest(100.50), unTaxedInterest(200.99)))

      Savings.Incomes(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) should contain theSameElementsAs
        Seq(domain.InterestFromUKBanksAndBuildingSocieties(unearnedIncomes.sourceId, BigDecimal(301)))
    }
  }

  "SavingsStartingRate" should {

    "be 5000 if payPensionProfitsReceived is less than deductions" in {
      Savings.StartingRate(profitFromSelfEmployments = 5000, totalDeduction = 6000) shouldBe 5000
    }

    "be 5000 if payPensionProfitsReceived is equal to deductions" in {
      Savings.StartingRate(profitFromSelfEmployments = 6000, totalDeduction = 6000) shouldBe 5000
    }

    "be the startingRateLimit - positiveOfZero(totalProfit - totalDeductions)" in {
      Savings.StartingRate(profitFromSelfEmployments = 9000, totalDeduction = 6000) shouldBe 2000
    }

    "return 0 if payPensionProfitsReceived is equal to deductions + startingRateLimit" in {
      Savings.StartingRate(profitFromSelfEmployments = 11000, totalDeduction = 6000) shouldBe 0
    }

    "return 0 if payPensionProfitsReceived is more than deductions + startingRateLimit" in {
      Savings.StartingRate(profitFromSelfEmployments = 12000, totalDeduction = 6000) shouldBe 0
    }
  }

  "Savings.PersonalSavingsAllowance" should {
    def generate(lowerLimit: Int, upperLimit: Int) = for {value <- Gen.chooseNum(lowerLimit, upperLimit)} yield value

    "be zero when the total income on which tax is due is zero" in {
      Savings.PersonalAllowance(totalTaxableIncome = 0) shouldBe 0
    }

    "be 1000 when the total income on which tax is due is less than equal to 32000 " in {
      Savings.PersonalAllowance(totalTaxableIncome = 1) shouldBe 1000
      generate(1, 32000) map { randomNumber => Savings.PersonalAllowance(totalTaxableIncome = randomNumber) shouldBe 1000 }
      Savings.PersonalAllowance(totalTaxableIncome = 32000) shouldBe 1000
    }

    "be 500 when the total income on which tax is due is greater than 32000 but less than equal to 150000" in {
      Savings.PersonalAllowance(totalTaxableIncome = 32001) shouldBe 500
      generate(32001, 150000) map { randomNumber => Savings.PersonalAllowance(totalTaxableIncome = randomNumber) shouldBe 500 }
      Savings.PersonalAllowance(totalTaxableIncome = 150000) shouldBe 500
    }

    "be 0 when the total income on which tax is due is greater than 150000" in {
      Savings.PersonalAllowance(totalTaxableIncome = 150001) shouldBe 0
      generate(150001, Int.MaxValue) map { randomNumber => Savings.PersonalAllowance(totalTaxableIncome = randomNumber) shouldBe 0 }
    }
  }

  "Savings.TotalTaxableIncome" should {
    "be equal to TotalSavingsIncomes - ((PersonalAllowance + IncomeTaxRelief) - ProfitsFromSelfEmployments) if ProfitsFromSelfEmployments" +
      " < (PersonalAllowance + IncomeTaxRelief) " in {
      Savings.TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalProfitFromSelfEmployments = 2000) shouldBe 3000
      Savings.TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalProfitFromSelfEmployments = 3999) shouldBe 4999
    }

    "be equal to TotalSavingsIncomes if ProfitsFromSelfEmployments >= (PersonalAllowance + IncomeTaxRelief) " in {
      Savings.TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalProfitFromSelfEmployments = 4000) shouldBe 5000
      Savings.TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalProfitFromSelfEmployments = 4001) shouldBe 5000
      Savings.TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalProfitFromSelfEmployments = 4500) shouldBe 5000
    }
  }

  "Savings.TotalTaxPaid" should {

    "be 0 when there is no interest from banks" in {
      Savings.TotalTaxPaid(SelfAssessment()) shouldBe 0
    }

    "be 0 if sum of all taxed interests is 0" in {

      val unearnedIncomes = anIncome().copy(savings = Seq(
          aSavingsIncome("", InterestFromBanksTaxed, 0),
          aSavingsIncome("", InterestFromBanksTaxed, 0),
          aSavingsIncome("", InterestFromBanksUntaxed, 0)))

      Savings.TotalTaxPaid(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) shouldBe 0

    }

    "be equal to Sum(Taxed Interest) * 100/80 - Sum(Taxed Interest)" in {

      Savings.TotalTaxPaid(SelfAssessment(unearnedIncomes = Seq(anIncome().copy(savings = Seq(
        aSavingsIncome("", InterestFromBanksTaxed, 100),
        aSavingsIncome("", InterestFromBanksTaxed, 200),
        aSavingsIncome("", InterestFromBanksTaxed, 2000),
        aSavingsIncome("", InterestFromBanksUntaxed, 500)))))) shouldBe 575

      Savings.TotalTaxPaid(SelfAssessment(unearnedIncomes = Seq(anIncome().copy(savings = Seq(
        aSavingsIncome("", InterestFromBanksTaxed, 400),
        aSavingsIncome("", InterestFromBanksTaxed, 700),
        aSavingsIncome("", InterestFromBanksTaxed, 5800),
        aSavingsIncome("", InterestFromBanksUntaxed, 500)))))) shouldBe 1725

    }

    "be equal to RoundUpToPennies(RoundUp(Sum(Taxed Interest)) * 100/80 - Sum(Taxed Interest))" in {

      Savings.TotalTaxPaid(SelfAssessment(unearnedIncomes = Seq(anIncome().copy(savings = Seq(
        aSavingsIncome("", InterestFromBanksTaxed, 786.78),
        aSavingsIncome("", InterestFromBanksTaxed, 456.76),
        aSavingsIncome("", InterestFromBanksTaxed, 2000.56),
        aSavingsIncome("", InterestFromBanksUntaxed, 1000.56)))))) shouldBe 810.90

      Savings.TotalTaxPaid(SelfAssessment(unearnedIncomes = Seq(anIncome().copy(savings = Seq(
        aSavingsIncome("", InterestFromBanksTaxed, 1000.78),
        aSavingsIncome("", InterestFromBanksTaxed, 999.22),
        aSavingsIncome("", InterestFromBanksTaxed, 3623.67),
        aSavingsIncome("", InterestFromBanksUntaxed, 2000.56)))))) shouldBe 1405.33
    }
  }

  "Savings.IncomeTaxBandSummary" should {
    "be allocated to correct tax bands" in {
      val inputs = Table(
        ("TotalProfitFromSelfEmployments", "TotalSavingsIncome", "StartingRateAmount", "NilRateAmount", "BasicRateTaxAmount",
          "HigherRateTaxAmount", "AdditionalHigherRateAmount"),
        ("8000", "12000", "5000", "1000", "3000", "0", "0"),
        ("5000", "6000", "0", "0", "0", "0", "0"),
        ("5000", "7000", "1000", "0", "0", "0", "0"),
        ("5000", "11000", "5000", "0", "0", "0", "0"),
        ("5000", "12000", "5000", "1000", "0", "0", "0"),
        ("20000", "11000", "0", "1000", "10000", "0", "0"),
        ("29000", "12000", "0", "1000", "11000", "0", "0"),
        ("32000", "12000", "0", "500", "10500", "1000", "0"),
        ("100000", "12000", "0", "500", "0", "11500", "0"),
        ("140000", "12000", "0", "0", "0", "10000", "2000"),
        ("150000", "12000", "0", "0", "0", "0", "12000"),
        ("60000", "85000", "0", "500", "0", "84500", "0"),
        ("80000", "85000", "0", "0", "0", "70000", "15000"),
        ("13000", "7000", "3000", "1000", "3000", "0", "0"),
        ("14000", "8000", "2000", "1000", "5000", "0", "0")
      )

      TableDrivenPropertyChecks.forAll(inputs) { (totalProfitFromSelfEmployments: String, totalSavingsIncome: String,
                                                  startingRateAmount: String, nilRateAmount: String, basicRateTaxAmount: String,
                                                  higherRateTaxAmount: String, additionalHigherRateAmount: String) =>

        val totalIncomeReceived = Totals.IncomeReceived(totalNonSavings = BigDecimal(totalProfitFromSelfEmployments.toInt),
          totalSavings = BigDecimal(totalSavingsIncome.toInt), totalDividends = 0)
        val personalAllowance = Print(Deductions.PersonalAllowance(totalIncomeReceived, 0, 0)).as("PersonalAllowance")
        val totalDeduction = Deductions.Total(incomeTaxRelief = 0, personalAllowance = personalAllowance, retirementAnnuityContract = 0)
        val totalTaxableProfits = Print(SelfEmployment.TotalTaxableProfit(BigDecimal(totalProfitFromSelfEmployments.toInt),
          totalDeduction)).as("TotalTaxableProfits")
        val savingStartingRate = Print(Savings.StartingRate(profitFromSelfEmployments = totalProfitFromSelfEmployments.toInt,
          totalDeduction = totalDeduction)).as("StartingSavingRate")
        val totalTaxableIncome = Totals.TaxableIncome(totalIncomeReceived = totalIncomeReceived, totalDeduction = totalDeduction)
        val personalSavingsAllowance = Print(Savings.PersonalAllowance(totalTaxableIncome = totalTaxableIncome)).as("PersonalSavingsAllowance")
        val taxableSavingsIncome = Print(Savings.TotalTaxableIncome(totalSavingsIncome = totalSavingsIncome.toInt, totalDeduction =
          totalDeduction, totalProfitFromSelfEmployments = totalProfitFromSelfEmployments.toInt)).as("Savings.TaxableIncome")

        val bandAllocations = Savings.IncomeTaxBandSummary(taxableSavingsIncome = taxableSavingsIncome, startingSavingsRate = savingStartingRate,
          personalSavingsAllowance = personalSavingsAllowance, totalTaxableProfits = totalTaxableProfits)

        println(bandAllocations)
        println("====================================================================================")

        bandAllocations.map(_.taxableAmount) should contain theSameElementsInOrderAs Seq(startingRateAmount.toInt, nilRateAmount.toInt, basicRateTaxAmount.toInt,
          higherRateTaxAmount.toInt, additionalHigherRateAmount.toInt)
      }
    }
  }

  "SavingsIncomeTax" should {
    "be equal to" in {
      val inputs = Table(
        ("NonSavingsIncome", "SavingsIncome", "SavingsIncomeTax"),
        ("0", "12000", "0"),
        ("0", "17001", "0"),
        ("0", "17005", "1"),
        ("0", "20000", "600"),
        ("0", "43000", "5300"),
        ("0", "43001", "5300"),
        ("0", "43005", "5302"),
        ("0", "100000", "28100"),
        ("0", "150000", "52500"),
        ("0", "150001", "52600"),
        ("0", "150005", "52602"),
        ("0", "160000", "57100"),
        ("11000", "32000", "5300"),
        ("11000", "32001", "5300"),
        ("11000", "32005", "5302"),
        ("11000", "89000", "28100"),
        ("11000", "150000", "56350"),
        ("11000", "150001", "56350"),
        ("11000", "150005", "56352"),
        ("11000", "160000", "60850")
      )

      TableDrivenPropertyChecks.forAll(inputs) { (nonSavingsIncome: String, savingsIncome: String, savingsIncomeTax: String) =>
        val nonSavings = BigDecimal(nonSavingsIncome.toInt)
        val savings = BigDecimal(savingsIncome.toInt)

        val allowance = Print(Deductions.PersonalAllowance(nonSavings + savings, 0, 0)).as("personalAllowance")
        val deduction = Deductions.Total(0, allowance, 0)

        val startingSavingsRate = Print(Savings.StartingRate(nonSavings, deduction)).as("startingSavingsRate")
        val taxableSavingsIncome = Print(Savings.TotalTaxableIncome(savings, deduction, nonSavings)).as("taxableSavingsIncome")
        val personalSavingsAllowance = Print(Savings.PersonalAllowance(nonSavings + savings)).as("personalSavingsAllowance")
        val profitFromSelfEmployments = Print(SelfEmployment.TotalTaxableProfit(nonSavings, deduction)).as("nonSavingsIncome")
        val savingsIncomeBandAllocation = Savings.IncomeTaxBandSummary(taxableSavingsIncome, startingSavingsRate, personalSavingsAllowance,profitFromSelfEmployments)

        println(savingsIncomeBandAllocation)
        println("==============================")

        Savings.IncomeTax(savingsIncomeBandAllocation) shouldBe BigDecimal(savingsIncomeTax.toInt)
      }
    }
  }


}
