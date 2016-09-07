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

package uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.UkCountryCodes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{JsonSpec, ErrorCode, UkCountryCodes}

class BlindPersonSpec extends JsonSpec {

  "format" should {
    "round trip valid BlindPerson json" in {
      roundTripJson(BlindPerson.example())
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      val testAmount = BigDecimal(1000.123)
      assertValidationError[BlindPerson](
          BlindPerson(country = Some(England),
                      spouseSurplusAllowance = Some(testAmount),
                      wantSpouseToUseSurplusAllowance = Some(true)),
          Map("/spouseSurplusAllowance" -> INVALID_MONETARY_AMOUNT),
          "Expected invalid spouse surplus allowance with more than 2 decimal places")
    }

    "reject negative amount" in {
      val testAmount = BigDecimal(-1000.123)
      assertValidationError[BlindPerson](
          BlindPerson(country = Some(England),
                      spouseSurplusAllowance = Some(testAmount),
                      wantSpouseToUseSurplusAllowance = Some(true)),
          Map("/spouseSurplusAllowance" -> INVALID_MONETARY_AMOUNT),
          "Expected negative spouse surplus allowance amount")
    }

    "reject amount greater than £2,290.00" in {
      val testAmount = BigDecimal(3000.00)
      assertValidationError[BlindPerson](
          BlindPerson(country = Some(England),
                      spouseSurplusAllowance = Some(testAmount),
                      wantSpouseToUseSurplusAllowance = Some(true)),
          Map("/spouseSurplusAllowance" -> MAX_MONETARY_AMOUNT),
          "Expected surplus allowance amount larger than £2,290.00")
    }

    "reject blind person allowance when country is England or Wales and registration authority is not provided" in {
      assertValidationError[BlindPerson](
          BlindPerson(country = Some(England),
                      registrationAuthority = None,
                      spouseSurplusAllowance = Some(2000.00),
                      wantSpouseToUseSurplusAllowance = Some(true)),
          Map("" -> MISSING_REGISTRATION_AUTHORITY),
          "Expected no registration authority to be provided when the country is England or Wales")
    }

    "reject blind person allowance when country is England or Wales and registration authority is provided but empty" in {
      assertValidationError[BlindPerson](
          BlindPerson(country = Some(England),
                      registrationAuthority = Some(""),
                      spouseSurplusAllowance = Some(2000.00),
                      wantSpouseToUseSurplusAllowance = Some(true)),
          Map("" -> MISSING_REGISTRATION_AUTHORITY),
          "Expected an empty registration authority to be provided when the country is England or Wales")
    }

    "reject blind person allowance when the registration authority is provided but the country is not" in {
      assertValidationError[BlindPerson](
        BlindPerson(country = None,
          registrationAuthority = Some("Registrar")),
        Map("" -> MISSING_COUNTRY),
        "Expected no country to be provided when the registration authority is provided")
    }

    "reject blind person allowance when the person wants the spouse to use surplus allowance but is not registered blind in a country" in {
      assertValidationError[BlindPerson](
        BlindPerson(country = None,
          wantSpouseToUseSurplusAllowance = Some(true)),
        Map("" -> MUST_BE_BLIND_TO_WANT_SPOUSE_TO_USE_SURPLUS_ALLOWANCE),
        "Expected wantSpouseToUseSurplusAllowance to be supplied when person is not registered blind")
    }
  }
}
