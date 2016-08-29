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

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.Tables.Table
import uk.gov.hmrc.selfassessmentapi.UnitSpec

class NonSavingsSpec extends UnitSpec {

  "NonSavings.TotalIncome" should {
    "be selfEmploymentProfits + employmentProfits + ukPropertyProfits + furnishedHolidayLettingProfits" in {
      NonSavings.TotalIncome(selfEmploymentProfits = 1000, employmentProfits = 2000, ukPropertyProfits = 500,
        furnishedHolidayLettingProfits = 200) shouldBe 3700
    }
  }

  "NonSavings.TaxBandSummaries" should {

    "be calculated for total NonSavingsIncome < 32000" in {
      NonSavings.IncomeTaxBandSummary(totalTaxableProfitFromSelfEmployments = 20000)
        .map(_.taxableAmount) should contain theSameElementsAs Seq(20000, 0, 0)
      NonSavings.IncomeTaxBandSummary(totalTaxableProfitFromSelfEmployments = 31999)
        .map(_.taxableAmount) should contain theSameElementsAs Seq(31999, 0, 0)
    }

    "be calculated for total NonSavingsIncome = 32000" in {
      NonSavings.IncomeTaxBandSummary(totalTaxableProfitFromSelfEmployments = 32000)
        .map(_.taxableAmount) should contain theSameElementsInOrderAs Seq(32000, 0, 0)
    }

    "be calculated tax for 32000 < NonSavingsIncome < 150000" in {
      NonSavings.IncomeTaxBandSummary(totalTaxableProfitFromSelfEmployments = 60000)
        .map(_.taxableAmount) should contain theSameElementsInOrderAs Seq(32000, 28000, 0)
    }

    "be calculated tax for NonSavingsIncome = 150000" in {
      NonSavings.IncomeTaxBandSummary(totalTaxableProfitFromSelfEmployments = 150000)
        .map(_.taxableAmount) should contain theSameElementsInOrderAs Seq(32000, 118000, 0)
    }

    "be calculated tax for NonSavingsIncome > 150000" in {
      NonSavings.IncomeTaxBandSummary(totalTaxableProfitFromSelfEmployments = 150001)
        .map(_.taxableAmount) should contain theSameElementsInOrderAs Seq(32000, 118000, 1)
      NonSavings.IncomeTaxBandSummary(totalTaxableProfitFromSelfEmployments = 300000)
        .map(_.taxableAmount) should contain theSameElementsInOrderAs Seq(32000, 118000, 150000)
    }
  }

  "NonSavingsIncomeTax" should {
    "be equal to" in {
      val inputs = Table(
        ("TotalTaxableProfits", "NonSavingsIncomeTax"),
        ("20000", "4000"),
        ("31999", "6399"),
        ("33000", "6800"),
        ("89000", "29200"),
        ("160000", "58100")
      )
      TableDrivenPropertyChecks.forAll(inputs) { (totalTaxableProfits: String, nonSavingsIncomeTax: String) =>
        NonSavings.IncomeTax(NonSavings.IncomeTaxBandSummary(BigDecimal(totalTaxableProfits.toInt))) shouldBe BigDecimal(nonSavingsIncomeTax.toInt)
      }
    }
  }
}
