package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.resources.models.PeriodId
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{Adjustments, _}
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  implicit def period2Json(period: PropertiesPeriod): JsValue = Json.toJson(period)
  implicit def annSummary2Json(summary: PropertiesAnnualSummary): JsValue = Json.toJson(summary)

  "createPeriod" should {
    "return code 201 containing a location header when creating a uk property period" in {
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
        rentIncome = 50.55, premiumsOfLeaseGrant = 20.22, reversePremiums = 100.25,
        premisesRunningCosts = (50.55, 10.12), otherCost = (10.22, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/properties/uk/periods/\\w+".r)
    }

    "return code 400 when provided with an invalid uk property period" in {

      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
        rentIncome = -50.55, premiumsOfLeaseGrant = 20.22, reversePremiums = 100.25,
        premisesRunningCosts = (50.55, 10.12), otherCost = (10.22, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_MONETARY_AMOUNT", "/incomes/rentIncome/amount")))
    }
  }

  "retrievePeriods" should {
    "return code 200 when retrieving all periods associated with a properties business" in {
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
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period associated with a specific identifier" in {
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

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
        .bodyIsLike(period.toString())
        .bodyDoesNotHavePath[PeriodId]("id")
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
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.propertiesPeriod(fromDate = None, toDate = None,
        rentIncome = 50.55, premisesRunningCosts = (50.55, 10.12))

      val updatedPeriod = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"),
        rentIncome = 50.55, premisesRunningCosts = (50.55, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
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
      val period = Jsons.propertiesPeriod(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.propertiesPeriod(fromDate = None, toDate = None,
        rentIncome = -50.55, premisesRunningCosts = (50.55, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
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

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Jsons.propertiesPeriod(rentIncome = 50.55)).at(s"/ni/$nino/properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "create or update annual summary" should {
    "return code 204 if the create/update is successful" in {
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
        .put(annualSummary).at(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 400 when provided with an invalid annual summary" in {
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
        .put(annualSummary).at(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }
  }


  "retrieveAnnualSummary" should {
    "return code 200 when retrieving an annual summary that exists" in {

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
        .put(annualSummaries).at(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 404 when retrieving a non-existent annual summary" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
