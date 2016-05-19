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

class SelfEmploymentAdjustmentsSpec extends JsonSpec {

  "format" should {
    "round trip valid SelfEmploymentAllowancesSpec json" in {
      roundTripJson(SelfEmploymentAdjustments(
        includedNonTaxableProfits = Some(BigDecimal(10.00)),
        basisAdjustment = Some(BigDecimal(10.00)),
        overlapReliefUsed = Some(BigDecimal(10.00)),
        accountingAdjustment = Some(BigDecimal(10.00)),
        averagingAdjustment = Some(BigDecimal(10.00)),
        lossBroughtForward = Some(BigDecimal(10.00)),
        outstandingBusinessIncome = Some(BigDecimal(10.00))))
    }
  }

  "validate" should {
    def validateNegative(model: SelfEmploymentAdjustments, fieldName: String) = {
      assertValidationError[SelfEmploymentAdjustments](
        model,
        Map((s"/$fieldName", INVALID_MONETARY_AMOUNT) -> s"$fieldName should be non-negative number up to 2 decimal values"),
        "Expected valid self-employment-adjustments")
    }

    "reject negative includedNonTaxableProfits" in {
      val se = SelfEmploymentAdjustments(includedNonTaxableProfits = Some(BigDecimal(-10.00)))
     validateNegative(se, "includedNonTaxableProfits")
    }

    "reject negative overlapReliefUsed" in {
      val se = SelfEmploymentAdjustments(overlapReliefUsed = Some(BigDecimal(-10.00)))
     validateNegative(se, "overlapReliefUsed")
    }

    "reject negative accountingAdjustment" in {
      val se = SelfEmploymentAdjustments(accountingAdjustment = Some(BigDecimal(-10.00)))
     validateNegative(se, "accountingAdjustment")
    }

    "not reject negative averagingAdjustment" in {
      val se = SelfEmploymentAdjustments(averagingAdjustment = Some(BigDecimal(-10.00)))
     assertValidationPasses[SelfEmploymentAdjustments](se)
    }

    "not reject positive averagingAdjustment" in {
      val se = SelfEmploymentAdjustments(averagingAdjustment = Some(BigDecimal(10.00)))
     assertValidationPasses[SelfEmploymentAdjustments](se)
    }

    "reject negative lossBroughtForward" in {
      val se = SelfEmploymentAdjustments(lossBroughtForward = Some(BigDecimal(-10.00)))
     validateNegative(se, "lossBroughtForward")
    }

    "reject negative outstandingBusinessIncome" in {
      val se = SelfEmploymentAdjustments(outstandingBusinessIncome = Some(BigDecimal(-10.00)))
     validateNegative(se, "outstandingBusinessIncome")
    }

    "not reject negative basisAdjustment" in {
      val se = SelfEmploymentAdjustments(basisAdjustment = Some(BigDecimal(-10.00)))
      assertValidationPasses[SelfEmploymentAdjustments](se)
    }

    "not reject positive basisAdjustment" in {
      val se = SelfEmploymentAdjustments(basisAdjustment = Some(BigDecimal(10.00)))
      assertValidationPasses[SelfEmploymentAdjustments](se)
    }

  }
}
