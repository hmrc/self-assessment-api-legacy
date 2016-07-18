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
import uk.gov.hmrc.selfassessmentapi.domain.IncomeTaxDeducted
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class TaxDeductedAmountForUkSavingsIncomeCalculationSpec
    extends UnitSpec
    with TableDrivenPropertyChecks
    with SelfEmploymentSugar {

  "run" should {

    "calculate tax deducted amount for UK savings income across several unearned incomes considering only taxed interest" in {

      val inputs = Table(("interest 1", "interest 2", "interest 3", "total interest", "total tax deducted"),
                         (anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksTaxed, 0),
                          anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksTaxed, 0),
                          anUnearnedInterestIncomeSummary("untaxedInterest1", InterestFromBanksUntaxed, 0),
                          BigDecimal(0),
                          BigDecimal(0)),
                         (anUnearnedInterestIncomeSummary("taxedInterest3", InterestFromBanksTaxed, 100),
                          anUnearnedInterestIncomeSummary("taxedInterest4", InterestFromBanksTaxed, 200),
                          anUnearnedInterestIncomeSummary("untaxedInterest2", InterestFromBanksUntaxed, 500),
                          BigDecimal(300),
                          BigDecimal(75)),
                         (anUnearnedInterestIncomeSummary("taxedInterest5", InterestFromBanksTaxed, 100),
                          anUnearnedInterestIncomeSummary("taxedInterest6", InterestFromBanksTaxed, 200),
                          anUnearnedInterestIncomeSummary("taxedInterest7", InterestFromBanksTaxed, 2000),
                          BigDecimal(2300),
                          BigDecimal(575)),
                         (anUnearnedInterestIncomeSummary("taxedInterest8", InterestFromBanksTaxed, 400),
                          anUnearnedInterestIncomeSummary("taxedInterest9", InterestFromBanksTaxed, 700),
                          anUnearnedInterestIncomeSummary("taxedInterest10", InterestFromBanksTaxed, 5800),
                          BigDecimal(6900),
                          BigDecimal(1725)),
                         (anUnearnedInterestIncomeSummary("taxedInterest11", InterestFromBanksTaxed, 786.78),
                          anUnearnedInterestIncomeSummary("taxedInterest12", InterestFromBanksTaxed, 456.76),
                          anUnearnedInterestIncomeSummary("taxedInterest13", InterestFromBanksTaxed, 2000.56),
                          BigDecimal(3244.10),
                          BigDecimal(811)),
                         (anUnearnedInterestIncomeSummary("taxedInterest14", InterestFromBanksTaxed, 1000.78),
                          anUnearnedInterestIncomeSummary("taxedInterest15", InterestFromBanksTaxed, 999.22),
                          anUnearnedInterestIncomeSummary("taxedInterest16", InterestFromBanksTaxed, 3623.67),
                          BigDecimal(5623.67),
                          BigDecimal(1406)))

      forAll(inputs) { (interest1, interest2, interest3, totalInterest, totalTaxDeducted) =>
        val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(interest1, interest2, interest3))
        val liability = aLiability()

        TaxDeductedAmountForUkSavingsIncomeCalculation.run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)),
                                                           liability) shouldBe liability.copy(
            incomeTaxDeducted = Some(IncomeTaxDeducted(interestFromUk = totalInterest, total = totalTaxDeducted)))
      }
    }
  }
}
