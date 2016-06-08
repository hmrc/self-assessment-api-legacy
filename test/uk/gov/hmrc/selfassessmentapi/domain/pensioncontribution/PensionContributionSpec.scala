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

package uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec

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

      }
    }
  }

}
