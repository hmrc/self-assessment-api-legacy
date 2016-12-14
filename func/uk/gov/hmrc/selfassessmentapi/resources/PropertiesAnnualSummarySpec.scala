package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesAnnualSummarySpec extends BaseFunctionalSpec {

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
    "return code 200 containing annual summary information" ignore {
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

    "return code 404 when retrieving annual summaries for a properties business that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
