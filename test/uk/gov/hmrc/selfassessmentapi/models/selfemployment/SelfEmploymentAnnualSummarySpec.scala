/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class SelfEmploymentAnnualSummarySpec extends JsonSpec {
  "format" should {
    "round trip" in {
      val summary = SelfEmploymentAnnualSummary(
        Some(Allowances(
          annualInvestmentAllowance = Some(50.50),
          capitalAllowanceMainPool = Some(12.34),
          capitalAllowanceSpecialRatePool = Some(55.65),
          businessPremisesRenovationAllowance = Some(20.20),
          enhancedCapitalAllowance = Some(12.23),
          allowanceOnSales = Some(87.56),
          zeroEmissionGoodsVehicleAllowance = Some(5.33)
        )),
        Some(Adjustments(
          includedNonTaxableProfits = Some(12.22),
          basisAdjustment = Some(55.55),
          overlapReliefUsed = Some(12.23),
          accountingAdjustment = Some(12.23),
          averagingAdjustment = Some(-12.22),
          lossBroughtForward = Some(22.22),
          outstandingBusinessIncome = Some(300.33),
          balancingChargeBPRA = Some(10.55),
          balancingChargeOther = Some(5.55),
          goodsAndServicesOwnUse = Some(12.23)
        )))

      roundTripJson(summary)
    }

    "round trip empty json" in {
      roundTripJson(SelfEmploymentAnnualSummary(None, None))
    }
  }

  "validate" should {
    "reject annual summaries where allowances.businessPremisesRenovationAllowance is not defined but adjustments.balancingChargeBPRA is defined" in {
      val summary = SelfEmploymentAnnualSummary(
        Some(Allowances(businessPremisesRenovationAllowance = Some(0))),
        Some(Adjustments(balancingChargeBPRA = Some(200.90))))

      assertValidationErrorWithCode(summary, "", ErrorCode.INVALID_BALANCING_CHARGE_BPRA)
    }

    "accept annual summaries with only businessPremisesRenovationAllowance defined" in {
      val summary = SelfEmploymentAnnualSummary(
        Some(Allowances(businessPremisesRenovationAllowance = Some(0))),
        None)

      assertValidationPasses(summary)
    }
  }

}
