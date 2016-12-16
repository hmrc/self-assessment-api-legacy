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
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, Period, PeriodId}

class PeriodValidatorSpec extends UnitSpec {
  case class TestPeriod(from: LocalDate, to: LocalDate) extends Period
  case class TestPeriodValidator(periods: Map[PeriodId, TestPeriod]) extends PeriodValidator[TestPeriodValidator, TestPeriod] {
    override val accountingPeriod: AccountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05"))
  }

  "containsOverlappingPeriod" should {
    "return true when adding a period that overlaps with an existing period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")))) {
        containsOverlappingPeriod(TestPeriod(LocalDate.parse("2018-04-05"), LocalDate.parse("2018-04-06"))) shouldBe true
      }

    "return false when adding a period that does not overlap with an existing period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")))) {
        containsOverlappingPeriod(TestPeriod(LocalDate.parse("2018-04-06"), LocalDate.parse("2018-04-06"))) shouldBe false
      }
  }

  "containsGaps" should {
    "return true when adding a period that creates a gap" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")))) {
      containsGap(TestPeriod(LocalDate.parse("2018-04-07"), LocalDate.parse("2018-04-08"))) shouldBe true
    }

    "return false when adding a period that does not create a gap" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")))) {
        containsGap(TestPeriod(LocalDate.parse("2018-04-06"), LocalDate.parse("2018-04-08"))) shouldBe false
      }
  }

  "containsMisalignedPeriod" should {
    "return true when adding a period that is not aligned to the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-06"))) shouldBe true
      }

    "return False when adding a period that is aligned to the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-05"))) shouldBe false
      }
  }

  "containsPeriod" should {
    "return true when adding a period that already exists" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
      containsPeriod(TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01"))) shouldBe Some("")
    }

    "return false when adding a period that does not yet exist" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        containsPeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-03"))) shouldBe None
      }
  }
}
