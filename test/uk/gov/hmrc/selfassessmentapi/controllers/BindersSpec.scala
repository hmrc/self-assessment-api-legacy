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
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes
import uk.gov.hmrc.selfassessmentapi.controllers.api._

class BindersSpec extends UnitSpec {

  "saUtrBinder.bind" should {

    "return Right with a SaUtr instance for a valid utr string" in {
      val utr = generateSaUtr()
      implicit val pathBindable = PathBindable.bindableString

      val result = Binders.saUtrBinder.bind("saUtr", utr.utr)
      result shouldEqual Right(utr)
    }

    "return Left for an ivalid utr string" in {
      val utr = "invalid"
      implicit val pathBindable = PathBindable.bindableString

      val result = Binders.saUtrBinder.bind("saUtr", utr)
      result shouldEqual Left("ERROR_SA_UTR_INVALID")
    }
  }

  "taxYear.bind" should {

    "return Right with a TaxYear instance for a valid tax year string" in {
      val taxYear = "2016-17"
      implicit val pathBindable = PathBindable.bindableString

      val result = Binders.taxYearBinder.bind("taxYear", taxYear)
      result shouldEqual Right(TaxYear(taxYear))
    }

    "return Left for an invalid taxYear string" in {
      val taxYear = "invalid"
      implicit val pathBindable = PathBindable.bindableString

      val result = Binders.taxYearBinder.bind("taxYear", taxYear)
      result shouldEqual Left("ERROR_TAX_YEAR_INVALID")
    }
  }

  "sourceType.bind" should {

    implicit val pathBindable = PathBindable.bindableString

    "return Right with a Source Type instance for a self-employments" in {
      SourceTypes.types.foreach { `type` =>
        val result = Binders.sourceTypeBinder.bind("sourceType", `type`.name)
        result shouldEqual Right(`type`)
      }
    }

    "return Left for an invalid sourceType string" in {
      val result = Binders.sourceTypeBinder.bind("summaryType", "invalid")
      result shouldEqual Left("ERROR_INVALID_SOURCE_TYPE")
    }
  }

}
