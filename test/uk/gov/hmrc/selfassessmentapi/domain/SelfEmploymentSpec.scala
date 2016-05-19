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

import org.joda.time.LocalDate
import ErrorCode._

class SelfEmploymentSpec extends JsonSpec {

  "format" should {
    "round trip valid SelfEmployment json" in {
      roundTripJson(SelfEmployment(
        name = "self employment 1",
        commencementDate = new LocalDate(2016, 4, 22),
        allowances = Some(SelfEmploymentAllowances(annualInvestmentAllowance = Some(BigDecimal(10))))))
    }
  }

  "validate" should {
    "reject name longer than 100 characters and commencement date after the present date" in {

      val se = SelfEmployment(name = "a" * 101, commencementDate = LocalDate.now().plusDays(1))

      assertValidationError[SelfEmployment](
        se,
        Map(("/commencementDate", COMMENCEMENT_DATE_NOT_IN_THE_PAST) -> "commencement date should be in the past",
          ("/name", MAX_FIELD_LENGTH_EXCEEDED) -> "field length exceeded the max 100 chars"),
        "Expected valid self-employment")
    }

    "reject invalid allowances" in {

      val se = SelfEmployment(
        name = "self employment 1",
        commencementDate = new LocalDate(2016, 4, 22),
        allowances = Some(SelfEmploymentAllowances(annualInvestmentAllowance = Some(BigDecimal(-10)))))

      assertValidationError[SelfEmployment](
        se,
        Map(("/allowances/annualInvestmentAllowance", INVALID_MONETARY_AMOUNT) -> s"annualInvestmentAllowance should be non-negative number up to 2 decimal values"),
        "Expected valid self-employment")
    }

  }
}
