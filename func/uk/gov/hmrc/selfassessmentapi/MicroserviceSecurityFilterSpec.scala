package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode
import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.selfassessmentapi.resources.XTestScenarioHeader

class MicroserviceSecurityFilterSpec extends BaseFunctionalSpec {

  "Request for self-employments with X-Test-Scenario = AGENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing Agent should be subscribed to Agent Services" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(XTestScenarioHeader, "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_SUBSCRIBED.toString)
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
        .withHeaders(XTestScenarioHeader, "FOO")
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
        .withHeaders(XTestScenarioHeader, "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_SUBSCRIBED.toString)
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
