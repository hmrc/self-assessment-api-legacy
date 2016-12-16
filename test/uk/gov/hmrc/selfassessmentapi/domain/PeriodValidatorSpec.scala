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
  case class TestPeriodValidator(periods: Map[PeriodId, TestPeriod]) extends PeriodValidator[TestPeriod]

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
    val accountingPeriod: AccountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05"))

    "return true when adding a period whose end date is beyond the end of the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-06")), accountingPeriod) shouldBe true
      }

    "return false when adding a period whose end date is equal to the end of the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-05")), accountingPeriod) shouldBe false
      }

    "return false when adding a period whose end date is before to the end of the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-04")), accountingPeriod) shouldBe false
      }

    "return true when adding a first period whose start date is before the start of the accounting period" in
      new TestPeriodValidator(Map.empty) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2017-04-05"), LocalDate.parse("2017-04-15")), accountingPeriod) shouldBe true
      }

    "return true when adding a first period whose start date is after the start of the accounting period" in
      new TestPeriodValidator(Map.empty) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2017-04-07"), LocalDate.parse("2017-04-08")), accountingPeriod) shouldBe true
    }

    "return false when adding a furst period whose start date is equal to the start of the accounting period" in
      new TestPeriodValidator(Map.empty) {
        containsMisalignedPeriod(TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2017-04-07")), accountingPeriod) shouldBe false
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
