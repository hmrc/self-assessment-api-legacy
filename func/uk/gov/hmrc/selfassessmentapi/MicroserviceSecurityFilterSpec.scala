package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class MicroserviceSecurityFilterSpec extends BaseFunctionalSpec {

  "Request for self-employments with X-Test-Scenario = AGENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing Agent should be subscribed to Agent Services" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders("X-Test-Scenario", "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
    }
  }

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
        .withHeaders("X-Test-Scenario", "FOO")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request for dividends with X-Test-Scenario = AGENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing Agent should be subscribed to Agent Services" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .withHeaders("X-Test-Scenario", "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
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
        .withHeaders("X-Test-Scenario", "FOO")
        .thenAssertThat()
        .statusIs(200)
    }
  }

}
