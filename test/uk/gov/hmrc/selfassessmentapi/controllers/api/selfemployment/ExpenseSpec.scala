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

package uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.Generators.amountGen
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType._

class ExpenseSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "format" should {

    val genValidExpense = for {
      expenseType <- Gen.oneOf(ExpenseType.values.toList)
      totalAmount <- amountGen(1000, 5000)
      disallowableAmount <- { // wrap if in a block to get proper indentation
        if (expenseType == ExpenseType.Depreciation) Gen.const(totalAmount)
        else amountGen(1, totalAmount - 1)
      }
    } yield Expense(`type` = expenseType, totalAmount = totalAmount, disallowableAmount = Some(disallowableAmount))

    "round trip Expense json" in forAll(genValidExpense) { expense =>
      roundTripJson(expense)
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach {
        testAmount =>
          val seExpense = Expense(`type` = CISPaymentsToSubcontractors, totalAmount = testAmount)
          assertValidationError[Expense](seExpense,
                                         Map("/totalAmount" -> INVALID_MONETARY_AMOUNT),
                                         "Expected invalid self-employment-income")
      }
    }

    "reject negative monetary amounts" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-10.12)).foreach { testAmount =>
        val seExpense = Expense(`type` = CISPaymentsToSubcontractors, totalAmount = testAmount)
        assertValidationError[Expense](seExpense,
                                       Map("/totalAmount" -> INVALID_MONETARY_AMOUNT),
                                       "Expected invalid self-employment-income")
      }
    }

    "reject negative amount" in {
      val seExpense = Expense(`type` = CISPaymentsToSubcontractors, totalAmount = BigDecimal(-1000.12))
      assertValidationError[Expense](seExpense,
                                     Map("/totalAmount" -> INVALID_MONETARY_AMOUNT),
                                     "Expected negative self-employment expense")
    }

    "reject invalid Expense category" in {
      val json = Json.parse("""
          |{ "type": "BAZ",
          |"totalAmount" : 10000.45
          |}
        """.stripMargin)

      assertValidationError[Expense](json,
                                     Map("/type" -> NO_VALUE_FOUND),
                                     s"Expected expense type not in {${ExpenseType.values.mkString(", ")}}")
    }

    val genExpenseWithInvalidDisallowableAmount = for {
      expenseType <- Gen.oneOf((ExpenseType.values - ExpenseType.Depreciation).toList)
      totalAmount <- amountGen(1000, 5000)
      disallowableAmount <- amountGen(totalAmount + 1, totalAmount + 1000)
    } yield Expense(`type` = expenseType, totalAmount = totalAmount, disallowableAmount = Some(disallowableAmount))

    "reject Expense with disallowable amount greater than total amount" in forAll(
      genExpenseWithInvalidDisallowableAmount) { expense =>
      assertValidationError[Expense](expense,
                                     Map("" -> INVALID_DISALLOWABLE_AMOUNT),
                                     "Expected disallowable amount to be greater than total amount")
    }

    val genInvalidDepreciationExpense = for {
      totalAmount <- amountGen(1000, 5000)
      optDisallowableAmount <- Gen.option(amountGen(1, totalAmount - 1))
    } yield
      Expense(`type` = ExpenseType.Depreciation, totalAmount = totalAmount, disallowableAmount = optDisallowableAmount)

    "reject Depreciation Expense where disallowable amount is different than total amount" in forAll(
      genInvalidDepreciationExpense) { expense =>
      assertValidationError[Expense](expense,
                                     Map("" -> DEPRECIATION_DISALLOWABLE_AMOUNT),
                                     "Expected disallowable amount to be equal to the total amount")
    }
  }
}
