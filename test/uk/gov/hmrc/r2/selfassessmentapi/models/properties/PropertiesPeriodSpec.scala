/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties


import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.r2.selfassessmentapi.models._
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class PropertiesPeriodSpec extends JsonSpec {

  // FHL
  val fhlIncomes: FHL.Incomes = FHL.Incomes(
    rentIncome = Some(Income(10.25, Some(10.25))),
    rarRentReceived = Some(IncomeR2(10.25, Some(10.25)))
  )
  val fhlExpenses: FHL.Expenses = FHL.Expenses(
    premisesRunningCosts = Some(FHL.Expense(10.25)),
    repairsAndMaintenance = Some(FHL.Expense(10.25)),
    financialCosts = Some(FHL.Expense(10.25)),
    professionalFees = Some(FHL.Expense(10.25)),
    costOfServices = Some(FHL.Expense(10.25)),
    consolidatedExpenses = None,
    other = Some(FHL.Expense(10.25)),
    travelCosts = Some(FHL.ExpenseR2(10.25)),
    rarReliefClaimed = Some(FHL.ExpenseR2(10.25))

  )
  val fhlFinancials: FHL.Financials = FHL.Financials(
    incomes = Some(fhlIncomes),
    expenses = Some(fhlExpenses)
  )
  val fhlProperties: FHL.Properties = FHL.Properties(
    id = None,
    from = LocalDate.parse("2020-01-28"),
    to = LocalDate.parse("2020-01-29"),
    financials = Some(fhlFinancials)
  )


  // Other
  val otherIncomes: Other.Incomes = Other.Incomes(
    rentIncome = Some(Income(10.25, Some(10.25))),
    premiumsOfLeaseGrant = Some(Income(10.25, Some(10.25))),
    reversePremiums = Some(Income(10.25, Some(10.25))),
    otherPropertyIncome = Some(Income(10.25, Some(10.25))),
    rarRentReceived = Some(IncomeR2(10.25, Some(10.25)))
  )
  val otherExpenses: Other.Expenses = Other.Expenses(
    premisesRunningCosts = Some(Other.Expense(10.25)),
    repairsAndMaintenance = Some(Other.Expense(10.25)),
    financialCosts = Some(Other.Expense(10.25)),
    professionalFees = Some(Other.Expense(10.25)),
    costOfServices = Some(Other.Expense(10.25)),
    consolidatedExpenses = None,
    residentialFinancialCost = Some(Other.Expense(10.25)),
    other = Some(Other.Expense(10.25)),
    travelCosts = Some(Other.ExpenseR2(10.25)),
    broughtFwdResidentialFinancialCost = Some(Other.ExpenseR2(10.25)),
    rarReliefClaimed = Some(Other.ExpenseR2(10.25))
  )
  val otherFinancials: Other.Financials = Other.Financials(
    incomes = Some(otherIncomes),
    expenses = Some(otherExpenses)
  )
  val otherProperties: Other.Properties = Other.Properties(
    id = None,
    from = LocalDate.parse("2020-01-28"),
    to = LocalDate.parse("2020-01-29"),
    financials = Some(otherFinancials)
  )

  "PropertiesPeriod" should {
    "read and write" when {
      "passed FHL properties with all fields" in {
        roundTripJson(fhlProperties)
      }
      "passed FHL properties with only incomes" in {
        val model = fhlProperties.copy(
          financials = Some(fhlFinancials.copy(
            expenses = None
          ))
        )
        roundTripJson(model)
      }
      "passed FHL properties with only expenses" in {
        val model = fhlFinancials.copy(incomes = None)
        roundTripJson(model)
      }
      "passed Other properties with all fields" in {
        roundTripJson(otherProperties)
      }
      "passed Other properties with only incomes" in {
        val model = otherProperties.copy(
          financials = Some(otherFinancials.copy(
            expenses = None
          ))
        )
        roundTripJson(model)
      }
      "passed Other properties with only expenses" in {
        val model = otherProperties.copy(
          financials = Some(otherFinancials.copy(
            incomes = None
          ))
        )
        roundTripJson(model)
      }
      "passed Other properties with only residentialFinancialCost in Expenses" in {
        val model = otherProperties.copy(
          financials = Some(otherFinancials.copy(
            incomes = None,
            expenses = Some(Other.Expenses(residentialFinancialCost = Some(Other.Expense(10.25))))
          ))
        )
        roundTripJson(model)
      }
    }
  }

  "FHL Financials" should {
    "read and write" when {
      "passed only rarRentReceived in Incomes" in {
        val model = fhlFinancials.copy(
          incomes = Some(FHL.Incomes(None, Some(IncomeR2(10.25, None))))
        )
        roundTripJson(model)
      }
      "passed only consolidated expenses" in {
        val model = fhlFinancials.copy(
          incomes = None,
          expenses = Some(FHL.Expenses(consolidatedExpenses = Some(FHL.Expense(10.25))))
        )
        roundTripJson(model)
      }
    }
  }

  "Other Financials" should {
    "read and write" when {
      "passed only premiumsOfLeaseGrant in Incomes" in {
        val model = otherFinancials.copy(
          incomes = Some(Other.Incomes(premiumsOfLeaseGrant = Some(Income(10.25, Some(10.25)))))
        )
        roundTripJson(model)
      }
      "passed only reversePremiums in Incomes" in {
        val model = otherFinancials.copy(
          incomes = Some(Other.Incomes(reversePremiums = Some(Income(10.25, Some(10.25)))))
        )
        roundTripJson(model)
      }
      "passed only otherPropertyIncome in Incomes" in {
        val model = otherFinancials.copy(
          incomes = Some(Other.Incomes(otherPropertyIncome = Some(Income(10.25, Some(10.25)))))
        )
        roundTripJson(model)
      }
      "passed only rarRentReceived in Incomes" in {
        val model = otherFinancials.copy(
          incomes = Some(Other.Incomes(rarRentReceived = Some(IncomeR2(10.25, Some(10.25)))))
        )
        roundTripJson(model)
      }
      "passed only expenses" in {
        val model = otherFinancials.copy(
          incomes = None,
          expenses = Some(otherExpenses)
        )
        roundTripJson(model)
      }
      "passed only consolidated expenses" in {
        val model = otherFinancials.copy(
          incomes = None,
          expenses = Some(Other.Expenses(consolidatedExpenses = Some(Other.Expense(10.25))))
        )
        roundTripJson(model)
      }
      "passed only residentialFinancialCost in Expenses" in {
        val model = otherFinancials.copy(
          incomes = None,
          expenses = Some(Other.Expenses(residentialFinancialCost = Some(Other.Expense(10.25))))
        )
        roundTripJson(model)
      }
    }
  }

  "FHL.Properties.asSummary" should {
    "return a PeriodSummary" when {
      "passed a Properties model with no ID" in {
        FHL.Properties(None, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"), None).asSummary shouldBe
          PeriodSummary("", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"))
      }
      "passed a Properties model with an ID" in {
        FHL.Properties(Some("id"), LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"), None).asSummary shouldBe
          PeriodSummary("id", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"))
      }
    }
  }

  "Other.Properties.asSummary" should {
    "return a PeriodSummary" when {
      "passed a Properties model with no ID" in {
        Other.Properties(None, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"), None).asSummary shouldBe
          PeriodSummary("", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"))
      }
      "passed a Properties model with an ID" in {
        Other.Properties(Some("id"), LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"), None).asSummary shouldBe
          PeriodSummary("id", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"))
      }
    }
  }


  "FHL validation" should {
    "return a JsonValidationError with the correct error code" when {
      "passed a `from` date after a `to` date" in {
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(fhlProperties.copy(to = LocalDate.parse("2020-01-26"))),
          pathAndCode = Map("" -> Seq(ErrorCode.INVALID_PERIOD))
        )
      }
      "passed both expenses and consolidatedExpenses" in {
        val model = fhlProperties.copy(
          financials = fhlProperties.financials.map(_.copy(
            expenses = fhlProperties.financials.flatMap(_.expenses.map(_.copy(
              consolidatedExpenses = Some(FHL.Expense(10.25))
            )))
          ))
        )
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.BOTH_EXPENSES_SUPPLIED))
        )
      }
      "passed no financials field" in {
        val model = fhlProperties.copy(financials = None)
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES))
        )
      }
      "passed no incomes or expenses fields" in {
        val model = fhlProperties.copy(financials = Some(FHL.Financials(None, None)))
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES))
        )
      }
    }
    "return multiple JsonValidationErrors" when {
      "passed a `from` date after a `to` date and no financials value" in {
        val model = fhlProperties.copy(to = LocalDate.parse("2020-01-26"), financials = None)
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES, ErrorCode.INVALID_PERIOD))
        )
      }
    }
    "reject IncomeR2 values" when {
      "the IncomeR2 value is less than 0" in {
        val model = fhlProperties.copy(
          financials = fhlProperties.financials.map(_.copy(
            incomes = Some(FHL.Incomes(Some(Income(10.25, Some(10.25))), Some(IncomeR2(-10.25, None))))
          ))
        )
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("/incomes/rarRentReceived/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
      "the IncomeR2 value is greater than 2dp" in {
        val model = fhlProperties.copy(
          financials = fhlProperties.financials.map(_.copy(
            incomes = Some(FHL.Incomes(Some(Income(10.25, Some(10.25))), Some(IncomeR2(10.254, None))))
          ))
        )
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("/incomes/rarRentReceived/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
      "the IncomeR2 value is greater than the max allowed amount" in {
        val model = fhlProperties.copy(
          financials = fhlProperties.financials.map(_.copy(
            incomes = Some(FHL.Incomes(Some(Income(10.25, Some(10.25))), Some(IncomeR2(100000000000.00, None))))
          ))
        )
        assertValidationErrorsWithCode[FHL.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("/incomes/rarRentReceived/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
    }
  }
  "Other validation" should {
    "return a JsonValidationError with the correct error code" when {
      "passed a `from` date after a `to` date" in {
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(otherProperties.copy(to = LocalDate.parse("2020-01-26"))),
          pathAndCode = Map("" -> Seq(ErrorCode.INVALID_PERIOD))
        )
      }
      "passed both expenses and consolidatedExpenses" in {
        val model = otherProperties.copy(
          financials = otherProperties.financials.map(_.copy(
            expenses = otherProperties.financials.flatMap(_.expenses.map(_.copy(
              consolidatedExpenses = Some(Other.Expense(10.25))
            )))
          ))
        )
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.BOTH_EXPENSES_SUPPLIED))
        )
      }
      "passed no financials field" in {
        val model = otherProperties.copy(financials = None)
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES))
        )
      }
      "passed no incomes or expenses fields" in {
        val model = otherProperties.copy(financials = Some(Other.Financials(None, None)))
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES))
        )
      }
    }
    "return multiple JsonValidationErrors" when {
      "passed a `from` date after a `to` date and no financials value" in {
        val model = otherProperties.copy(to = LocalDate.parse("2020-01-26"), financials = None)
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES, ErrorCode.INVALID_PERIOD))
        )
      }
    }
    "reject IncomeR2 values" when {
      "the IncomeR2 value is less than 0" in {
        val model = otherProperties.copy(
          financials = otherProperties.financials.map(_.copy(
            incomes = Some(Other.Incomes(rarRentReceived = Some(IncomeR2(-10.25, None))))
          ))
        )
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("/incomes/rarRentReceived/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
      "the IncomeR2 value is greater than 2dp" in {
        val model = otherProperties.copy(
          financials = otherProperties.financials.map(_.copy(
            incomes = Some(Other.Incomes(rarRentReceived = Some(IncomeR2(10.254, None))))
          ))
        )
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("/incomes/rarRentReceived/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
      "the IncomeR2 value is greater than the max allowed amount" in {
        val model = otherProperties.copy(
          financials = otherProperties.financials.map(_.copy(
            incomes = Some(Other.Incomes(rarRentReceived = Some(IncomeR2(100000000000.00, None))))
          ))
        )
        assertValidationErrorsWithCode[Other.Properties](
          value = Json.toJson(model),
          pathAndCode = Map("/incomes/rarRentReceived/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
    }
  }

}
