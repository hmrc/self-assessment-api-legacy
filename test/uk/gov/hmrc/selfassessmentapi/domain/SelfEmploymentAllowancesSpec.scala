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

import ErrorCode._

class SelfEmploymentAllowancesSpec extends JsonSpec {

  "format" should {
    "round trip valid SelfEmploymentAllowances json" in {
      roundTripJson(SelfEmploymentAllowances(
        annualInvestmentAllowance = Some(BigDecimal(10.00)),
        capitalAllowanceMainPool = Some(BigDecimal(10.00)),
        capitalAllowanceSpecialRatePool = Some(BigDecimal(10.00)),
        restrictedCapitalAllowance = Some(BigDecimal(10.00)),
        businessPremisesRenovationAllowance = Some(BigDecimal(10.00)),
        enhancedCapitalAllowance = Some(BigDecimal(10.00)),
        allowancesOnSales = Some(BigDecimal(10.00))))
    }
  }

  "validate" should {
    def validateNegative(model: SelfEmploymentAllowances, fieldName: String) = {
      assertValidationError[SelfEmploymentAllowances](
        model,
        Map((s"/$fieldName", INVALID_MONETARY_AMOUNT) -> s"$fieldName should be non-negative number up to 2 decimal values"),
        "Expected valid self-employment-allowance")
    }

    "reject negative annualInvestmentAllowance" in {
      val se = SelfEmploymentAllowances(annualInvestmentAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "annualInvestmentAllowance")
    }

    "reject negative capitalAllowanceMainPool" in {
      val se = SelfEmploymentAllowances(capitalAllowanceMainPool = Some(BigDecimal(-10.00)))
     validateNegative(se, "capitalAllowanceMainPool")
    }

    "reject negative capitalAllowanceSpecialRatePool" in {
      val se = SelfEmploymentAllowances(capitalAllowanceSpecialRatePool = Some(BigDecimal(-10.00)))
     validateNegative(se, "capitalAllowanceSpecialRatePool")
    }

    "reject negative restrictedCapitalAllowance" in {
      val se = SelfEmploymentAllowances(restrictedCapitalAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "restrictedCapitalAllowance")
    }

    "reject negative businessPremisesRenovationAllowance" in {
      val se = SelfEmploymentAllowances(businessPremisesRenovationAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "businessPremisesRenovationAllowance")
    }

    "reject negative enhancedCapitalAllowance" in {
      val se = SelfEmploymentAllowances(enhancedCapitalAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "enhancedCapitalAllowance")
    }

    "reject negative allowancesOnSales" in {
      val se = SelfEmploymentAllowances(allowancesOnSales = Some(BigDecimal(-10.00)))
     validateNegative(se, "allowancesOnSales")
    }

  }
}
