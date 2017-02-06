package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.XTestScenarioHeader
import uk.gov.hmrc.support.BaseFunctionalSpec

class TestScenarioHeaderSpec extends BaseFunctionalSpec {
  "Request for self-employments with no X-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request for self-employments with invalid X-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(XTestScenarioHeader, "FOO")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request for dividends with no X-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request for dividends with invalid X-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .withHeaders(XTestScenarioHeader, "FOO")
        .thenAssertThat()
        .statusIs(200)
    }
  }
}
