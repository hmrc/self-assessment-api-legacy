package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.selfassessmentapi.resources.models.PeriodId
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesPeriodicSummarySpec extends BaseFunctionalSpec {

  "createPeriod" should {
    "return code 201 containing a location header when creating a uk property period" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
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

    "return code 400 when provided with an invalid uk property period" ignore {

      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
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
    "return code 200 when retrieving all periods associated with a properties business" ignore {
      val periodOne = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-05"))
      val periodTwo = Jsons.Properties.period(fromDate = Some("2016-05-06"), toDate = Some("2016-06-05"))

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
    "return code 200 when retrieving a period associated with a specific identifier" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

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
    "return code 204 when updating a period associated with a specific identifier" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.Properties.period(fromDate = None, toDate = None,
        rentIncome = 50.55, premisesRunningCosts = (50.55, 10.12))

      val updatedPeriod = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"),
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

    "return code 400 when provided with an invalid period" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.Properties.period(fromDate = None, toDate = None,
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
        .put(Jsons.Properties.period(rentIncome = 50.55)).at(s"/ni/$nino/properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "creating a period" should {
    "return code 201 containing a location header pointing to the newly created property period" ignore {
      val property = Jsons.Properties()
      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("2017-04-02"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = 200.22,
        reversePremiums = 22.35,
        premisesRunningCosts = (20.20, 10),
        repairsAndMaintenance = (11.25, 5.50),
        financialCosts = (100, 25.25),
        professionalFees = (1232.55, 130.55),
        costOfServices = (500.25, 20.50),
        otherCost = (50.22, 27.89))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties/other/periods/w+".r)
    }

    "return code 400 when provided with an invalid property period" ignore {
      val property = Jsons.Properties()
      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("02-04-2017"),
        rentIncome = -500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = -200.22,
        reversePremiums = 22.35,
        premisesRunningCosts = (20.20, 10),
        repairsAndMaintenance = (11.25, 5.50),
        financialCosts = (100, 25.25),
        professionalFees = (1232.55, 130.55),
        costOfServices = (500.25, 20.50),
        otherCost = (50.22, 27.89))

      val expectedJson = Jsons.Errors.invalidRequest("INVALID_DATE" -> "/toDate",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }
  }

  "retrieving all periods" should {
    "return code 200 with a JSON list of all periods belonging to the property business" ignore {
      val property = Jsons.Properties()
      val periodOne = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("2017-04-02"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = 200.22,
        reversePremiums = 22.35,
        premisesRunningCosts = (20.20, 10),
        repairsAndMaintenance = (11.25, 5.50),
        financialCosts = (100, 25.25),
        professionalFees = (1232.55, 130.55),
        costOfServices = (500.25, 20.50),
        otherCost = (50.22, 27.89))

      val periodTwo = Jsons.Properties.period(
        fromDate = Some("2017-04-03"),
        toDate = Some("2017-04-04"),
        rentIncome = 200000,
        rentIncomeTaxDeducted = 2550.55,
        premiumsOfLeaseGrant = 2000.22,
        reversePremiums = 222.35,
        premisesRunningCosts = (200.20, 100),
        repairsAndMaintenance = (110.25, 50.50),
        financialCosts = (1000, 205.25),
        professionalFees = (10232.55, 1300.55),
        costOfServices = (5000.25, 200.50),
        otherCost = (500.22, 270.89))

      val expectedJson = Jsons.Properties.periodSummary(("2017-04-01", "2017-04-02"),
        ("2017-04-03", "2017-04-04"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }
  }

  "retrieving a single period" should {
    "return code 200 containing period information for a period that exists" ignore {
      val property = Jsons.Properties()
      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("2017-04-02"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = 200.22,
        reversePremiums = 22.35,
        premisesRunningCosts = (20.20, 10),
        repairsAndMaintenance = (11.25, 5.50),
        financialCosts = (100, 25.25),
        professionalFees = (1232.55, 130.55),
        costOfServices = (500.25, 20.50),
        otherCost = (50.22, 27.89))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString)
    }

    "return code 404 for a period that does not exist" ignore {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/other/periods/sillyid")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "amending a single period" should {
    "return code 204 when updating a period" ignore {
      val property = Jsons.Properties()

      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("2017-04-02"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = 200.22,
        reversePremiums = 22.35,
        premisesRunningCosts = (20.20, 10),
        repairsAndMaintenance = (11.25, 5.50),
        financialCosts = (100, 25.25),
        professionalFees = (1232.55, 130.55),
        costOfServices = (500.25, 20.50),
        otherCost = (50.22, 27.89))

      val updatedPeriod = Jsons.Properties.period(
        fromDate = None,
        toDate = None,
        rentIncome = 600,
        rentIncomeTaxDeducted = 252.55,
        premiumsOfLeaseGrant = 202.22,
        reversePremiums = 22.37,
        premisesRunningCosts = (20.22, 12),
        repairsAndMaintenance = (111.25, 51.50),
        financialCosts = (160, 25.15),
        professionalFees = (1132.55, 131.55),
        costOfServices = (510.25, 20.10),
        otherCost = (50.12, 17.89))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedPeriod).at("%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 400 when updating a period with invalid data" ignore {
      val property = Jsons.Properties()

      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("2017-04-02"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = 200.22,
        reversePremiums = 22.35,
        premisesRunningCosts = (20.20, 10),
        repairsAndMaintenance = (11.25, 5.50),
        financialCosts = (100, 25.25),
        professionalFees = (1232.55, 130.55),
        costOfServices = (500.25, 20.50),
        otherCost = (50.22, 27.89))

      val invalidPeriod = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("2017-04-02"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = 200.22,
        reversePremiums = 22.35,
        premisesRunningCosts = (20.20, 10),
        repairsAndMaintenance = (11.25, 5.50),
        financialCosts = (100, 25.25),
        professionalFees = (1232.55, 130.55),
        costOfServices = (500.25, 20.50),
        otherCost = (50.22, 27.89))

      val expectedJson = Jsons.Errors.invalidRequest("INVALID_DATE" -> "/toDate",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(invalidPeriod).at("%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
    }

    "return code 404 when updating a period that does not exist" ignore {
      val property = Jsons.Properties()
      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-01"),
        toDate = Some("2017-04-02"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(period).at("%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
