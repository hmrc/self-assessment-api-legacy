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

import uk.gov.hmrc.selfassessmentapi.domain.InterestFromUKBanksAndBuildingSocieties
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.{UnearnedIncomesSugar, UnitSpec}

class UnearnedInterestFromUKBanksAndBuildingSocietiesCalculationSpec extends UnitSpec with UnearnedIncomesSugar {

  "run" should {

    "calculate interest when there are no interest from uk banks and building societies from unearned income source" in {

      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(), liability)
        .getLiabilityOrFail shouldBe liability.copy(interestFromUKBanksAndBuildingSocieties = Seq())
    }

    "calculate rounded down interest when there are multiple interest of both taxed and unTaxed from uk banks and building societies from multiple unearned income source" in {

      val taxedInterest1 = anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksTaxed, 100.50)
      val unTaxedInterest1 = anUnearnedInterestIncomeSummary("unTaxedInterest1", InterestFromBanksUntaxed, 200.50)

      val taxedInterest2 = anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksTaxed, 300.99)
      val unTaxedInterest2 = anUnearnedInterestIncomeSummary("unTaxedInterest2", InterestFromBanksUntaxed, 400.99)

      val unearnedIncomes1 = anUnearnedIncomes().copy(savings = Seq(taxedInterest1, unTaxedInterest1))
      val unearnedIncomes2 = anUnearnedIncomes().copy(savings = Seq(taxedInterest2, unTaxedInterest2))

      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes1, unearnedIncomes2)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(
            interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes1.sourceId, BigDecimal(326)),
                  InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes2.sourceId, BigDecimal(777))))
    }

    "calculate interest when there is one taxed interest from uk banks and building societies from a single unearned income source" in {
      val taxedInterest = anUnearnedInterestIncomeSummary("taxedInterest", InterestFromBanksTaxed, 100)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(taxedInterest))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(125))))
    }

    "calculate interest when there are multiple taxed interest from uk banks and building societies from a single unearned income source" in {
      val taxedInterest1 = anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksTaxed, 100)
      val taxedInterest2 = anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksTaxed, 200)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(taxedInterest1, taxedInterest2))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(375))))
    }

    "calculate round down interest when there is one taxed interest from uk banks and building societies from a single unearned income source" in {
      val taxedInterest = anUnearnedInterestIncomeSummary("taxedInterest", InterestFromBanksTaxed, 100.50)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(taxedInterest))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(125))))
    }

    "calculate round down interest when there are multiple taxed interest from uk banks and building societies from a single unearned income source" in {
      val taxedInterest1 = anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksTaxed, 100.90)
      val taxedInterest2 = anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksTaxed, 200.99)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(taxedInterest1, taxedInterest2))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(377))))
    }

    "calculate interest when there is one unTaxed interest from uk banks and building societies from a single unearned income source" in {
      val unTaxedInterest = anUnearnedInterestIncomeSummary("unTaxedInterest", InterestFromBanksUntaxed, 100)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(unTaxedInterest))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(100))))
    }

    "calculate interest when there are multiple unTaxed interest from uk banks and building societies from a single unearned income source" in {
      val taxedInterest1 = anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksUntaxed, 100)
      val taxedInterest2 = anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksUntaxed, 200)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(taxedInterest1, taxedInterest2))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(300))))
    }

    "calculate rounded down interest when there is one unTaxed interest from uk banks and building societies from a single unearned income source" in {
      val unTaxedInterest = anUnearnedInterestIncomeSummary("unTaxedInterest", InterestFromBanksUntaxed, 100.50)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(unTaxedInterest))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(100))))
    }

    "calculate rounded down interest when there are multiple unTaxed interest from uk banks and building societies from a single unearned income source" in {
      val taxedInterest1 = anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksUntaxed, 100.50)
      val taxedInterest2 = anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksUntaxed, 200.99)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(taxedInterest1, taxedInterest2))
      val liability = aLiability()

      UnearnedInterestFromUKBanksAndBuildingSocietiesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail shouldBe
        liability.copy(interestFromUKBanksAndBuildingSocieties =
              Seq(InterestFromUKBanksAndBuildingSocieties(sourceId = unearnedIncomes.sourceId, BigDecimal(301))))
    }

  }
}
