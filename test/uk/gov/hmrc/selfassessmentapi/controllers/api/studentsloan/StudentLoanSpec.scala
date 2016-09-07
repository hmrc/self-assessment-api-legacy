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

package uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class StudentLoanSpec extends JsonSpec {

  "format" should {
    "round trip valid json" in {
      roundTripJson(StudentLoan.example())
    }
  }

  "validate" should {

    "reject amounts with more than 2 decimal values" in {
      assertValidationError[StudentLoan](
        StudentLoan(planType = StudentLoanPlanType.Plan1, deductedByEmployers = Some(BigDecimal(1000.123))),
        Map("/deductedByEmployers" -> INVALID_MONETARY_AMOUNT), "Expected validation error for deductedByEmployers for amount with more than 2 decimal places"
      )
    }

    "reject negative amount" in {
      assertValidationError[StudentLoan](
        StudentLoan(planType = StudentLoanPlanType.Plan1, deductedByEmployers = Some(BigDecimal(-1000.00))),
        Map("/deductedByEmployers" -> INVALID_MONETARY_AMOUNT), "Expected validation error for deductedByEmployers for negative amount"
      )
    }

    "accept a student loan without specifying the amount already deducted by employers" in {
      val sl = StudentLoan(planType = StudentLoanPlanType.Plan1, deductedByEmployers = None)
      assertValidationPasses[StudentLoan](sl)
    }
  }
}
