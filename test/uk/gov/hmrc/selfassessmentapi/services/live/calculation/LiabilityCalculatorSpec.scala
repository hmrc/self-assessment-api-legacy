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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.IncomeType
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class LiabilityCalculatorSpec extends UnitSpec with SelfEmploymentSugar {

  "calculate" should {

    "calculate tax on income (profit = 5000, interest = 6000)" in {
    }

    "calculate tax on income (profit = 8000, interest = 12000)" in {
    }

    "calculate tax on income (profit = 20000, interest = 11000)" in {
    }

    "calculate tax on income (profit = 29000, interest = 12000)" in {
    }

    "calculate tax on income (profit = 29000, interest = 125000)" in {
    }

    "calculate tax on income (profit = 32000, interest = 12000)" in {
    }

    "calculate tax on income (profit = 100000, interest = 12000)" in {
    }

    "calculate tax on income (profit = 140000, interest = 12000)" in {
    }

    "calculate tax on income (profit = 60000, interest = 85000)" in {
    }

    "calculate tax on income (profit = 80000, interest = 85000)" in {
    }
  }

  private def savingsInterestFor(profit: BigDecimal, interest: BigDecimal) = {

    val selfAssessment = SelfAssessment(
      selfEmployments = Seq(
        aSelfEmployment().copy(incomes = Seq(income(IncomeType.Turnover, profit)))
      ),
      unearnedIncomes = Seq(
        anUnearnedIncomes().copy(savings = Seq(anUnearnedInterestIncomeSummary(amount = interest)))
      )
    )

    LiabilityCalculator().calculate(selfAssessment, MongoLiability.create(generateSaUtr(), taxYear)).savingsIncome
  }
}
