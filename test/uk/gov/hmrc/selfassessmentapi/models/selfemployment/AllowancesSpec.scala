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
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode.INVALID_MONETARY_AMOUNT

class AllowancesSpec extends JsonSpec {

  "format" should {
    "round trip valid Allowances json" in {
      roundTripJson(Allowances(
        annualInvestmentAllowance = Some(10.00),
        capitalAllowanceMainPool = Some(10.00),
        capitalAllowanceSpecialRatePool = Some(10.00),
        businessPremisesRenovationAllowance = Some(10.00),
        enhancedCapitalAllowance = Some(10.00),
        allowanceOnSales = Some(10.00)))
    }

    "round trip Allowances with no fields" in {
      roundTripJson(Allowances())
    }
  }

  "validate" should {
    def validateNonNegativeAmount(model: Allowances, fieldName: String) = {
      assertValidationErrorWithCode(
        model,
        fieldName, INVALID_MONETARY_AMOUNT)
    }

    "reject negative annualInvestmentAllowance" in {
      val se = Allowances(annualInvestmentAllowance = Some(-10.00))
      validateNonNegativeAmount(se, "/annualInvestmentAllowance")
    }

    "reject annualInvestmentAllowance with more than 2 decimal places" in {
      val se = Allowances(annualInvestmentAllowance = Some(10.123))
      validateNonNegativeAmount(se, "/annualInvestmentAllowance")
    }

    "reject negative capitalAllowanceMainPool" in {
      val se = Allowances(capitalAllowanceMainPool = Some(-10.00))
      validateNonNegativeAmount(se, "/capitalAllowanceMainPool")
    }

    "reject capitalAllowanceMainPool with more than 2 decimal places" in {
      val se = Allowances(capitalAllowanceMainPool = Some(10.123))
      validateNonNegativeAmount(se, "/capitalAllowanceMainPool")
    }

    "reject negative capitalAllowanceSpecialRatePool" in {
      val se = Allowances(capitalAllowanceSpecialRatePool = Some(-10.00))
      validateNonNegativeAmount(se, "/capitalAllowanceSpecialRatePool")
    }

    "reject capitalAllowanceSpecialRatePool with more than 2 decimal places" in {
      val se = Allowances(capitalAllowanceSpecialRatePool = Some(10.123))
      validateNonNegativeAmount(se, "/capitalAllowanceSpecialRatePool")
    }

    "reject negative businessPremisesRenovationAllowance" in {
      val se = Allowances(businessPremisesRenovationAllowance = Some(-10.00))
      validateNonNegativeAmount(se, "/businessPremisesRenovationAllowance")
    }

    "reject businessPremisesRenovationAllowance with more than 2 decimal places" in {
      val se = Allowances(businessPremisesRenovationAllowance = Some(10.123))
      validateNonNegativeAmount(se, "/businessPremisesRenovationAllowance")
    }

    "reject negative enhancedCapitalAllowance" in {
      val se = Allowances(enhancedCapitalAllowance = Some(-10.00))
      validateNonNegativeAmount(se, "/enhancedCapitalAllowance")
    }

    "reject enhancedCapitalAllowance with more than 2 decimal places" in {
      val se = Allowances(enhancedCapitalAllowance = Some(10.123))
      validateNonNegativeAmount(se, "/enhancedCapitalAllowance")
    }

    "reject negative allowancesOnSales" in {
      val se = Allowances(allowanceOnSales = Some(-10.00))
      validateNonNegativeAmount(se, "/allowanceOnSales")
    }

    "reject allowancesOnSales with more than 2 decimal places" in {
      val se = Allowances(allowanceOnSales = Some(10.123))
      validateNonNegativeAmount(se, "/allowanceOnSales")
    }

    "reject negative zeroEmissionGoodsVehicleAllowance" in {
      val se = Allowances(zeroEmissionGoodsVehicleAllowance = Some(-10))
      validateNonNegativeAmount(se, "/zeroEmissionGoodsVehicleAllowance")
    }
    
    "reject zeroEmissionGoodsVehicleAllowance with more than 2 decimal places" in {
      val se = Allowances(zeroEmissionGoodsVehicleAllowance = Some(10.123))
      validateNonNegativeAmount(se, "/zeroEmissionGoodsVehicleAllowance")
    }
  }
}
