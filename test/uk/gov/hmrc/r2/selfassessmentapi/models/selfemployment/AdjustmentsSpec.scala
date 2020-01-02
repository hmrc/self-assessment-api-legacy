/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment

import uk.gov.hmrc.r2.selfassessmentapi.models.ErrorCode.INVALID_MONETARY_AMOUNT
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class AdjustmentsSpec extends JsonSpec {

  "format" should {
    "round trip valid Adjustments json" in {
      roundTripJson(Adjustments(
        includedNonTaxableProfits = Some(BigDecimal(10.00)),
        basisAdjustment = Some(BigDecimal(10.00)),
        overlapReliefUsed = Some(BigDecimal(10.00)),
        accountingAdjustment = Some(BigDecimal(10.00)),
        averagingAdjustment = Some(BigDecimal(10.00)),
        lossBroughtForward = Some(BigDecimal(10.00)),
        outstandingBusinessIncome = Some(BigDecimal(10.00)),
        balancingChargeBPRA = Some(BigDecimal(10.00)),
        balancingChargeOther = Some(BigDecimal(10.00)),
        goodsAndServicesOwnUse = Some(BigDecimal(10.00))))

    }

    "round trip Adjustments with no fields" in {
      roundTripJson(Adjustments())
    }
  }

  "validate" should {
   
    def validateAmount(model: Adjustments, path: String) = {
      assertValidationErrorWithCode(model, path, INVALID_MONETARY_AMOUNT)
    }

    "reject negative includedNonTaxableProfits" in {
      val se = Adjustments(includedNonTaxableProfits = Some(-10.00))
      validateAmount(se, "/includedNonTaxableProfits")
    }

    "reject includedNonTaxableProfits more than 99999999999999.98" in {
      val se = Adjustments(includedNonTaxableProfits = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/includedNonTaxableProfits")
    }

    "reject includedNonTaxableProfits with more than two decimal places" in {
      val se = Adjustments(includedNonTaxableProfits = Some(BigDecimal(10.123)))
      validateAmount(se, "/includedNonTaxableProfits")
    }

    "reject basisAdjustment with more than two decimal places" in {
      val se = Adjustments(basisAdjustment = Some(BigDecimal(10.123)))
      validateAmount(se, "/basisAdjustment")
    }

    "reject basisAdjustment more than 99999999999999.98" in {
      val se = Adjustments(basisAdjustment = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/basisAdjustment")
    }

    "reject negative overlapReliefUsed" in {
      val se = Adjustments(overlapReliefUsed = Some(-10.00))
      validateAmount(se, "/overlapReliefUsed")
    }

    "reject overlapReliefUsed more than 99999999999999.98" in {
      val se = Adjustments(overlapReliefUsed = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/overlapReliefUsed")
    }

    "reject overlapReliefUsed with more than two decimal places" in {
      val se = Adjustments(overlapReliefUsed = Some(BigDecimal(10.123)))
      validateAmount(se, "/overlapReliefUsed")
    }

    "reject negative accountingAdjustment" in {
      val se = Adjustments(accountingAdjustment = Some(-10.00))
      validateAmount(se, "/accountingAdjustment")
    }

    "reject accountingAdjustment more than 99999999999999.98" in {
      val se = Adjustments(accountingAdjustment = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/accountingAdjustment")
    }

    "reject accountingAdjustment with more than two decimal places" in {
      val se = Adjustments(accountingAdjustment = Some(BigDecimal(10.123)))
      validateAmount(se, "/accountingAdjustment")
    }

    "reject averagingAdjustment with more than two decimal places" in {
      val se = Adjustments(averagingAdjustment = Some(BigDecimal(10.123)))
      validateAmount(se, "/averagingAdjustment")
    }

    "reject averagingAdjustment more than 99999999999999.98" in {
      val se = Adjustments(averagingAdjustment = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/averagingAdjustment")
    }

    "reject negative lossBroughtForward" in {
      val se = Adjustments(lossBroughtForward = Some(-10.00))
      validateAmount(se, "/lossBroughtForward")
    }

    "reject lossBroughtForward more than 99999999999999.98" in {
      val se = Adjustments(lossBroughtForward = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/lossBroughtForward")
    }

    "reject lossBroughtForward with more than two decimal places" in {
      val se = Adjustments(lossBroughtForward = Some(BigDecimal(10.123)))
      validateAmount(se, "/lossBroughtForward")
    }

    "reject negative outstandingBusinessIncome" in {
      val se = Adjustments(outstandingBusinessIncome = Some(-10.00))
      validateAmount(se, "/outstandingBusinessIncome")
    }

    "reject outstandingBusinessIncome more than 99999999999999.98" in {
      val se = Adjustments(outstandingBusinessIncome = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/outstandingBusinessIncome")
    }

    "reject outstandingBusinessIncome with more than two decimal places" in {
      val se = Adjustments(outstandingBusinessIncome = Some(BigDecimal(10.123)))
      validateAmount(se, "/outstandingBusinessIncome")
    }

    "reject negative balancingChargeBPRA" in {
      val se = Adjustments(balancingChargeBPRA = Some(-10.00))
      validateAmount(se, "/balancingChargeBPRA")
    }

    "reject balancingChargeBPRA more than 99999999999999.98" in {
      val se = Adjustments(balancingChargeBPRA = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/balancingChargeBPRA")
    }
    
    "reject balancingChargeBPRA with more than two decimal places" in {
      val se = Adjustments(balancingChargeBPRA = Some(BigDecimal(10.123)))
      validateAmount(se, "/balancingChargeBPRA")
    }

    "reject negative balancingChargeOther" in {
      val se = Adjustments(balancingChargeOther = Some(-10.00))
      validateAmount(se, "/balancingChargeOther")
    }

    "reject balancingChargeOther with more than two decimal places" in {
      val se = Adjustments(balancingChargeOther = Some(BigDecimal(10.123)))
      validateAmount(se, "/balancingChargeOther")
    }

    "reject balancingChargeOther more than 99999999999999.98" in {
      val se = Adjustments(balancingChargeOther = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/balancingChargeOther")
    }

    "reject negative goodsAndServicesOwnUse" in {
      val se = Adjustments(goodsAndServicesOwnUse = Some(-10.00))
      validateAmount(se, "/goodsAndServicesOwnUse")
    }

    "reject goodsAndServicesOwnUse more than 99999999999999.98" in {
      val se = Adjustments(goodsAndServicesOwnUse = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/goodsAndServicesOwnUse")
    }

    "reject goodsAndServicesOwnUse with more than two decimal places" in {
      val se = Adjustments(goodsAndServicesOwnUse = Some(10.123))
      validateAmount(se, "/goodsAndServicesOwnUse")
    }
  }
}
