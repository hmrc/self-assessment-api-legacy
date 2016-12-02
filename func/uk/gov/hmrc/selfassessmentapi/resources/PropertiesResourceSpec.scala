package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.resources.models.{Expense, Income}
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{ExpenseType, IncomeType, PropertiesPeriod}
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  implicit def period2Json(period: PropertiesPeriod): JsValue = Json.toJson(period)

  "createPeriod" should {
    "return code 201 containing a location header when creating a uk property period" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2017-04-05"), incomes, expenses, Some(100.25), Some(20.00))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/properties/uk/periods/\\w+".r)
    }

    "return code 400 when provided with an invalid uk property period" in {
      val incomes = Map(IncomeType.RentIncome -> Income(-50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period =  PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-05"), incomes, expenses, Some(100.25), Some(20.00))

      val expectedJson =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/incomes/rentIncome/amount",
           |      "message": "amounts should be positive numbers with up to 2 decimal places"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }
  }

  "retrievePeriods" should {
    "return code 200 when retrieving all periods associated with a properties business" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val periodOne = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-07"), incomes, expenses, Some(100.25), Some(20.00))
      val periodTwo = periodOne.copy(from = LocalDate.parse("2016-04-08"), to = LocalDate.parse("2016-04-09"))

      val expectedBody =
         """
           |[
           |  {
           |    "from": "2016-04-06",
           |    "to": "2016-04-07"
           |  },
           |  {
           |    "from": "2016-04-08",
           |    "to": "2016-04-09"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(periodOne).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "periodId").isLength(2).matches("\\w+".r)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period associated with a specific identifier" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-07"), incomes, expenses, Some(100.25), Some(20.00))

      val expectedJson =
        s"""
           |{
           |  "from": "2016-04-06",
           |  "to": "2016-04-07",
           |  "incomes": {
           |    "rentIncome": { "amount": 50.55 },
           |    "premiumsOfLeaseGrant": { "amount": 20.22 },
           |    "reversePremiums": { "amount": 100.25 }
           |  },
           |  "expenses": {
           |    "premisesRunningCosts": { "amount": 50.55, "disallowableAmount": 10 },
           |    "other": { "amount": 100.22, "disallowableAmount": 10 }
           |  },
           |  "privateUseAdjustment": 100.25,
           |  "balancingCharge": 20
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 404 when retrieving a non-existent period with a bad identifier" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period associated with a specific identifier" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val periodOne = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-07"), incomes, expenses, Some(100.25), Some(20.00))
      val periodTwo = periodOne.copy(expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(25, Some(5)), ExpenseType.Other -> Expense(200, Some(100))), balancingCharge = Some(50))

      val expectedJson =
        s"""
           |{
           |  "from": "2016-04-06",
           |  "to": "2016-04-07",
           |  "incomes": {
           |    "rentIncome": { "amount": 50.55 },
           |    "premiumsOfLeaseGrant": { "amount": 20.22 },
           |    "reversePremiums": { "amount": 100.25 }
           |  },
           |  "expenses": {
           |    "premisesRunningCosts": { "amount": 25, "disallowableAmount": 5 },
           |    "other": { "amount": 200, "disallowableAmount": 100 }
           |  },
           |  "privateUseAdjustment": 100.25,
           |  "balancingCharge": 50
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(periodOne).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(periodTwo).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get("%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 400 when provided with an invalid period" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-07"), incomes, expenses, Some(100.25), Some(20.00))

      val invalidIncomes = Map(IncomeType.RentIncome -> Income(-50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val invalidExpenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val invalidPeriod =  PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-05"), invalidIncomes, invalidExpenses, Some(100.25), Some(20.00))

      val expectedJson =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/incomes/rentIncome/amount",
           |      "message": "amounts should be positive numbers with up to 2 decimal places"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(invalidPeriod).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 404 when attempting to update a non-existent period" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-07"), incomes, expenses, Some(100.25), Some(20.00))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(period).at(s"/ni/$nino/properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
