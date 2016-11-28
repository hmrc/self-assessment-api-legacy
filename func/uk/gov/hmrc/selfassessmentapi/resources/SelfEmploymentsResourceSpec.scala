package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.{DateTimeZone, LocalDate}
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.PeriodId
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{BalancingCharge => _, Expense => _, Income => _, SelfEmployment => _, _}
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.periods._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsResourceSpec extends BaseFunctionalSpec {

  val selfEmployment = SelfEmployment(
    accountingPeriod = AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1)),
    accountingType = AccountingType.CASH,
    commencementDate = LocalDate.now.minusDays(1))

  implicit def selfEmployment2Json(selfEmployment: SelfEmployment) = Json.toJson(selfEmployment)

  "create" should {
    "return code 201 when creating a valid a self-employment resource" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/ni/$nino/self-employments/\\w+".r)
    }

    "return code 400 (MANDATORY_FIELD) when attempting to create a self-employment with an invalid dates in the accountingPeriod" in {
      val selfEmployment =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "01-01-2016",
           |    "end": "02-01-2016"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": "${LocalDate.now.minusDays(1)}"
           |}
         """.stripMargin

      val expectedBody =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_DATE",
           |      "path": "/accountingPeriod/start",
           |      "message": "please provide a date in ISO format (i.e. YYYY-MM-DD)"
           |    },
           |    {
           |      "code": "INVALID_DATE",
           |      "path": "/accountingPeriod/end",
           |      "message": "please provide a date in ISO format (i.e. YYYY-MM-DD)"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Json.parse(selfEmployment)).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "update" should {
    "return code 204 when successfully updating a self-employment resource" in {
      val selfEmployment2 = Json.toJson(selfEmployment.copy(commencementDate = LocalDate.now.minusDays(2)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(selfEmployment2).at("%sourceLocation%")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when attempting to update a non-existent self-employment resource" in {
      val selfEmployment2 = Json.toJson(selfEmployment.copy(commencementDate = LocalDate.now.minusDays(2)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(selfEmployment2).at(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 (INVALID_VALUE) when attempting to update a self-employment with an invalid accounting type" in {
      val updatedSelfEmployment =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "2016-01-01",
           |    "end": "2016-01-02"
           |  },
           |  "accountingType": "NOOOOO",
           |  "commencementDate": "${LocalDate.now.minusDays(1)}"
           |}
         """.stripMargin

      val expectedBody =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_VALUE",
           |      "path": "/accountingType",
           |      "message": "AccountingType should be either CASH or ACCRUAL"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.parse(updatedSelfEmployment)).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "retrieve" should {
    "return code 200 when retrieving a self-employment resource that exists" in {
      val expectedBody =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "${LocalDate.now}",
           |    "end": "${LocalDate.now.plusYears(1)}"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": "${LocalDate.now.minusDays(1)}"
           |}
       """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 404 when retrieving a self-employment resource that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieveAll" should {
    "return code 200 when retrieving self-employments that exist" in {
      val selfEmploymentTwo = Json.toJson(selfEmployment.copy(commencementDate = LocalDate.now.minusDays(2)))

      val expectedBody =
        s"""
           |[
           |  {
           |    "accountingPeriod": {
           |      "start": "${LocalDate.now}",
           |      "end": "${LocalDate.now.plusYears(1)}"
           |    },
           |    "accountingType": "CASH",
           |    "commencementDate": "${LocalDate.now.minusDays(1)}"
           |  },
           |  {
           |    "accountingPeriod": {
           |      "start": "${LocalDate.now}",
           |      "end": "${LocalDate.now.plusYears(1)}"
           |    },
           |    "accountingType": "CASH",
           |    "commencementDate": "${LocalDate.now.minusDays(2)}"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(selfEmploymentTwo).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .body1(_ \\ "id").isLength(2).matches("\\w+".r)
    }

    "return code 200 with an empty body when the user has no self-employment sources" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray
    }
  }

  "updateAnnualSummary" should {
    "return code 204 when updating an annual summary for a valid self-employment source" in {
      val annualSummaries = Json.toJson(models.SelfEmploymentAnnualSummary(Some(SelfEmploymentAllowances.example), Some(SelfEmploymentAdjustments.example)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when updating an annual summary for an invalid self-employment source" in {
      val annualSummaries = Json.toJson(models.SelfEmploymentAnnualSummary(Some(SelfEmploymentAllowances.example), Some(SelfEmploymentAdjustments.example)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/self-employments/sillysource/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when updating an annual summary providing an invalid adjustment & allowance" in {
      val invalidAdjustment = SelfEmploymentAdjustments.example.copy(includedNonTaxableProfits = Some(-100), overlapReliefUsed = Some(-100))
      val invalidAllowances = SelfEmploymentAllowances.example.copy(capitalAllowanceMainPool = Some(-100))
      val annualSummaries = Json.toJson(models.SelfEmploymentAnnualSummary(Some(invalidAllowances), Some(invalidAdjustment)))

      val expectedBody =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/adjustments/includedNonTaxableProfits",
           |      "message": "amounts should be positive numbers with up to 2 decimal places"
           |    },
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/adjustments/overlapReliefUsed",
           |      "message": "amounts should be positive numbers with up to 2 decimal places"
           |    },
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/allowances/capitalAllowanceMainPool",
           |      "message": "amounts should be positive numbers with up to 2 decimal places"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "retrieveAnnualSummary" should {
    "return code 200 when retrieving an annual summary that exists" in {
      val annualSummaries = Json.toJson(models.SelfEmploymentAnnualSummary(Some(SelfEmploymentAllowances.example), Some(SelfEmploymentAdjustments.example)))
      val expectedJson = Json.toJson(annualSummaries).toString()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 404 when retrieving a non-existent annual summary" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "createPeriod" should {
    "return code 201 containing a location header when creating a period" in {
      val incomes = Map(IncomeType.Turnover -> Income(50.55), IncomeType.Other -> Income(20.22))
      val expenses = Map(ExpenseType.BadDebt -> Expense(50.55, Some(10)), ExpenseType.CoGBought -> Expense(100.22, Some(10)))
      val balancingCharges = Map(BalancingChargeType.BPRA -> BalancingCharge(50.25))
      val period = Json.toJson(SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), incomes, expenses, balancingCharges, Some(20.00)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/ni/$nino/self-employments/\\w+/periods/\\w+".r)
    }

    "return code 400 when attempting to create a period with the 'from' and 'to' dates are in the incorrect order" in {
      val period = Json.toJson(SelfEmploymentPeriod(LocalDate.now.plusDays(1), LocalDate.now, Map.empty, Map.empty, Map.empty, None))

      val expectedBody =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_PERIOD",
           |      "message": "the period 'from' date should come before the 'to' date"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 403 when attempting to create a period whose date range overlaps or abuts with a period that already exists" in {
      val periodOne = Json.toJson(SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(10), Map.empty, Map.empty, Map.empty, None))
      val periodTwo = Json.toJson(SelfEmploymentPeriod(LocalDate.now.plusDays(11), LocalDate.now.plusDays(13), Map.empty, Map.empty, Map.empty, None))

      val badPeriod = Json.toJson(SelfEmploymentPeriod(LocalDate.now.plusDays(5), LocalDate.now.plusDays(15), Map.empty, Map.empty, Map.empty, None))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .when()
        .post(badPeriod).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)

    }

    "return code 403 when attempting to create a period that would leave a gap between the latest period and the one provided" in {
      val period = Json.toJson(SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(10), Map.empty, Map.empty, Map.empty, None))
      val badPeriod = Json.toJson(SelfEmploymentPeriod(LocalDate.now.plusDays(12), LocalDate.now.plusDays(13), Map.empty, Map.empty, Map.empty, None))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(badPeriod).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period that exists" in {
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), Map.empty, Map.empty, Map.empty, None)
      val updatedPeriod = period.copy(to = period.to.plusDays(5))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.toJson(period)).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.toJson(updatedPeriod)).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when attempting to update a non-existent period" in {
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), Map.empty, Map.empty, Map.empty, None)
      val updatedPeriod = period.copy(to = period.to.plusDays(5))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.toJson(updatedPeriod)).at(s"%sourceLocation%/periods/thereisnoperiodhere")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when attempting to update a period with the 'from' and 'to' dates are in the incorrect order" in {
      val validPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), Map.empty, Map.empty, Map.empty, None)
      val invalidPeriod = validPeriod.copy(from = validPeriod.to, to = validPeriod.from)

      val expectedBody =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_PERIOD",
           |      "message": "the period 'from' date should come before the 'to' date"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.toJson(validPeriod)).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.toJson(invalidPeriod)).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period that exists" in {
      val fromDate = LocalDate.now(DateTimeZone.UTC)
      val toDate = fromDate.plusDays(1)
      val period = Json.toJson(SelfEmploymentPeriod(fromDate, toDate, Map.empty, Map.empty, Map.empty, None))

      val expectedBody =
        s"""
           |{
           |  "from": "$fromDate",
           |  "to": "$toDate"
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .bodyDoesNotHavePath[PeriodId]("periodId")
    }

    "return code 404 when retrieving a period that does not exist" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods/oops")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieveAllPeriods" should {
    "return code 200 when retrieving all periods where periods.size > 0, sorted by from date" in {
      val periodOne = SelfEmploymentPeriod(
        LocalDate.now(DateTimeZone.UTC), LocalDate.now(DateTimeZone.UTC).plusDays(15), Map.empty, Map.empty, Map.empty, None)
      val periodTwo = SelfEmploymentPeriod(
        LocalDate.now(DateTimeZone.UTC).plusDays(16), LocalDate.now(DateTimeZone.UTC).plusDays(17), Map.empty, Map.empty, Map.empty, None)

      val expectedBody =
        s"""
           |[
           |  {
           |    "from": "${periodOne.from}",
           |    "to": "${periodOne.to}"
           |  },
           |  {
           |    "from": "${periodTwo.from}",
           |    "to": "${periodTwo.to}"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.toJson(periodOne)).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.toJson(periodTwo)).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .body1(_ \\ "periodId").matches("\\w+".r)
    }

    "return code 200 containing an empty json body when retrieving all periods where periods.size == 0" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray
    }
  }
}
