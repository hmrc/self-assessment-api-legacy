package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.{DateTimeZone, LocalDate}
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.PeriodId
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{Expense => _, Income => _, _}
import uk.gov.hmrc.selfassessmentapi.resources.models.{Expense, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.SelfEmploymentPeriod
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsResourceSpec extends BaseFunctionalSpec {

  "create" should {
    "return code 201 when creating a valid a self-employment resource" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/ni/$nino/self-employments/\\w+".r)
    }

    "return code 400 when attempting to create a self-employment with an invalid commencementDate" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2100-01-01")))

      val expectedBody =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "COMMENCEMENT_DATE_NOT_IN_THE_PAST",
           |      "path": "/commencementDate",
           |      "message": "commencement date should be in the past"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "update" should {
    "return code 204 when successfully updating a self-employment resource" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val selfEmployment2 = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-02")))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Some(selfEmployment2)).at("%sourceLocation%")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when attempting to update a non-existent self-employment resource" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val selfEmployment2 = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-02")))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Some(selfEmployment2)).at(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieve" should {
    "return code 200 when retrieving a self-employment resource that exists" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))

      val expectedBody =
        s"""
           |{
           |  "commencementDate": "2016-01-01"
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
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val selfEmploymentTwo = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-02")))

      val expectedBody =
        s"""
           |[
           |  {
           |    "commencementDate": "2016-01-01"
           |  },
           |  {
           |    "commencementDate": "2016-01-02"
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
    }

    "return code 204 No Content when the user has no self-employment sources" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(204)
    }
  }

  "updateAnnualSummary" should {
    "return code 204 when updating an annual summary for a valid self-employment source" in {
      val selfEmployment = Json.toJson(models.SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val annualSummaries = Json.toJson(models.SelfEmploymentAnnualSummary(Some(Allowances.example), Some(Adjustments.example)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Some(annualSummaries)).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when updating an annual summary for an invalid self-employment source" in {
      val annualSummaries = Json.toJson(models.SelfEmploymentAnnualSummary(Some(Allowances.example), Some(Adjustments.example)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Some(annualSummaries)).at(s"/ni/$nino/self-employments/sillysource/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when updating an annual summary providing an invalid adjustment" in {
      val selfEmployment = Json.toJson(models.SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val invalidAdjustment = Adjustments.example.copy(includedNonTaxableProfits = Some(-100), overlapReliefUsed = Some(-100))
      val invalidAllowances = Allowances.example.copy(capitalAllowanceMainPool = Some(-100))
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
           |      "message": "includedNonTaxableProfits should be non-negative number up to 2 decimal values"
           |    },
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/adjustments/overlapReliefUsed",
           |      "message": "overlapReliefUsed should be non-negative number up to 2 decimal values"
           |    },
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/allowances/capitalAllowanceMainPool",
           |      "message": "capitalAllowanceMainPool should be non-negative number up to 2 decimal values"
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
        .put(Some(annualSummaries)).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "retrieveAnnualSummary" should {
    "return code 200 when retrieving an annual summary that exists" in {
      val selfEmployment = Json.toJson(models.SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val annualSummaries = Json.toJson(models.SelfEmploymentAnnualSummary(Some(Allowances.example), Some(Adjustments.example)))
      val expectedJson = Json.toJson(annualSummaries).toString()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Some(annualSummaries)).at(s"%sourceLocation%/$taxYear")
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
      val selfEmployment = Json.toJson(models.SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))

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

    "return code 404 when retrieving an annual summary for a non-existent self-employment source" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments/sillyid/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "createPeriod" should {
    "return code 201 containing a location header when creating a period" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val incomes = Map(IncomeType.Turnover -> BigDecimal(50.55), IncomeType.Other -> BigDecimal(20.22))
      val expenses = Map(ExpenseType.BadDebt -> Expense(50.55, 10), ExpenseType.CoGBought -> Expense(100.22, 10))
      val balancingCharges = Map(BalancingChargeType.BPRA -> BigDecimal(50.25))
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
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val period = Json.toJson(SelfEmploymentPeriod(LocalDate.now.plusDays(1), LocalDate.now, Map.empty, Map.empty, Map.empty, None))

      val expectedBody =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_PERIOD",
           |      "message": "The period 'from' date should come before the 'to' date."
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

    "return code 409 when attempting to create a period with a duplicate `from` and `to` date" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val period = Json.toJson(SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), Map.empty, Map.empty, Map.empty, None))

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
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(409)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period that exists" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
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
        .put(Some(Json.toJson(updatedPeriod))).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when attempting to update a non-existent period" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), Map.empty, Map.empty, Map.empty, None)
      val updatedPeriod = period.copy(to = period.to.plusDays(5))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Some(Json.toJson(updatedPeriod))).at(s"%sourceLocation%/periods/thereisnoperiodhere")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when attempting to update a period with the 'from' and 'to' dates are in the incorrect order" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
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
           |      "message": "The period 'from' date should come before the 'to' date."
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
        .put(Some(Json.toJson(invalidPeriod))).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period that exists" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
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
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))

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
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))
      val periodOne = SelfEmploymentPeriod(
        LocalDate.now(DateTimeZone.UTC), LocalDate.now(DateTimeZone.UTC).plusDays(1), Map.empty, Map.empty, Map.empty, None)
      val periodTwo = SelfEmploymentPeriod(
        LocalDate.now(DateTimeZone.UTC).minusDays(15), LocalDate.now(DateTimeZone.UTC), Map.empty, Map.empty, Map.empty, None)

      val expectedBody =
        s"""
           |[
           |  {
           |    "from": "${periodTwo.from}",
           |    "to": "${periodTwo.to}"
           |  },
           |  {
           |    "from": "${periodOne.from}",
           |    "to": "${periodOne.to}"
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

    "return code 204 when retrieving all periods where periods.size == 0" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(204)
    }
  }
}
