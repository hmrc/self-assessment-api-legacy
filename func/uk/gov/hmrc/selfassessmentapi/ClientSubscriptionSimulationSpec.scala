package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.XTestScenarioHeader
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode
import uk.gov.hmrc.support.BaseFunctionalSpec

class ClientSubscriptionSimulationSpec extends BaseFunctionalSpec {

  "Request for self-employments with X-Test-Scenario = CLIENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(XTestScenarioHeader, "CLIENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.CLIENT_NOT_SUBSCRIBED.toString)
    }
  }

  "Request for dividends with X-Test-Scenario = CLIENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(s"/ni/$nino/dividends/$taxYear")
        .withHeaders(XTestScenarioHeader, "CLIENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.CLIENT_NOT_SUBSCRIBED.toString)
    }
  }
}