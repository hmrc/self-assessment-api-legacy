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

package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.domain.SaUtrGenerator
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear

import scala.concurrent.duration._

trait UnitSpec extends uk.gov.hmrc.play.test.UnitSpec {

  override implicit val defaultTimeout: FiniteDuration = 30 seconds

  implicit def taxYearToString(taxYear: TaxYear): String = taxYear.value

  private val saUtrGenerator = new SaUtrGenerator()

  def generateSaUtr() = saUtrGenerator.nextSaUtr

  val taxYear = TaxYear("2016-17")

  case class Print(value: BigDecimal) {
    def as(name: String) = {
      println(s"$name => $value")
      value
    }
  }

}
