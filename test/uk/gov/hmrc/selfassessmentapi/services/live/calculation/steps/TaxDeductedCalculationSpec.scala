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
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoTaxDeducted, MongoUnearnedIncomesSavingsIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class TaxDeductedCalculationSpec extends UnitSpec with TableDrivenPropertyChecks with SelfEmploymentSugar {

  "run" should {

    "calculate tax deducted amount when there is no savings interest from banks and no property deductions" in {
      val liability = aLiability()

      TaxDeductedCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(
        taxDeducted = Some(MongoTaxDeducted(interestFromUk = 0, deductionFromUkProperties = 0)))
    }

    "calculate tax deducted amount for UK savings income across several unearned incomes considering only taxed interest" in {

      val inputs = Table(("interest 1", "interest 2", "interest 3", "tax deducted"),
        (anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksTaxed, 0),
          anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksTaxed, 0),
          anUnearnedInterestIncomeSummary("untaxedInterest1", InterestFromBanksUntaxed, 0),
          BigDecimal(0)),
        (anUnearnedInterestIncomeSummary("taxedInterest3", InterestFromBanksTaxed, 100),
          anUnearnedInterestIncomeSummary("taxedInterest4", InterestFromBanksTaxed, 200),
          anUnearnedInterestIncomeSummary("untaxedInterest2", InterestFromBanksUntaxed, 500),
          BigDecimal(75)))

      checkTable(inputs)
    }

    "calculate tax deducted amount for UK savings income with amounts that do not require rounding" in {
      val inputs = Table(("interest 1", "interest 2", "interest 3", "tax deducted"),
        (anUnearnedInterestIncomeSummary("taxedInterest5", InterestFromBanksTaxed, 100),
          anUnearnedInterestIncomeSummary("taxedInterest6", InterestFromBanksTaxed, 200),
          anUnearnedInterestIncomeSummary("taxedInterest7", InterestFromBanksTaxed, 2000),
          BigDecimal(575)),
        (anUnearnedInterestIncomeSummary("taxedInterest8", InterestFromBanksTaxed, 400),
          anUnearnedInterestIncomeSummary("taxedInterest9", InterestFromBanksTaxed, 700),
          anUnearnedInterestIncomeSummary("taxedInterest10", InterestFromBanksTaxed, 5800),
          BigDecimal(1725)))
      checkTable(inputs)
    }

    "calculate tax deducted amount for UK savings income with amounts that require rounding" in {
      val inputs = Table(("interest 1", "interest 2", "interest 3", "tax deducted"),
        (anUnearnedInterestIncomeSummary("taxedInterest11", InterestFromBanksTaxed, 786.78),
          anUnearnedInterestIncomeSummary("taxedInterest12", InterestFromBanksTaxed, 456.76),
          anUnearnedInterestIncomeSummary("taxedInterest13", InterestFromBanksTaxed, 2000.56),
          BigDecimal(811)),
        (anUnearnedInterestIncomeSummary("taxedInterest14", InterestFromBanksTaxed, 1000.78),
          anUnearnedInterestIncomeSummary("taxedInterest15", InterestFromBanksTaxed, 999.22),
          anUnearnedInterestIncomeSummary("taxedInterest16", InterestFromBanksTaxed, 3623.67),
          BigDecimal(1406)))
      checkTable(inputs)
    }

    "calculate tax deducted amount for UK properties with amounts that do not require rounding" in {
      val liability = aLiability()

      TaxDeductedCalculation.run(aSelfAssessment(
        ukProperties = Seq(aUkProperty().copy(
          taxesPaid = Seq(aUkPropertyTaxPaidSummary(500))))), liability) shouldBe
        liability.copy(taxDeducted = Some(MongoTaxDeducted(interestFromUk = 0, deductionFromUkProperties = 500)))
    }

    "calculate tax deducted amount for UK properties with amounts that require rounding" in {
      val liability = aLiability()

      TaxDeductedCalculation.run(aSelfAssessment(
        ukProperties = Seq(aUkProperty().copy(
          taxesPaid = Seq(aUkPropertyTaxPaidSummary(500.22))))), liability) shouldBe
        liability.copy(taxDeducted = Some(MongoTaxDeducted(interestFromUk = 0, deductionFromUkProperties = 501)))
    }
  }

  private def checkTable(inputs: TableFor4[MongoUnearnedIncomesSavingsIncomeSummary,
                            MongoUnearnedIncomesSavingsIncomeSummary,
                            MongoUnearnedIncomesSavingsIncomeSummary,
                            BigDecimal]): Unit = {
    forAll(inputs) { (interest1, interest2, interest3, taxDeducted) =>
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(interest1, interest2, interest3))
      val liability = aLiability()

      TaxDeductedCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability) shouldBe liability.copy(
        taxDeducted = Some(MongoTaxDeducted(interestFromUk = taxDeducted, deductionFromUkProperties = 0)))
    }
  }
}
