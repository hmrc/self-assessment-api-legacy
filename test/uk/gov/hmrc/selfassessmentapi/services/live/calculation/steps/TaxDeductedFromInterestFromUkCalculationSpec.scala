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

import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor4}
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.UnearnedIncomesSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoTaxDeducted, MongoUnearnedIncomesSavingsIncomeSummary}

class TaxDeductedFromInterestFromUkCalculationSpec extends UnitSpec with TableDrivenPropertyChecks {

  "run" should {

    "calculate tax deducted amount for UK savings when there is no interest from banks" in {
      val liability = aLiability()

      TaxDeductedFromInterestFromUkCalculation.run(SelfAssessment(), liability).getLiabilityOrFail shouldBe liability
        .copy(taxDeducted = Some(MongoTaxDeducted(interestFromUk = 0)))
    }

    "calculate tax deducted amount for UK savings income across several unearned incomes considering only taxed interest" in {

      val inputs = Table(("interest 1", "interest 2", "interest 3", "interest from UK"),
                         (aSavingsIncome("taxedInterest1", InterestFromBanksTaxed, 0),
                          aSavingsIncome("taxedInterest2", InterestFromBanksTaxed, 0),
                          aSavingsIncome("untaxedInterest1", InterestFromBanksUntaxed, 0),
                          BigDecimal(0)),
                         (aSavingsIncome("taxedInterest3", InterestFromBanksTaxed, 100),
                          aSavingsIncome("taxedInterest4", InterestFromBanksTaxed, 200),
                          aSavingsIncome("untaxedInterest2", InterestFromBanksUntaxed, 500),
                          BigDecimal(75)))

      checkTableForInterestFromUk(inputs)
    }

    "calculate tax deducted amount for UK savings income with amounts that do not require rounding" in {
      val inputs = Table(("interest 1", "interest 2", "interest 3", "interest from UK"),
                         (aSavingsIncome("taxedInterest5", InterestFromBanksTaxed, 100),
                          aSavingsIncome("taxedInterest6", InterestFromBanksTaxed, 200),
                          aSavingsIncome("taxedInterest7", InterestFromBanksTaxed, 2000),
                          BigDecimal(575)),
                         (aSavingsIncome("taxedInterest8", InterestFromBanksTaxed, 400),
                          aSavingsIncome("taxedInterest9", InterestFromBanksTaxed, 700),
                          aSavingsIncome("taxedInterest10", InterestFromBanksTaxed, 5800),
                          BigDecimal(1725)))
      checkTableForInterestFromUk(inputs)
    }

    "calculate tax deducted amount for UK savings income with amounts that require rounding" in {
      val inputs = Table(("interest 1", "interest 2", "interest 3", "interest from UK"),
                         (aSavingsIncome("taxedInterest11", InterestFromBanksTaxed, 786.78),
                          aSavingsIncome("taxedInterest12", InterestFromBanksTaxed, 456.76),
                          aSavingsIncome("taxedInterest13", InterestFromBanksTaxed, 2000.56),
                          BigDecimal(811)),
                         (aSavingsIncome("taxedInterest14", InterestFromBanksTaxed, 1000.78),
                          aSavingsIncome("taxedInterest15", InterestFromBanksTaxed, 999.22),
                          aSavingsIncome("taxedInterest16", InterestFromBanksTaxed, 3623.67),
                          BigDecimal(1406)))
      checkTableForInterestFromUk(inputs)
    }

    def checkTableForInterestFromUk(
        inputs: TableFor4[MongoUnearnedIncomesSavingsIncomeSummary,
                          MongoUnearnedIncomesSavingsIncomeSummary,
                          MongoUnearnedIncomesSavingsIncomeSummary,
                          BigDecimal]): Unit = {
      forAll(inputs) { (interest1, interest2, interest3, interestFromUk) =>
        val unearnedIncomes = anIncome().copy(savings = Seq(interest1, interest2, interest3))
        val liability = aLiability()

        TaxDeductedFromInterestFromUkCalculation
          .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
          .getLiabilityOrFail shouldBe liability.copy(taxDeducted =
              Some(MongoTaxDeducted(interestFromUk = interestFromUk, ukTaxPaid = 0, ukTaxesPaidForEmployments = Nil)))
      }
    }
  }
}
