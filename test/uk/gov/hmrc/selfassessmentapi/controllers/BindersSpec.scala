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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.mvc.PathBindable
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.test.UnitSpec

class BindersSpec extends UnitSpec {

  "saUtrBinder.bind" should {
    "return Right with a SaUtr instance for a valid utr string" in {
      val utr = "2234567890"
      implicit val pathBindable = PathBindable.bindableString

      val result = Binders.saUtrBinder.bind("saUtr", utr)
      result shouldEqual Right(SaUtr(utr))
    }

    "return Left for an ivalid utr string" in {
      val utr = "invalid"
      implicit val pathBindable = PathBindable.bindableString

      val result = Binders.saUtrBinder.bind("saUtr", utr)
      result shouldEqual Left("ERROR_SA_UTR_INVALID")
    }
  }

}
