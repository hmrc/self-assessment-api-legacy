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
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoTaxDeducted, MongoUkTaxPaidForEmployment, MongoUnearnedIncomesSavingsIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.{EmploymentSugar, UnearnedIncomesSugar, UnitSpec}

class TaxDeductedCalculationSpec
    extends UnitSpec
    with TableDrivenPropertyChecks
    with EmploymentSugar
    with UnearnedIncomesSugar {

  "calculateInterestFromUk" should {

    "calculate tax deducted amount for UK savings when there is no interest from banks" in {
      val liability = aLiability()

      TaxDeductedCalculation.calculateInterestFromUk(SelfAssessment(), liability) shouldBe liability.copy(
          taxDeducted = Some(MongoTaxDeducted(interestFromUk = 0)))
    }

    "calculate tax deducted amount for UK savings income across several unearned incomes considering only taxed interest" in {

      val inputs = Table(("interest 1", "interest 2", "interest 3", "interest from UK"),
                         (anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksTaxed, 0),
                          anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksTaxed, 0),
                          anUnearnedInterestIncomeSummary("untaxedInterest1", InterestFromBanksUntaxed, 0),
                          BigDecimal(0)),
                         (anUnearnedInterestIncomeSummary("taxedInterest3", InterestFromBanksTaxed, 100),
                          anUnearnedInterestIncomeSummary("taxedInterest4", InterestFromBanksTaxed, 200),
                          anUnearnedInterestIncomeSummary("untaxedInterest2", InterestFromBanksUntaxed, 500),
                          BigDecimal(75)))

      checkTableForInterestFromUk(inputs)
    }

    "calculate tax deducted amount for UK savings income with amounts that do not require rounding" in {
      val inputs = Table(("interest 1", "interest 2", "interest 3", "interest from UK"),
                         (anUnearnedInterestIncomeSummary("taxedInterest5", InterestFromBanksTaxed, 100),
                          anUnearnedInterestIncomeSummary("taxedInterest6", InterestFromBanksTaxed, 200),
                          anUnearnedInterestIncomeSummary("taxedInterest7", InterestFromBanksTaxed, 2000),
                          BigDecimal(575)),
                         (anUnearnedInterestIncomeSummary("taxedInterest8", InterestFromBanksTaxed, 400),
                          anUnearnedInterestIncomeSummary("taxedInterest9", InterestFromBanksTaxed, 700),
                          anUnearnedInterestIncomeSummary("taxedInterest10", InterestFromBanksTaxed, 5800),
                          BigDecimal(1725)))
      checkTableForInterestFromUk(inputs)
    }

    "calculate tax deducted amount for UK savings income with amounts that require rounding" in {
      val inputs = Table(("interest 1", "interest 2", "interest 3", "interest from UK"),
                         (anUnearnedInterestIncomeSummary("taxedInterest11", InterestFromBanksTaxed, 786.78),
                          anUnearnedInterestIncomeSummary("taxedInterest12", InterestFromBanksTaxed, 456.76),
                          anUnearnedInterestIncomeSummary("taxedInterest13", InterestFromBanksTaxed, 2000.56),
                          BigDecimal(811)),
                         (anUnearnedInterestIncomeSummary("taxedInterest14", InterestFromBanksTaxed, 1000.78),
                          anUnearnedInterestIncomeSummary("taxedInterest15", InterestFromBanksTaxed, 999.22),
                          anUnearnedInterestIncomeSummary("taxedInterest16", InterestFromBanksTaxed, 3623.67),
                          BigDecimal(1406)))
      checkTableForInterestFromUk(inputs)
    }

    def checkTableForInterestFromUk(
        inputs: TableFor4[MongoUnearnedIncomesSavingsIncomeSummary,
                          MongoUnearnedIncomesSavingsIncomeSummary,
                          MongoUnearnedIncomesSavingsIncomeSummary,
                          BigDecimal]): Unit = {
      forAll(inputs) { (interest1, interest2, interest3, interestFromUk) =>
        val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(interest1, interest2, interest3))
        val liability = aLiability()

        TaxDeductedCalculation
          .calculateInterestFromUk(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
          .getLiabilityOrFail shouldBe liability.copy(taxDeducted =
              Some(MongoTaxDeducted(interestFromUk = interestFromUk, ukTaxPAid = 0, ukTaxesPaidForEmployments = Nil)))
      }
    }
  }

  "calculateUkTaxPaidForEmployments" should {

    "return the UK tax paid as zero and an empty list of UK taxes paid for employments if there are no employments" in {
      val liability = aLiability()

      TaxDeductedCalculation
        .calculateUkTaxPaidForEmployments(SelfAssessment(employments = Nil), liability)
        .getLiabilityOrFail shouldBe liability.copy(
          taxDeducted = Some(MongoTaxDeducted(interestFromUk = 0, ukTaxPAid = 0, ukTaxesPaidForEmployments = Nil)))
    }

    "return a calculation error if none of the sum of the UK tax paid for a given employment is positive" in {
      val employment1UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", -34.87)
      val employment2UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -299.45)
      val employment2ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", -300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))
      val liability = aLiability()

      TaxDeductedCalculation
        .calculateUkTaxPaidForEmployments(SelfAssessment(employments = Seq(employment1, employment2)), liability)
        .fold(calculationError => calculationError.errors.map(_.code),
              liability =>
                fail(s"Expected a calculation error instead of the valid liability $liability")) shouldBe Seq.fill(2)(
          ErrorCode.INVALID_EMPLOYMENT_TAX_PAID)
    }

    "cap the UK tax paid at zero if the total tax paid is not positive" in {
      val employment1UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", -934.87)
      val employment2UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", 199.45)
      val employment2ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", 300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))
      val liability = aLiability()

      TaxDeductedCalculation
        .calculateUkTaxPaidForEmployments(SelfAssessment(employments = Seq(employment1, employment2)), liability)
        .getLiabilityOrFail shouldBe liability.copy(
          taxDeducted = Some(
              MongoTaxDeducted(interestFromUk = 0,
                               ukTaxPAid = 0,
                               ukTaxesPaidForEmployments =
                                 Seq(MongoUkTaxPaidForEmployment(employment1.sourceId, -1047.32),
                                     MongoUkTaxPaidForEmployment(employment2.sourceId, 500.32)))))
    }

    "calculate the tax deducted as the rounded up sum of UK tax paid across all employments" in {
      val employment1UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", -34.87)
      val employment2UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", 299.45)
      val employment2ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", 300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))
      val liability = aLiability()

      TaxDeductedCalculation
        .calculateUkTaxPaidForEmployments(SelfAssessment(employments = Seq(employment1, employment2)), liability)
        .getLiabilityOrFail shouldBe liability.copy(
          taxDeducted = Some(
              MongoTaxDeducted(interestFromUk = 0,
                               ukTaxPAid = 453,
                               ukTaxesPaidForEmployments =
                                 Seq(MongoUkTaxPaidForEmployment(employment1.sourceId, -147.32),
                                     MongoUkTaxPaidForEmployment(employment2.sourceId, 600.32)))))
    }
  }

  "run" should {
    "calculate the interest from UK and the UK tax paid across employments" in {
      val unearnedIncomes = anUnearnedIncomes().copy(
          savings = Seq(anUnearnedInterestIncomeSummary("taxedInterest3", InterestFromBanksTaxed, 100),
                        anUnearnedInterestIncomeSummary("taxedInterest4", InterestFromBanksTaxed, 200),
                        anUnearnedInterestIncomeSummary("untaxedInterest2", InterestFromBanksUntaxed, 500)))

      val employment1UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", -34.87)
      val employment2UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", 299.45)
      val employment2ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", 300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))

      val selfAssessment =
        SelfAssessment(unearnedIncomes = Seq(unearnedIncomes), employments = Seq(employment1, employment2))

      val liability = aLiability()

      TaxDeductedCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          taxDeducted = Some(
              MongoTaxDeducted(interestFromUk = 75,
                               ukTaxPAid = 453,
                               ukTaxesPaidForEmployments =
                                 Seq(MongoUkTaxPaidForEmployment(employment1.sourceId, -147.32),
                                     MongoUkTaxPaidForEmployment(employment2.sourceId, 600.32)))))
    }
  }
}
