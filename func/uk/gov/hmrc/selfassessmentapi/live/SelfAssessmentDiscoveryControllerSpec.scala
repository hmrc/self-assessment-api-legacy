package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  "Live tax years discovery" should {
    "return a 200 response with links" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr")
        .bodyHasLink(taxYear, s"/self-assessment/$saUtr/$taxYear")
    }
  }

  "Live tax year discovery" should {
    "return a 501 response status" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear")
        .thenAssertThat()
        .statusIs(501)
    }
  }

}
