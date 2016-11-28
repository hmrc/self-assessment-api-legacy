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

package uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class UKPropertySpec extends JsonSpec {

  "UKProperty" should {

    "make a valid json round trip" in {
      roundTripJson(UKProperty.example())
    }

    "reject annualInvestmentAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example().copy(allowances = Some(Allowances(annualInvestmentAllowance = Some(amount))))
        assertValidationErrorWithCode(
          value,
          "/allowances/annualInvestmentAllowance", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject businessPremisesRenovationAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example().copy(allowances = Some(Allowances(businessPremisesRenovationAllowance = Some(amount))))
        assertValidationErrorWithCode(
          value,
          "/allowances/businessPremisesRenovationAllowance", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject otherCapitalAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example().copy(allowances = Some(Allowances(otherCapitalAllowance = Some(amount))))
        assertValidationErrorWithCode(
          value,
          "/allowances/otherCapitalAllowance", INVALID_MONETARY_AMOUNT)
      }
    }


    "reject wearAndTearAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example().copy(allowances = Some(Allowances(wearAndTearAllowance = Some(amount))))
        assertValidationErrorWithCode(
          value,
          "/allowances/wearAndTearAllowance", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject lossBroughtForward with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        assertValidationErrorWithCode(
          UKProperty.example().copy(adjustments = Some(Adjustments(Some(amount)))),
          "/adjustments/lossBroughtForward", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject rentARoomRelief with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        assertValidationErrorWithCode(
          UKProperty.example().copy(rentARoomRelief = Some(amount)),
          "/rentARoomRelief", INVALID_MONETARY_AMOUNT)
      }
    }

  }

}
