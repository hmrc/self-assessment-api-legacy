/*
 * Copyright 2017 HM Revenue & Customs
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

import uk.gov.hmrc.selfassessmentapi.UnitSpec

class TotalsSpec extends UnitSpec {

  "TotalIncomeReceived" should {
    "be nonSavingsIncome + savingsIncome + dividendsIncome" in {
      Totals.IncomeReceived(totalNonSavings = 1000, totalSavings = 1000, totalDividends = 2000) shouldBe 4000
    }
  }

  "TotalTaxableIncome" should {
    "be (TotalIncomeReceived - TotalDeductions)" in {
      Totals.TaxableIncome(totalIncomeReceived = 10000, totalDeduction = 5000) shouldBe 5000
    }

    "be 0 if TotalDeductions > TotalIncomeReceived" in {
      Totals.TaxableIncome(totalIncomeReceived = 10000, totalDeduction = 10001) shouldBe 0
    }
  }
}
