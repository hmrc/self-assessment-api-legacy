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

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.Tables.Table
import uk.gov.hmrc.selfassessmentapi.UnearnedIncomesSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.{DividendsFromUKSources, TaxBandSummary}
import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.DividendType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment

class DividendsSpec extends UnitSpec {

  "Dividends FromUK" should {

    "be empty when there are no dividends from uk sources" in {
      Dividends.FromUK(SelfAssessment()) shouldBe empty
    }

    "be calculated for multiple UK dividends from multiple income sources" in {

      val dividendUK1 = aDividendIncome("dividendUK1", FromUKCompanies, 1000.50)
      val dividendOther1 = aDividendIncome("dividendOtherUK1", OtherFromUKCompanies, 2000.99)

      val dividendUK2 = aDividendIncome("dividendUK2", FromUKCompanies, 3000.50)
      val dividendOther2 = aDividendIncome("dividendOtherUK2", OtherFromUKCompanies, 4000.999)

      val unearnedIncomes1 = anIncome().copy(dividends = Seq(dividendUK1, dividendOther1))
      val unearnedIncomes2 = anIncome().copy(dividends = Seq(dividendUK2, dividendOther2))

      Dividends.FromUK(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes1, unearnedIncomes2))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes1.sourceId, BigDecimal(3001)),
          DividendsFromUKSources(sourceId = unearnedIncomes2.sourceId, BigDecimal(7001)))
    }

    "be calculated for a single one uk dividend for a single income source" in {
      val unearnedIncomes = anIncome().copy(dividends = Seq(aDividendIncome("dividendUK", FromUKCompanies, 1000)))

      Dividends.FromUK(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(1000)))
    }

    "be calculated for multiple uk dividends from a single income source" in {
      val dividendUK1 = aDividendIncome("dividendUK1", FromUKCompanies, 1000)
      val dividendUK2 = aDividendIncome("dividendUK2", FromUKCompanies, 2000)
      val unearnedIncomes = anIncome().copy(dividends = Seq(dividendUK1, dividendUK2))

      Dividends.FromUK(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(3000)))
    }

    "be round down to nearest pound for a single dividend" in {
      val unearnedIncomes = anIncome().copy(dividends = Seq(aDividendIncome("dividendUK", FromUKCompanies, 1000.50)))

      Dividends.FromUK(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(1000)))
    }

    "be round down to nearest pound for multiple dividends" in {
      val dividendUK1 = aDividendIncome("dividendUK1", FromUKCompanies, 1000.90)
      val dividendUK2 = aDividendIncome("dividendUK2", FromUKCompanies, 2000.99)
      val unearnedIncomes = anIncome().copy(dividends = Seq(dividendUK1, dividendUK2))

      Dividends.FromUK(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(3001)))
    }

  }

  "TotalDividends" should {
    "be sum of all dividends across multiple sources which have been rounded at source level" in {
      val dividendUK1 = aDividendIncome("dividendUK1", FromUKCompanies, 1000.50)
      val dividendOther1 = aDividendIncome("dividendOtherUK1", OtherFromUKCompanies, 2000.99)

      val dividendUK2 = aDividendIncome("dividendUK2", FromUKCompanies, 3000.50)
      val dividendOther2 = aDividendIncome("dividendOtherUK2", OtherFromUKCompanies, 4000.999)

      val unearnedIncomes1 = anIncome().copy(dividends = Seq(dividendUK1, dividendOther1))
      val unearnedIncomes2 = anIncome().copy(dividends = Seq(dividendUK2, dividendOther2))

      Dividends.TotalIncome(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes1, unearnedIncomes2))) shouldBe 10002

    }
  }

  "TaxableDividendIncome" should {
    "equal to total dividend income if there is no remaining deductions" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 1000, totalProfits = 11000, totalSavingsIncome = 0, totalDeduction = 11000) shouldBe 1000
    }

    "equal to total dividend income - deduction remain after allocation to profits and savings income" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 9001, totalProfits = 1000, totalSavingsIncome = 1000, totalDeduction = 11000) shouldBe 1
    }

    "equal to zero if total dividend income is equal to deduction remain after allocation to profits and savings income" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 9000, totalProfits = 1000, totalSavingsIncome = 1000, totalDeduction = 11000) shouldBe 0
    }

    "equal to zero if total dividend income is less than the deduction remain after allocation to profits and savings income" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 8999, totalProfits = 1000, totalSavingsIncome = 1000, totalDeduction = 11000) shouldBe 0
    }
  }

  "Dividends.IncomeTaxBandSummary" should {
    "be calculated when TaxableNonSavingsIncome = 0, TaxableSavingIncome = 0 and TaxableDividendsIncome falls within BasicRate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 0, taxableNonSavingsIncome =0, taxableDividendIncome = 31999,
        personalDividendAllowance = 5000, ukPensionContribution = 0) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 26999.0, "7.5%", 2024.92),
          TaxBandSummary("higherRate", 0.0, "32.5%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0 and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 0, taxableNonSavingsIncome =0, taxableDividendIncome = 32001,
        personalDividendAllowance = 5000, ukPensionContribution = 0) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 27000.0, "7.5%", 2025.0),
          TaxBandSummary("higherRate", 1.0, "32.5%", 0.32),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0, ukPensionContributions is present and TaxableSavingIncome is all in Basic Rate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 0, taxableNonSavingsIncome =0, taxableDividendIncome = 32001,
        personalDividendAllowance = 5000, ukPensionContribution = 500) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 27001.0, "7.5%", 2025.07),
          TaxBandSummary("higherRate", 0, "32.5%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0 and TaxableSavingIncome is spread over Basic, Higher and Additional Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 0, taxableNonSavingsIncome =0, taxableDividendIncome = 150001,
        personalDividendAllowance = 5000, ukPensionContribution = 0) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 27000.0, "7.5%", 2025.0),
          TaxBandSummary("higherRate", 118000.0, "32.5%", 38350.0),
          TaxBandSummary("additionalHigherRate", 1.0, "38.1%", 0.38)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome falls within BasicRate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 500, taxableNonSavingsIncome = 500, taxableDividendIncome = 30999,
        personalDividendAllowance = 5000, ukPensionContribution = 0) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 25999.0, "7.5%", 1949.92),
          TaxBandSummary("higherRate", 0.0, "32.5%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 500, taxableNonSavingsIncome = 500, taxableDividendIncome = 31001,
        personalDividendAllowance = 5000, ukPensionContribution = 0) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 26000.0, "7.5%", 1950.0),
          TaxBandSummary("higherRate", 1.0, "32.5%", 0.32),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome is spread over Basic, Higher and Additional Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 500, taxableNonSavingsIncome = 500, taxableDividendIncome = 149001,
        personalDividendAllowance = 5000, ukPensionContribution = 0) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 26000.0, "7.5%", 1950.0),
          TaxBandSummary("higherRate", 118000.0, "32.5%", 38350.0),
          TaxBandSummary("additionalHigherRate", 1.0, "38.1%", 0.38)
        )
    }

   "be calculated when NonSavingsIncome > 0, ukPensionContributions is present and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(taxableSavingsIncome = 500, taxableNonSavingsIncome = 500, taxableDividendIncome = 149001,
        personalDividendAllowance = 5000, ukPensionContribution = 500) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 26500.0, "7.5%", 1987.5),
          TaxBandSummary("higherRate", 117501.0, "32.5%", 38187.82),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

  }

  "Dividends.IncomeTaxBandSummary" should {
    "acceptance test" in {
      val inputs = Table(
        ("TotalProfitFromSelfEmployments", "TotalSavingsIncome", "UkPensionContribution", "TotalDividends", "NilRateAmount", "BasicRateTaxAmount",
          "HigherRateTaxAmount", "AdditionalHigherRateAmount"),
        ("0", "0", "0", "10999", "0", "0", "0", "0"),
        ("0", "0", "0", "11001", "1", "0", "0", "0"),
        ("0", "0", "0", "15000", "4000", "0", "0", "0"),
        ("0", "0", "0", "16001", "5000", "1", "0", "0"),
        ("0", "0", "0", "20000", "5000", "4000", "0", "0"),
        ("0", "0", "0", "33000", "5000", "17000", "0", "0"),
        ("0", "0", "0", "43000", "5000", "27000", "0", "0"),
        ("0", "0", "0", "43001", "5000", "27000", "1", "0"),
        ("0", "0", "0", "120000", "5000", "27000", "87000", "0"),
        ("0", "0", "0", "149000", "5000", "27000", "117000", "0"),
        ("0", "0", "5000", "149000", "5000", "32000", "112000", "0"),
        ("0", "0", "0", "150001", "5000", "27000", "118000", "1"),
        ("0", "0", "0", "151000", "5000", "27000", "118000", "1000"),
        ("1000", "0", "0", "11000", "1000", "0", "0", "0"),
        ("1000", "0", "0", "15000", "5000", "0", "0", "0"),
        ("1000", "0", "0", "16000", "5000", "1000", "0", "0"),
        ("11000", "0", "0", "5001", "5000", "1", "0", "0"),
        ("12000", "0", "0", "5000", "5000", "0", "0", "0"),
        ("12000", "0", "0", "6000", "5000", "1000", "0", "0"),
        ("12000", "0", "0", "139000", "5000", "15000", "118000", "1000"),
        ("12000", "0", "500", "139000", "5000", "15500", "118000", "500")
      )

      TableDrivenPropertyChecks.forAll(inputs) { (totalProfits: String, totalSavingsIncome: String, ukPensionContributions: String,
                                                  totalDividends:String, nilRateAmount: String, basicRateTaxAmount: String,
                                                  higherRateTaxAmount: String, additionalHigherRateAmount: String) =>

        val incomeTaxRelief = 0
        val profits = BigDecimal(totalProfits.toInt)
        val savings = BigDecimal(totalSavingsIncome.toInt)
        val dividends = BigDecimal(totalDividends.toInt)
        val ukPensionsContributions = BigDecimal(ukPensionContributions.toInt)

        val personalAllowance = Print(Deductions.PersonalAllowance(totalIncomeReceived = profits + savings + dividends, incomeTaxRelief,
          pensionContribution = 0)).as("personalAllowance")
        val totalDeduction = Print(Deductions.Total(incomeTaxRelief, personalAllowance, 0)).as("totalDeduction")
        val taxableProfitFromSelfEmployments = Print(SelfEmployment.TotalTaxableProfit(profits, totalDeduction)).as("taxableProfits")
        val taxableSavingsIncome = Print(Savings.TotalTaxableIncome(savings, totalDeduction, profits)).as("taxableSavingsIncome")
        val taxableDividendIncome = Print(Dividends.TotalTaxableIncome(dividends, profits, savings, totalDeduction)).as("taxableDividendIncome")
        val personalDividendAllowance = Print(Dividends.PersonalAllowance(taxableDividendIncome)).as("personalDividendAllowance")

        val dividendIncomeTax = Dividends.IncomeTaxBandSummary(taxableProfitFromSelfEmployments, taxableSavingsIncome, taxableDividendIncome,
          personalDividendAllowance, ukPensionContribution = ukPensionsContributions)

        println(dividendIncomeTax.map(_.taxableAmount))
        println("==========================================")

        dividendIncomeTax.map(_.taxableAmount) should contain theSameElementsInOrderAs
          Seq(BigDecimal(nilRateAmount.toInt), BigDecimal(basicRateTaxAmount.toInt), BigDecimal(higherRateTaxAmount.toInt), BigDecimal(additionalHigherRateAmount.toInt))
      }
    }
  }

  "Dividends.PersonalAllowance" should {

    "acceptance test" in {

      val inputs = Table(
        ("ProfitFromSelfEmployment", "SavingsIncome", "DividendIncome", "PersonalDividendAllowance"),
        ("8000", "0", "2000", "0"),
        ("8000", "0", "6000", "1000"),
        ("8000", "2000", "6000", "3000"),
        ("11000", "2000", "4999", "4999"),
        ("11000", "5000", "5001", "5000"),
        ("13000", "2000", "8000", "5000"),
        ("15000", "3000", "10000", "5000"),
        ("13000", "0", "2000", "2000"),
        ("11000", "0", "2000", "0"),
        ("13000", "0", "5000", "5000"),
        ("14000", "0", "6000", "5000"),
        ("16000", "3000", "8000", "5000")
      )

      TableDrivenPropertyChecks.forAll(inputs) { (profitFromSelfEmployment: String, savingsIncome: String, dividendIncome: String,
                                                  personalDividendAllowance: String) =>

        val profits = Print(BigDecimal(profitFromSelfEmployment.toInt)).as("profits")
        val savings = Print(BigDecimal(savingsIncome.toInt)).as("savings")
        val dividends = Print(BigDecimal(dividendIncome.toInt)).as("dividends")

        val totalIncome = Print(profits + savings + dividends).as("totalIncome")
        val totalDeduction = Print(Deductions.Total(2000, Deductions.PersonalAllowance(totalIncome, 0, 0), 0)).as("totalDeduction")
        val taxableDividendIncome = Print(Dividends.TotalTaxableIncome(dividends, profits, savings, totalDeduction)).as("taxableDividendIncome")

        println("=======================================")

        Dividends.PersonalAllowance(taxableDividendIncome) shouldBe personalDividendAllowance.toInt
      }
    }

  }
}
