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

package uk.gov.hmrc.selfassessmentapi.resources.models

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class SelfEmploymentAdjustmentsSpec extends JsonSpec {

  "format" should {
    "round trip valid Adjustments json" in {
      roundTripJson(SelfEmploymentAdjustments(
        includedNonTaxableProfits = Some(BigDecimal(10.00)),
        basisAdjustment = Some(BigDecimal(10.00)),
        overlapReliefUsed = Some(BigDecimal(10.00)),
        accountingAdjustment = Some(BigDecimal(10.00)),
        averagingAdjustment = Some(BigDecimal(10.00)),
        lossBroughtForward = Some(BigDecimal(10.00)),
        outstandingBusinessIncome = Some(BigDecimal(10.00))))
    }

    "round trip Adjustments with no fields" in {
      roundTripJson(SelfEmploymentAdjustments())
    }
  }

  "validate" should {
    def validatePositiveAmount(model: SelfEmploymentAdjustments, path: String) = {
      assertValidationErrorWithCode(model, path,
        INVALID_MONETARY_AMOUNT)
    }

    def validateAmount(model: SelfEmploymentAdjustments, path: String) = {
      assertValidationErrorWithCode(model, path,
        INVALID_MONETARY_AMOUNT)
    }

    "reject negative includedNonTaxableProfits" in {
      val se = SelfEmploymentAdjustments(includedNonTaxableProfits = Some(BigDecimal(-10.00)))
      validatePositiveAmount(se, "/includedNonTaxableProfits")
    }

    "reject includedNonTaxableProfits with more than two decimal places" in {
      val se = SelfEmploymentAdjustments(includedNonTaxableProfits = Some(BigDecimal(10.123)))
      validatePositiveAmount(se, "/includedNonTaxableProfits")
    }

    "reject basisAdjustment with more than two decimal places" in {
      val se = SelfEmploymentAdjustments(basisAdjustment = Some(BigDecimal(10.123)))
      validateAmount(se, "/basisAdjustment")
    }

    "reject negative overlapReliefUsed" in {
      val se = SelfEmploymentAdjustments(overlapReliefUsed = Some(BigDecimal(-10.00)))
     validatePositiveAmount(se, "/overlapReliefUsed")
    }

    "reject overlapReliefUsed with more than two decimal places" in {
      val se = SelfEmploymentAdjustments(overlapReliefUsed = Some(BigDecimal(10.123)))
      validatePositiveAmount(se, "/overlapReliefUsed")
    }

    "reject negative accountingAdjustment" in {
      val se = SelfEmploymentAdjustments(accountingAdjustment = Some(BigDecimal(-10.00)))
      validatePositiveAmount(se, "/accountingAdjustment")
    }

    "reject accountingAdjustment with more than two decimal places" in {
      val se = SelfEmploymentAdjustments(accountingAdjustment = Some(BigDecimal(10.123)))
      validatePositiveAmount(se, "/accountingAdjustment")
    }

    "reject averagingAdjustment with more than two decimal places" in {
      val se = SelfEmploymentAdjustments(averagingAdjustment = Some(BigDecimal(10.123)))
      validateAmount(se, "/averagingAdjustment")
    }

    "reject negative lossBroughtForward" in {
      val se = SelfEmploymentAdjustments(lossBroughtForward = Some(BigDecimal(-10.00)))
      validatePositiveAmount(se, "/lossBroughtForward")
    }

    "reject lossBroughtForward with more than two decimal places" in {
      val se = SelfEmploymentAdjustments(lossBroughtForward = Some(BigDecimal(10.123)))
      validatePositiveAmount(se, "/lossBroughtForward")
    }

    "reject negative outstandingBusinessIncome" in {
      val se = SelfEmploymentAdjustments(outstandingBusinessIncome = Some(BigDecimal(-10.00)))
      validatePositiveAmount(se, "/outstandingBusinessIncome")
    }

    "reject outstandingBusinessIncome with more than two decimal places" in {
      val se = SelfEmploymentAdjustments(outstandingBusinessIncome = Some(BigDecimal(10.123)))
      validatePositiveAmount(se, "/outstandingBusinessIncome")
    }
  }
}
