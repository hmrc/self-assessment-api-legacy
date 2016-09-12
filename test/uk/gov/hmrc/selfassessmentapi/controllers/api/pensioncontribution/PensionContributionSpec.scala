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

package uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class PensionContributionSpec extends JsonSpec {

  "format" should {
    "round trip valid PensionContribution json" in {
      roundTripJson(PensionContribution.example())
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationError[PensionContribution](
          PensionContribution(ukRegisteredPension = Some(testAmount)),
          Map("/ukRegisteredPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(retirementAnnuity = Some(testAmount)),
          Map("/retirementAnnuity" -> INVALID_MONETARY_AMOUNT), "Expected invalid retirement annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(employerScheme = Some(testAmount)),
          Map("/employerScheme" -> INVALID_MONETARY_AMOUNT), "Expected invalid employer annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(overseasPension = Some(testAmount)),
          Map("/overseasPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid overseas pension with more than 2 decimal places")
        assertValidationError[PensionSaving](PensionSaving(Some(testAmount), None),
          Map("/excessOfAnnualAllowance" -> INVALID_MONETARY_AMOUNT), "Expected invalid excess of annual allowance with more than 2 decimal places")
        assertValidationError[PensionSaving](PensionSaving(None, Some(testAmount)),
          Map("/taxPaidByPensionScheme" -> INVALID_MONETARY_AMOUNT), "Expected invalid tax paid by pension scheme with more than 2 decimal places")
      }
    }

    "reject negative amount" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-1000)).foreach { testAmount =>
        assertValidationError[PensionContribution](
          PensionContribution(ukRegisteredPension = Some(testAmount)),
          Map("/ukRegisteredPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(retirementAnnuity = Some(testAmount)),
          Map("/retirementAnnuity" -> INVALID_MONETARY_AMOUNT), "Expected invalid retirement annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(employerScheme = Some(testAmount)),
          Map("/employerScheme" -> INVALID_MONETARY_AMOUNT), "Expected invalid employer annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(overseasPension = Some(testAmount)),
          Map("/overseasPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid overseas pension with more than 2 decimal places")
        assertValidationError[PensionSaving](
          PensionSaving(Some(testAmount), None),
          Map("/excessOfAnnualAllowance" -> INVALID_MONETARY_AMOUNT), "Expected invalid excess of annual allowance with more than 2 decimal places")
        assertValidationError[PensionSaving](
          PensionSaving(None, Some(testAmount)),
          Map("/taxPaidByPensionScheme" -> INVALID_MONETARY_AMOUNT), "Expected invalid tax paid by pension scheme with more than 2 decimal places")
      }
    }

    "reject pensionSavings when there are no pension contributions" in {
      assertValidationError[PensionContribution](
        PensionContribution(pensionSavings = Some(PensionSaving(excessOfAnnualAllowance = None, taxPaidByPensionScheme = None))),
        Map("" -> UNDEFINED_REQUIRED_ELEMENT), "pensionSavings may only exist if there is at least one pension contribution")
    }

    "reject pensionSavings when the sum of pensionSavings exceeds the sum of all other pension contributions" in {
      assertValidationError[PensionContribution](
        PensionContribution(employerScheme = Some(100), pensionSavings = Some(PensionSaving(excessOfAnnualAllowance = Some(500), taxPaidByPensionScheme = Some(500)))),
        Map("" -> MAXIMUM_AMOUNT_EXCEEDED), "excessOfAnnualAllowance may not exceed the sum of all pension contributions")
    }
  }

  "PensionSavings" should {
    "reject taxPaidByPensionScheme when excessOfAnnualAllowance is undefined" in {
      assertValidationError[PensionSaving](
        PensionSaving(excessOfAnnualAllowance = None, taxPaidByPensionScheme = Some(500)),
        Map("" -> UNDEFINED_REQUIRED_ELEMENT), "taxPaidByPensionScheme can not exist when there is no excessOfAnnualAllowance")
    }

    "reject taxPaidByPensionScheme when its value exceeds the value of the excessOfAnnualAllowance" in {
      assertValidationError[PensionSaving](
        PensionSaving(excessOfAnnualAllowance = Some(200), taxPaidByPensionScheme = Some(500)),
        Map("" -> MAXIMUM_AMOUNT_EXCEEDED), "the value of taxPaidByPensionScheme may not exceed the excessOfAnnualAllowance")
    }
  }
}
