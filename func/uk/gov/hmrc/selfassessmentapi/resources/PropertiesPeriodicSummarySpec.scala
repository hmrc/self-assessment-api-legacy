package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesPeriodicSummarySpec extends BaseFunctionalSpec {

  "creating a period" should {
    "return code 201 containing a location header pointing to the newly created property period" in {
      val property = Jsons.Properties()
      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"),
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
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties/periods/\\w+".r)
    }

    "return code 400 when provided with an invalid property period" in {
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

      val expectedJson = Jsons.Errors.invalidRequest("INVALID_DATE" -> "/to",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount")

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
        .bodyIsLike(expectedJson)
    }

    "return code 403 when creating a period that overlaps with another" in {
      val property = Jsons.Properties()
      val periodOne = Jsons.Properties.period(fromDate = Some("2017-04-06"), toDate = Some("2018-04-01"))
      val periodTwo = Jsons.Properties.period(fromDate = Some("2017-04-06"), toDate = Some("2018-04-02"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"/ni/$nino/uk-properties/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"/ni/$nino/uk-properties/periods")
        .thenAssertThat()
        .statusIs(403)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.businessError(("OVERLAPPING_PERIOD", "")))
    }

    "return code 409 when creating a period with a from and to date that already exists" in {
      val property = Jsons.Properties()
      val periodOne = Jsons.Properties.period(fromDate = Some("2017-04-06"), toDate = Some("2018-04-01"))
      val periodTwo = Jsons.Properties.period(fromDate = Some("2017-04-06"), toDate = Some("2018-04-01"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"/ni/$nino/uk-properties/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"/ni/$nino/uk-properties/periods")
        .thenAssertThat()
        .statusIs(409)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties/periods/\\w+".r)
    }
  }

  "retrieving all periods" should {
    "return code 200 with a JSON list of all periods belonging to the property business" in {
      val property = Jsons.Properties()
      val periodOne = Jsons.Properties.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-04-07"),
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
        fromDate = Some("2017-04-08"),
        toDate = Some("2017-04-09"),
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

      val expectedJson = Jsons.Properties.periodSummary(("2017-04-06", "2017-04-07"),
        ("2017-04-08", "2017-04-09"))

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
        .bodyIsLike(expectedJson.toString)
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }

    "return code 200 with an empty JSON array for a property business containing no periods" in {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .jsonBodyIsEmptyArray
    }
  }

  "retrieving a single period" should {
    "return code 200 containing period information for a period that exists" in {
      val property = Jsons.Properties()
      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"),
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
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString)
    }

    "return code 404 for a period that does not exist" in {
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
    "return code 204 when updating a period" in {
      val property = Jsons.Properties()

      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"),
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
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedPeriod).at("%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(updatedPeriod.toString)
    }

    "return code 400 when updating a period with invalid data" in {
      val property = Jsons.Properties()

      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"),
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

      val expectedJson = Jsons.Errors.invalidRequest(
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount")

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
        .put(invalidPeriod).at("%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
    }

    "return code 404 when updating a period that does not exist" in {
      val property = Jsons.Properties()
      val period = Jsons.Properties.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(period).at("%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
