package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  "creating a property business" should {
    "return code 201 containing a location header when creating a uk property business" ignore {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 409 when attempting to create the same property business more than once" ignore {
      val property = Jsons.Properties()

      val expectedJson =
        Jsons.Errors.businessError("ALREADY_EXISTS" -> "A property business already exists")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .bodyIsLike(expectedJson)
    }

    "return code 400 when attempting to create a property business with invalid information" ignore {
      val property = Jsons.Properties("OOPS")

      val expectedJson = Jsons.Errors.invalidRequest(("INVALID_VALUE" -> "/accountingType"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(expectedJson.toString)
    }
  }

  "amending a property business" should {
    "return code 204 when updating property business information" ignore {
      val property = Jsons.Properties("CASH")
      val updatedProperty = Jsons.Properties("ACCRUAL")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedProperty).at(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 400 when updating a property business with invalid information" ignore {
      val property = Jsons.Properties("CASH")
      val updatedProperty = Jsons.Properties("OOPS")

      val expectedJson = Jsons.Errors.invalidRequest(("INVALID_VALUE" -> "/accountingType"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedProperty).at(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(expectedJson.toString)
    }

    "return code 404 when updating a property business that does not exist" ignore {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(property).at(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieving a property business" should {
    "return code 200 containing property business information" ignore {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(property.toString())
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

      val expectedJson = Jsons.Errors.invalidRequest(("INVALID_DATE" -> "/toDate"),
        ("INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount"),
        ("INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount"))

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

      val expectedJson = Jsons.Errors.invalidRequest(("INVALID_DATE" -> "/toDate"),
                                                     ("INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount"),
                                                     ("INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount"))

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

  "amending annual summaries" should {
    "return code 204 when amending annual summaries for an arbitrary tax year" ignore {
      val property = Jsons.Properties()

      val annualSummaries = Jsons.Properties.annualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = 500.50,
        otherCapitalAllowance = 1000.20,
        wearAndTearAllowance = 150.55,
        lossBroughtForward = 20.22,
        rentARoomRelief = 50.23,
        privateUseAdjustment = 22.23,
        balancingCharge = 350.34)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 400 when amending annual summaries with invalid data" ignore {
      val property = Jsons.Properties()

      val annualSummaries = Jsons.Properties.annualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = -500.50,
        otherCapitalAllowance = 1000.20,
        wearAndTearAllowance = 150.55,
        lossBroughtForward = 20.22,
        rentARoomRelief = 50.23,
        privateUseAdjustment = -22.23,
        balancingCharge = 350.34)

      val expectedJson = Jsons.Errors.invalidRequest(
        "INVALID_MONETARY_AMOUNT" -> "/allowances/businessPremisesRenovationAllowance",
        "INVALID_MONETARY_AMOUNT" -> "/adjustments/privateUseAdjustment")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
    }

    "return code 404 when amending annual summaries for a properties business that does not exist" ignore {
      val annualSummaries = Jsons.Properties.annualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = -500.50,
        otherCapitalAllowance = 1000.20,
        wearAndTearAllowance = 150.55,
        lossBroughtForward = 20.22,
        rentARoomRelief = 50.23,
        privateUseAdjustment = -22.23,
        balancingCharge = 350.34)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieving annual summaries" should {
    "return code 200 containing annual summary information" in {
      val property = Jsons.Properties()

      val annualSummaries = Jsons.Properties.annualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = 500.50,
        otherCapitalAllowance = 1000.20,
        wearAndTearAllowance = 150.55,
        lossBroughtForward = 20.22,
        rentARoomRelief = 50.23,
        privateUseAdjustment = 22.23,
        balancingCharge = 350.34)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(annualSummaries.toString)
    }

    "return code 404 when retrieving annual summaries for a properties business that does not exist" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
