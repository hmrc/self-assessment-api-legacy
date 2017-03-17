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

package uk.gov.hmrc.selfassessmentapi.models

import uk.gov.hmrc.play.test.UnitSpec

trait MapperSpec extends UnitSpec {

  // test for lossy mapping between two product types: round trip mapping is possible for the smaller
  // of the two product types
  def roundTrip[A <: Product, B <: Product](a: A, b: B)(implicit ma: Mapper[A, B], mb: Mapper[B, A]): Unit = {
    if (a.productArity < b.productArity)
      (mb.from _ compose ma.from)(a) shouldEqual a
    else (ma.from _ compose mb.from)(b) shouldEqual b
  }

}
