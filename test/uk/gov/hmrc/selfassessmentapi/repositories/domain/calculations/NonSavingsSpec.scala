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
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxBandSummary

class NonSavingsSpec extends UnitSpec {

  "NonSavings.TotalIncome" should {
    "be selfEmploymentProfits + employmentProfits + ukPropertyProfits + furnishedHolidayLettingProfits" in {
      NonSavings.TotalIncome(selfEmploymentProfits = 1000, employmentProfits = 2000, ukPropertyProfits = 500,
        furnishedHolidayLettingProfits = 200) shouldBe 3700
    }
  }

  "NonSavings.TaxBandSummaries" should {

    "be calculated for total NonSavingsIncome < 32000" in {
      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 20000) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 20000.00, "20%", 4000.00), TaxBandSummary("higherRate", 0.00, "40%", 0.00),
          TaxBandSummary("additionalHigherRate", 0.00, "45%", 0.00))

      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 31999.00) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 31999.00, "20%", 6399.80), TaxBandSummary("higherRate", 0.0, "40%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "45%", 0.0))

    }

    "be calculated for total NonSavingsIncome = 32000" in {
      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 32000) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 32000.00, "20%", 6400.00), TaxBandSummary("higherRate", 0, "40%", 0),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0))
    }

    "be calculated tax for 32000 < NonSavingsIncome < 150000" in {
      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 32001) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 32000.00, "20%", 6400.00), TaxBandSummary("higherRate", 1.0, "40%", 0.40),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0))

      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 33003) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 32000.00, "20%", 6400.00), TaxBandSummary("higherRate", 1003, "40%", 401.20),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0))
    }

    "be calculated tax for 32000 < NonSavingsIncome < 150000 and pension contributions" in {
      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 32001, ukPensionContributions = 100) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 32001.00, "20%", 6400.20), TaxBandSummary("higherRate", 0, "40%", 0),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0))
    }

    "be calculated tax for NonSavingsIncome = 150000" in {
      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 150000) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 32000.00, "20%", 6400.00), TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0))
    }

    "be calculated tax for NonSavingsIncome > 150000" in {
      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 150001) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 32000.00, "20%", 6400.00), TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 1, "45%", 0.45))

    }

    "be calculated tax for NonSavingsIncome > 150000 and pension contributions" in {
      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 150003, ukPensionContributions = 100) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 32100.00, "20%", 6420.00), TaxBandSummary("higherRate", 117903.00, "40%", 47161.20),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0.00))

      NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = 160000, ukPensionContributions = 1000) should contain theSameElementsAs
        Seq(TaxBandSummary("basicRate", 33000.00, "20%", 6600.00), TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 9000, "45%", 4050))
    }

  }

  "NonSavingsIncomeTax" should {
    "be equal to" in {
      val inputs = Table(
        ("TotalTaxableProfits", "UkPensionContributions", "NonSavingsIncomeTax"),
        ("20001", "1000", "4000.20"),
        ("31999", "1000", "6399.80"),
        ("33001", "0", "6800.40"),
        ("33001", "1000", "6600.40"),
        ("89002", "0", "29200.80"),
        ("160003", "0", "58101.35"),
        ("160003", "10000", "55601.35")
      )
      TableDrivenPropertyChecks.forAll(inputs) { (totalTaxableProfits: String, ukPensionContributions: String, nonSavingsIncomeTax: String) =>
        NonSavings.IncomeTax(NonSavings.IncomeTaxBandSummary(totalNonSavingsTaxableIncome = BigDecimal(totalTaxableProfits.toInt),
          ukPensionContributions = BigDecimal(ukPensionContributions.toInt))) shouldBe BigDecimal(nonSavingsIncomeTax.toDouble)
      }
    }
  }
}
