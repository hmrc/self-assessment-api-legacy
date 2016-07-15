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

import org.scalatest.prop.TableDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.domain.{Deductions, DividendsFromUKSources, InterestFromUKBanksAndBuildingSocieties}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.DividendsNilTaxBand
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{AllowancesAndReliefs, SelfEmploymentIncome, TaxBandAllocation}
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class PersonalDividendAllowanceCalculationSpec extends UnitSpec with SelfEmploymentSugar with TableDrivenPropertyChecks {

  "run" should {

    "calculate personal dividend allowance when there is no input data" in {

      val liability = aLiability()

      PersonalDividendAllowanceCalculation.run(SelfAssessment(), liability).dividendsIncome shouldBe Seq(TaxBandAllocation(0, DividendsNilTaxBand))
    }

    "calculate personal dividend allowance capped for various data driven inputs" in {
      val incomeTaxRelief = BigDecimal(2000)
      val personalAllowance = BigDecimal(11000)
      val inputs = Table(
        ("ProfitFromSelfEmployment", "InterestReceived", "DividendIncome", "PersonalDividendAllowance"),
        ("8000", "0", "2000", "0"),
        ("8000", "0", "6000", "1000"),
        ("11000", "2000", "5000", "5000"),
        ("11000", "5000", "5000", "5000"),
        ("13000", "2000", "7000", "5000"),
        ("15000", "3000", "9000", "5000"),
        ("13000", "0", "2000", "2000"),
        ("11000", "0", "2000", "0"),
        ("13000", "0", "5000", "5000"),
        ("14000", "0", "6000", "5000"),
        ("16000", "3000", "8000", "5000")
      )

      forAll(inputs) {  (profitFromSelfEmployment: String, interestReceived: String, dividendIncome: String, personalDividendAllowance: String) =>
        val liability = aLiability().copy(profitFromSelfEmployments = Seq(SelfEmploymentIncome("income", 0, profit = BigDecimal(profitFromSelfEmployment), 0)),
          allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(personalAllowance)), deductions = Some(Deductions(incomeTaxRelief = incomeTaxRelief, incomeTaxRelief)),
          dividendsFromUKSources = Seq(DividendsFromUKSources("dividend", BigDecimal(dividendIncome))),
          interestFromUKBanksAndBuildingSocieties = Seq(InterestFromUKBanksAndBuildingSocieties("interest", BigDecimal(interestReceived))))

        PersonalDividendAllowanceCalculation.run(SelfAssessment(), liability).dividendsIncome shouldBe Seq(TaxBandAllocation(BigDecimal(personalDividendAllowance), DividendsNilTaxBand))
      }
    }


    "calculate personal dividend allowance when Profit from Self Employments is less than the (Personal Allowance + Income Tax Relief) and there isn't any Savings Income" in {

      val income = SelfEmploymentIncome("income", 0, profit = 8000, 0)
      val deductions = Deductions(incomeTaxRelief = 4000, 4000)
      val dividends = DividendsFromUKSources("dividend", 2000)
      val liability = aLiability().copy(profitFromSelfEmployments = Seq(income), allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(5000)), deductions = Some(deductions), dividendsFromUKSources = Seq(dividends))

      PersonalDividendAllowanceCalculation.run(SelfAssessment(), liability).dividendsIncome shouldBe Seq(TaxBandAllocation(1000, DividendsNilTaxBand))
    }



    "calculate personal dividend allowance when  Profit from Self Employments is less than the (Personal Allowance + Income Tax Relief) and there is a Savings Income (Interest), " +
      "but Sum of Profit from Self Employments and Savings Income is less than the (Personal Allowance + Income Tax Relief)" in {

      val income = SelfEmploymentIncome("income", 0, profit = 8000, 0)
      val deductions = Deductions(incomeTaxRelief = 4000, 4000)
      val dividends = DividendsFromUKSources("dividend", 2000)
      val interestSavings = InterestFromUKBanksAndBuildingSocieties("interest", 500)
      val liability = aLiability().copy(profitFromSelfEmployments = Seq(income), allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(5000)), deductions = Some(deductions),
        dividendsFromUKSources = Seq(dividends), interestFromUKBanksAndBuildingSocieties = Seq(interestSavings))

      PersonalDividendAllowanceCalculation.run(SelfAssessment(), liability).dividendsIncome shouldBe Seq(TaxBandAllocation(1500, DividendsNilTaxBand))
    }


    "calculate personal dividend allowance when the Profit from Self Employments is less than the (Personal Allowance + Income Tax Relief) and there is a Savings Income (Interest), " +
      "but Sum of Profit from Self Employments and Savings Income is greater or equal to the (Personal Allowance + Income Tax Relief)" in {

      val income = SelfEmploymentIncome("income", 0, profit = 8000, 0)
      val deductions = Deductions(incomeTaxRelief = 4000, 4000)
      val dividends = DividendsFromUKSources("dividend", 2000)
      val interestSavings = InterestFromUKBanksAndBuildingSocieties("interest", 1000)
      val liability = aLiability().copy(profitFromSelfEmployments = Seq(income), allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(5000)), deductions = Some(deductions),
        dividendsFromUKSources = Seq(dividends), interestFromUKBanksAndBuildingSocieties = Seq(interestSavings))

      PersonalDividendAllowanceCalculation.run(SelfAssessment(), liability).dividendsIncome shouldBe Seq(TaxBandAllocation(2000, DividendsNilTaxBand))
    }


    "calculate personal dividend allowance when the  Profit from Self Employments is greater or equal to the (Personal Allowance + Income Tax Relief) " in {

      val income = SelfEmploymentIncome("income", 0, profit = 12000, 0)
      val deductions = Deductions(incomeTaxRelief = 4000, 4000)
      val dividends = DividendsFromUKSources("dividend", 2000)
      val liability = aLiability().copy(profitFromSelfEmployments = Seq(income), allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(5000)), deductions = Some(deductions),
        dividendsFromUKSources = Seq(dividends))

      PersonalDividendAllowanceCalculation.run(SelfAssessment(), liability).dividendsIncome shouldBe Seq(TaxBandAllocation(2000, DividendsNilTaxBand))
    }

    "calculate personal dividend allowance capped at 5000 " in {

      val income = SelfEmploymentIncome("income", 0, profit = 12000, 0)
      val deductions = Deductions(incomeTaxRelief = 4000, 4000)
      val dividends = DividendsFromUKSources("dividend", 6000)
      val liability = aLiability().copy(profitFromSelfEmployments = Seq(income), allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(5000)), deductions = Some(deductions),
        dividendsFromUKSources = Seq(dividends))

      PersonalDividendAllowanceCalculation.run(SelfAssessment(), liability).dividendsIncome shouldBe Seq(TaxBandAllocation(5000, DividendsNilTaxBand))
    }

  }
}
