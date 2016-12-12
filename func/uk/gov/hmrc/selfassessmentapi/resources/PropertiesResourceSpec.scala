package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.resources.models.PeriodId
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{Adjustments, _}
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  implicit def annSummary2Json(summary: PropertiesAnnualSummary): JsValue = Json.toJson(summary)

  "create" should {
    "return code 201 containing a location header when creating a uk property business" in {
      val otherProperty = Jsons.property("CASH")
      val fhlProperty = Jsons.property("CASH")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(otherProperty).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 409 when attempting to create the same property business more than once" in {
      val property = Jsons.property("CASH")

      val expectedJson =
        s"""
           |{
           |  "code": "BUSINESS_ERROR",
           |  "message": "Business validation error",
           |  "errors": [
           |    {
           |      "code": "ALREADY_EXISTS",
           |      "message": "A property business already exists"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(expectedJson)
    }
  }

  "createPeriod" should {
    "return code 201 containing a location header when creating a uk property period" in {
      val property = Jsons.property("CASH")
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
        rentIncome = 50.55, premiumsOfLeaseGrant = 20.22, reversePremiums = 100.25,
        premisesRunningCosts = (50.55, 10.12), otherCost = (10.22, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties/other/periods/\\w+".r)
    }

    "return code 400 when provided with an invalid uk property period" in {
      val property = Jsons.property("CASH")
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
        rentIncome = -50.55, premiumsOfLeaseGrant = 20.22, reversePremiums = 100.25,
        premisesRunningCosts = (50.55, 10.12), otherCost = (10.22, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_MONETARY_AMOUNT", "/incomes/rentIncome/amount")))
    }
  }

  "retrievePeriods" should {
    "return code 200 when retrieving all periods associated with a properties business" in {
      val property = Jsons.property("CASH")
      val periodOne = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-05"))
      val periodTwo = Jsons.propertiesPeriod(fromDate = Some("2016-05-06"), toDate = Some("2016-06-05"))

      val expectedBody =
        s"""
           |[
           |  {
           |    "from": "2016-04-06",
           |    "to": "2016-05-05"
           |  },
           |  {
           |    "from": "2016-05-06",
           |    "to": "2016-06-05"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
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
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period associated with a specific identifier" in {
      val property = Jsons.property("CASH")
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"/ni/$nino/uk-properties/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString())
        .bodyDoesNotHavePath[PeriodId]("id")
    }

    "return code 404 when retrieving a non-existent period with a bad identifier" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period associated with a specific identifier" in {
      val property = Jsons.property("CASH")
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.propertiesPeriod(fromDate = None, toDate = None,
        rentIncome = 50.55, premisesRunningCosts = (50.55, 10.12))

      val updatedPeriod = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"),
        rentIncome = 50.55, premisesRunningCosts = (50.55, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(periodicUpdate).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get("%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(updatedPeriod.toString())
    }

    "return code 400 when provided with an invalid period" in {
      val property = Jsons.property("CASH")
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.propertiesPeriod(fromDate = None, toDate = None,
        rentIncome = -50.55, premisesRunningCosts = (50.55, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(periodicUpdate).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_MONETARY_AMOUNT", "/incomes/rentIncome/amount")))
    }

    "return code 404 when attempting to update a non-existent period" in {
      val property = Jsons.property("CASH")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.propertiesPeriod(rentIncome = 50.55)).at(s"/ni/$nino/uk-properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "create or update annual summary" should {
    "return code 204 if the create/update is successful" in {
      val property = Jsons.property("CASH")

      val annualSummary = PropertiesAnnualSummary(
        Some(Allowances(Some(100), Some(50.50), Some(20.15), Some(10.50))),
        Some(Adjustments(Some(100.50))),
        Some(20.35))

      val expectedJson =
        """
          |{
          |  "allowances": {
          |    "annualInvestmentAllowance": 100,
          |    "businessPremisesRenovationAllowance": 50.50,
          |    "otherCapitalAllowance": 20.15,
          |    "wearAndTearAllowance": 10.50
          |  },
          |  "adjustments": {
          |   "lossBroughtForward": 100.50
          |  },
          |  "rentARoomRelief": 20.35
          |}
        """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummary).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 400 when provided with an invalid annual summary" in {
      val property = Jsons.property("CASH")
      val annualSummary = PropertiesAnnualSummary(
        Some(Allowances(Some(-100), Some(50.50), Some(20.15), Some(10.50))),
        Some(Adjustments(Some(100.50))),
        Some(-20.35))

      val expectedJson =
        """
          |{
          |  "code": "INVALID_REQUEST",
          |  "message": "Invalid request",
          |  "errors": [
          |    {
          |      "code": "INVALID_MONETARY_AMOUNT",
          |      "path": "/allowances/annualInvestmentAllowance",
          |      "message": "amounts should be positive numbers with up to 2 decimal places"
          |    },
          |    {
          |      "code": "INVALID_MONETARY_AMOUNT",
          |      "path": "/rentARoomRelief",
          |      "message": "amounts should be positive numbers with up to 2 decimal places"
          |    }
          |  ]
          |}
        """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummary).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }
  }


  "retrieveAnnualSummary" should {
    "return code 200 when retrieving an annual summary that exists" in {
      val property = Jsons.property("CASH")
      val annualSummaries = PropertiesAnnualSummary(
        Some(Allowances(Some(100), Some(50.50), Some(20.15), Some(10.50))),
        Some(Adjustments(Some(100.50))),
        Some(20.35))

      val expectedJson =
        """
          |{
          |  "allowances": {
          |    "annualInvestmentAllowance": 100,
          |    "businessPremisesRenovationAllowance": 50.50,
          |    "otherCapitalAllowance": 20.15,
          |    "wearAndTearAllowance": 10.50
          |  },
          |  "adjustments": {
          |   "lossBroughtForward": 100.50
          |  },
          |  "rentARoomRelief": 20.35
          |}
        """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
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

    "return code 200 with an empty body when retrieving a non-existent annual summary" in {
      val property = Jsons.property("CASH")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyObject
    }
  }
}
