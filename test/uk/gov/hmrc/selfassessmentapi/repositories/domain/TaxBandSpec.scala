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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.Generators._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand, TaxBandRangeCheck}

class TaxBandSpec extends UnitSpec with GeneratorDrivenPropertyChecks {

  "isWithin" should {

    "return true for every valid value in the Basic Tax Band" in {
      val basicTaxBandLower = 1
      val basicTaxBandUpper = 32000
      forAll(amountGen(basicTaxBandLower, basicTaxBandUpper)) { amount =>
        amount isWithin BasicTaxBand shouldBe true
      }
    }

    "return true for every valid value in the Higher Tax Band" in {
      val higherTaxBandLower = 32001
      val higherTaxBandUpper = 150000
      forAll(amountGen(higherTaxBandLower, higherTaxBandUpper)) { amount =>
        amount isWithin HigherTaxBand shouldBe true
      }
    }

    "return true for every valid value in the Additional Higher Tax Band" in {
      val additionalHigherTaxBandLower = 150001
      forAll(amountGen(additionalHigherTaxBandLower, Int.MaxValue)) { amount =>
        amount isWithin AdditionalHigherTaxBand shouldBe true
      }
    }
  }

  "width" should {

    "return the difference between upper and lower bound" in {

      HigherTaxBand.width shouldBe 118000
    }

    "return the max value if upper bound is None" in {

      AdditionalHigherTaxBand.width shouldBe Long.MaxValue
    }
  }
}
