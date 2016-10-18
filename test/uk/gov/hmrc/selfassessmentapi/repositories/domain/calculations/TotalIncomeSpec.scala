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

import uk.gov.hmrc.selfassessmentapi.UnitSpec

class TotalIncomeSpec extends UnitSpec {

  "total income" should {
    "calculate total income" in {
      Totals.IncomeReceived(totalNonSavings = 200, totalSavings = 250, totalDividends = 3000) shouldBe 3450
    }

    "calculate total income if there is no income from self employments" in {
      Totals.IncomeReceived(totalNonSavings = 0, totalSavings = 0, totalDividends = 0) shouldBe 0
    }

    "calculate total income if there is no income from self employments but has interest from UK banks and building societies" in {
      Totals.IncomeReceived(totalNonSavings = 0, totalSavings = 250, totalDividends = 0) shouldBe 250
    }

    "calculate total income if there is no income from self employments but has dividends" in {
      Totals.IncomeReceived(totalNonSavings = 0, totalSavings = 0, totalDividends = 3000) shouldBe 3000
    }
  }

}
