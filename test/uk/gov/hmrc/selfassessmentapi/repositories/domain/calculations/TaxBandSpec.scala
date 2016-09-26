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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand._

class TaxBandSpec extends UnitSpec {
  "BasicTaxBand" should {
    "round up the upper bound to the nearest pound" in {
      BasicTaxBand(reductionInUpperBound = 0, additionsToUpperBound = 500.2).upperBound shouldBe Some(32501)
    }
  }

  "HigherTaxBand" should {
    "round up the upper bound to the nearest pound" in {
      HigherTaxBand(additionsToUpperBound = 500.2).upperBound shouldBe Some(150501)
    }
  }
}
