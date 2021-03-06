/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import uk.gov.hmrc.utils.TaxYear

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait BaseUnitSpec extends Matchers with OptionValues with TestUtils {
  implicit val timeout: FiniteDuration = 5 seconds

  def await[T](f: Future[T])(implicit duration: FiniteDuration = timeout): T =
    Await.result(f, duration)
}

trait UnitSpec extends AnyWordSpec with BaseUnitSpec

trait AsyncUnitSpec extends AsyncWordSpec with BaseUnitSpec

trait TestUtils {
  private val ninoGenerator = NinoGenerator()

  def generateNino = ninoGenerator.nextNino()

  def now = DateTime.now(DateTimeZone.UTC)

  val taxYear: TaxYear = TaxYear("2017-18")

  implicit def taxYearToString(taxYear: TaxYear): String = taxYear.value
}

object TestUtils extends TestUtils
