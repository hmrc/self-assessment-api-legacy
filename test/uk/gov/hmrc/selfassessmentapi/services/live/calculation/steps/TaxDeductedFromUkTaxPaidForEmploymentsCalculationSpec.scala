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
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Error, MongoTaxDeducted, MongoUkTaxPaidForEmployment}
import uk.gov.hmrc.selfassessmentapi.{EmploymentSugar, UnitSpec}

class TaxDeductedFromUkTaxPaidForEmploymentsCalculationSpec
    extends UnitSpec
    with TableDrivenPropertyChecks
    with EmploymentSugar {

  "run" should {

    "return the UK tax paid as zero and an empty list of UK taxes paid for employments if there are no employments" in {
      val liability = aLiability()

      TaxDeductedFromUkTaxPaidForEmploymentsCalculation
        .run(SelfAssessment(employments = Nil), liability)
        .getLiabilityOrFail shouldBe liability.copy(
          taxDeducted = Some(MongoTaxDeducted(interestFromUk = 0, ukTaxPAid = 0, ukTaxesPaidForEmployments = Nil)))
    }

    "return a calculation error identifying the sources if none of the sum of the UK tax paid for a given employment is positive" in {
      val employment1UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", -34.87)
      val employment2UkTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -299.45)
      val employment2ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", -300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))
      val liability = aLiability()

      val calculationError = TaxDeductedFromUkTaxPaidForEmploymentsCalculation
        .run(SelfAssessment(employments = Seq(employment1, employment2)), liability)
        .fold(identity, liability => fail(s"Expected a calculation error instead of the valid liability $liability"))

      calculationError.errors should contain theSameElementsAs Seq(
          Error(ErrorCode.INVALID_EMPLOYMENT_TAX_PAID,
                s"The UK tax paid for employment with source id ${employment1.sourceId} should not be negative"),
          Error(ErrorCode.INVALID_EMPLOYMENT_TAX_PAID,
                s"The UK tax paid for employment with source id ${employment2.sourceId} should not be negative"))
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

      TaxDeductedFromUkTaxPaidForEmploymentsCalculation
        .run(SelfAssessment(employments = Seq(employment1, employment2)), liability)
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

      TaxDeductedFromUkTaxPaidForEmploymentsCalculation
        .run(SelfAssessment(employments = Seq(employment1, employment2)), liability)
        .getLiabilityOrFail shouldBe liability.copy(
          taxDeducted = Some(
              MongoTaxDeducted(interestFromUk = 0,
                               ukTaxPAid = 453,
                               ukTaxesPaidForEmployments =
                                 Seq(MongoUkTaxPaidForEmployment(employment1.sourceId, -147.32),
                                     MongoUkTaxPaidForEmployment(employment2.sourceId, 600.32)))))
    }
  }
}
