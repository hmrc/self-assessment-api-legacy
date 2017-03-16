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

package uk.gov.hmrc.selfassessmentapi.models.properties

import org.joda.time.LocalDate
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models._
import org.scalacheck.Gen

class PropertiesPeriodSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  def amountGen(lower: BigDecimal, upper: BigDecimal): Gen[BigDecimal] =
    for {
      value <- Gen.chooseNum(lower.intValue(), upper.intValue())
    } yield BigDecimal(value)

  val genSimpleIncome: Gen[SimpleIncome] = for {
    amount <- amountGen(1000, 5000)
  } yield SimpleIncome(amount)

  val genIncome: Gen[Income] = for {
    amount <- amountGen(1000, 5000)
  } yield Income(amount, None)

  val genSimpleExpense: Gen[SimpleExpense] = for {
    amount <- amountGen(1000, 5000)
  } yield SimpleExpense(amount)

  def genFHLPropertiesPeriodicData(valid: Boolean): Gen[FHLProperties] =
    for {
      from <- Gen.const(LocalDate.now())
      to <- Gen.oneOf(from, from.plusDays(1))
      incomes <- Gen.mapOf(Gen.zip(Gen.oneOf(FHLIncomeType.values.toList), genSimpleIncome))
      expenses <- Gen.mapOf(Gen.zip(Gen.oneOf(FHLExpenseType.values.toList), genSimpleExpense))
    } yield
      if (valid) FHLProperties(from, to, FHLPeriodicData(incomes, expenses))
      else FHLProperties(from, from.minusDays(1), FHLPeriodicData(incomes, expenses))

  def genOtherPropertiesPeriodicData(valid: Boolean): Gen[OtherProperties] =
    for {
      from <- Gen.const(LocalDate.now())
      to <- Gen.oneOf(from, from.plusDays(1))
      incomes <- Gen.mapOf(Gen.zip(Gen.oneOf(IncomeType.values.toList), genIncome))
      expenses <- Gen.mapOf(Gen.zip(Gen.oneOf(ExpenseType.values.toList), genSimpleExpense))
    } yield
      if (valid) OtherProperties(from, to, OtherPeriodicData(incomes, expenses))
      else OtherProperties(from, from.minusDays(1), OtherPeriodicData(incomes, expenses))

  "PropertiesPeriod" should {

    "round trip FHL properties" in forAll(genFHLPropertiesPeriodicData(true)) { fhlProps =>
      roundTripJson(fhlProps)
    }

    "round trip Other properties" in forAll(genOtherPropertiesPeriodicData(true)) { otherProps =>
      roundTripJson(otherProps)
    }
  }

  "validate" should {

    "reject a FHL properties where the `to` date comes before the `from` date" in forAll(
      genFHLPropertiesPeriodicData(false)) { fhlProps =>
      assertValidationErrorWithCode(fhlProps, "", ErrorCode.INVALID_PERIOD)
    }

    "reject a Other properties where the `to` date comes before the `from` date" in forAll(
      genOtherPropertiesPeriodicData(false)) { otherProps =>
      assertValidationErrorWithCode(otherProps, "", ErrorCode.INVALID_PERIOD)
    }

  }
}
