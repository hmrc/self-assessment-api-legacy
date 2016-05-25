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

package uk.gov.hmrc.selfassessmentapi.domain

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._

class UKPropertySpec extends JsonSpec {

  "UKProperty" should {

    "make a valid json round trip" in {
      roundTripJson(UKProperty.example)
    }

    "reject name with more than 100 characters" in {
      val value = UKProperty(None, "Abcd" * 100, None, None, None)
      assertValidationError[UKProperty](
        value,
        Map(("/name", MAX_FIELD_LENGTH_EXCEEDED) -> "field length exceeded the max 100 chars"),
        "Expected invalid uk-property")
    }


    "reject annualInvestmentAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(UKPropertyAllowances(annualInvestmentAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map(("/allowances/annualInvestmentAllowance", INVALID_MONETARY_AMOUNT) -> "annualInvestmentAllowance should be non-negative number up to 2 decimal values"),
          "Expected invalid uk-property")
      }
    }

    "reject businessPremisesRenovationAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(UKPropertyAllowances(businessPremisesRenovationAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map(("/allowances/businessPremisesRenovationAllowance", INVALID_MONETARY_AMOUNT) -> "businessPremisesRenovationAllowance should be non-negative number up to 2 decimal values"),
          "Expected invalid uk-property")
      }
    }

    "reject otherCapitalAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(UKPropertyAllowances(otherCapitalAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map(("/allowances/otherCapitalAllowance", INVALID_MONETARY_AMOUNT) -> "otherCapitalAllowance should be non-negative number up to 2 decimal values"),
          "Expected invalid uk-property")
      }
    }


    "reject wearAndTearAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(UKPropertyAllowances(wearAndTearAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map(("/allowances/wearAndTearAllowance", INVALID_MONETARY_AMOUNT) -> "wearAndTearAllowance should be non-negative number up to 2 decimal values"),
          "Expected invalid uk-property")
      }
    }

    "reject lossBroughtForward with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(adjustments = Some(UKPropertyAdjustments(Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map(("/adjustments/lossBroughtForward", INVALID_MONETARY_AMOUNT) -> "lossBroughtForward should be non-negative number up to 2 decimal values"),
          "Expected invalid uk-property")
      }
    }

    "reject rentARoomRelief with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(rentARoomRelief = Some(amount))
        assertValidationError[UKProperty](
          value,
          Map(("/rentARoomRelief", INVALID_MONETARY_AMOUNT) -> "rentARoomRelief should be non-negative number up to 2 decimal values"),
          "Expected invalid uk-property")
      }
    }

  }

}
