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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.BasicTaxBand

class CalculationStepSpec extends UnitSpec {

  "TaxBandState.allocate" should {

    "fully allocate the income if it is less than the available income in the band" in {

      val allocated = TaxBandState(taxBand = BasicTaxBand, available = 1000) allocate 999

      allocated shouldBe 999
    }

    "fully allocate the income if it is equal to the available income in the band" in {

      val allocated = TaxBandState(taxBand = BasicTaxBand, available = 999) allocate 999

      allocated shouldBe 999
    }

    "allocate the income up to the available income in the band" in {

      val allocated = TaxBandState(taxBand = BasicTaxBand, available = 500) allocate 999

      allocated shouldBe 500
    }
  }
}
