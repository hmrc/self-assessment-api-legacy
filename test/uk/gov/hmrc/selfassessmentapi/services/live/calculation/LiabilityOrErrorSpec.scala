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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.selfassessmentapi.controllers.api.{UkTaxPaidForEmployment, ErrorCode}
import uk.gov.hmrc.selfassessmentapi.{LiabilitySugar, UnitSpec}
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxDeducted

class LiabilityOrErrorSpec extends UnitSpec {
  "LiabilityOrError.validate" should {
    "return the same liability passed if it contains a tax paid containing zero" in {
      val taxDeducted = TaxDeducted(
        ukTaxesPaidForEmployments = Seq(UkTaxPaidForEmployment("", -200.15),
                                        UkTaxPaidForEmployment("", -300.33),
                                        UkTaxPaidForEmployment("", 0)))

      val liability = LiabilitySugar.aLiability(taxDeducted = taxDeducted)

      LiabilityOrError(liability) shouldBe liability
    }

    "return the same liability passed if it contains a tax paid containing a positive number" in {
      val taxDeducted = TaxDeducted(
        ukTaxesPaidForEmployments = Seq(UkTaxPaidForEmployment("", -200.15),
          UkTaxPaidForEmployment("", -300.33),
          UkTaxPaidForEmployment("", 256.84)))

      val liability = LiabilitySugar.aLiability(taxDeducted = taxDeducted)

      LiabilityOrError(liability) shouldBe liability
    }

    "return a calculation error if passed a liability that contains invalid employment tax paid" in {
      val taxDeducted = TaxDeducted(
        ukTaxesPaidForEmployments = Seq(UkTaxPaidForEmployment("", -200.15),
          UkTaxPaidForEmployment("", -300.33),
          UkTaxPaidForEmployment("", -22)))

      val liability = LiabilitySugar.aLiability(taxDeducted = taxDeducted)

      val result = LiabilityOrError(liability)
      result.fold({ errors =>
        errors.errors.size shouldBe 1
        errors.errors.head.code shouldBe INVALID_EMPLOYMENT_TAX_PAID
      },{ results =>
        fail("This calculation should fail validation")
      })

    }
  }
}
