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

import uk.gov.hmrc.selfassessmentapi.domain.{DividendsFromUKSources, InterestFromUKBanksAndBuildingSocieties}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{EmploymentIncome, SelfEmploymentIncome, UkPropertyIncome}
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class TotalIncomeCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "calculate total income" in {

      val liability = aLiability(incomeFromEmployments = Seq(
        EmploymentIncome("e1", 100, 50, 25, 25),
        EmploymentIncome("e2", 200, 100.50, 50, 49.50)
      ),profitFromSelfEmployments = Seq(
        SelfEmploymentIncome("se1", 0, 300),
        SelfEmploymentIncome("se2", 0, 200.50)
      ), interestFromUKBanksAndBuildingSocieties = Seq(
        InterestFromUKBanksAndBuildingSocieties("ue1", 100),
        InterestFromUKBanksAndBuildingSocieties("ue2", 150)
      ), dividendsFromUKSources = Seq(
        DividendsFromUKSources("dividend1", 1000),
        DividendsFromUKSources("dividend2", 2000)
      ), profitFromUkProperties = Seq(
        UkPropertyIncome("property1", 2000),
        UkPropertyIncome("property2", 1800)
      ))

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(nonSavingsIncomeReceived = Some(4375), totalIncomeReceived = Some(7625))
    }

    "calculate total income if there are no sources" in {

      val liability = aLiability()

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(nonSavingsIncomeReceived = Some(0), totalIncomeReceived = Some(0))
    }

    "calculate total income if there are employments source" in {

      val liability =  aLiability(incomeFromEmployments = Seq(
        EmploymentIncome("e1", 100, 50, 25, 25),
        EmploymentIncome("e2", 200, 100.50, 50, 49.50)
      ))

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(nonSavingsIncomeReceived = Some(74.5), totalIncomeReceived = Some(74.5))
    }


    "calculate total income if there are self employments source" in {

      val liability =  aLiability(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome("se1", 0, 300),
        SelfEmploymentIncome("se2", 0, 200.50)
      ))

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(nonSavingsIncomeReceived = Some(500.5), totalIncomeReceived = Some(500.5))
    }

    "calculate total income if there are interest from UK banks and building societies" in {

      val liability =  aLiability(interestFromUKBanksAndBuildingSocieties = Seq(
        InterestFromUKBanksAndBuildingSocieties("ue1", 150),
        InterestFromUKBanksAndBuildingSocieties("ue2", 250)
      ))

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(nonSavingsIncomeReceived = Some(0), totalIncomeReceived = Some(400))
    }


    "calculate total income if there are dividends from unearned income" in {

      val liability =  aLiability(dividendsFromUKSources = Seq(
        DividendsFromUKSources("dividend1", 1000),
        DividendsFromUKSources("dividend2", 2000)
      ))

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(nonSavingsIncomeReceived = Some(0), totalIncomeReceived = Some(3000))
    }

    "calculate total income if there is a UK property source" in {

      val liability =  aLiability(profitFromUkProperties = Seq(UkPropertyIncome("property1", profit = 1000)))

      val updatedLiability = TotalIncomeCalculation.run(SelfAssessment(), liability)
      updatedLiability.totalIncomeReceived shouldBe Some(1000)
      updatedLiability.nonSavingsIncomeReceived shouldBe Some(1000)
    }
  }
}
