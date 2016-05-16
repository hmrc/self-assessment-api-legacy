package uk.gov.hmrc.selfassessmentapi.sandbox

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()

  "Sandbox Self assessment tax years discovery" should {
    "return a 200 response with links to tax years" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr")
        .bodyHasLink(taxYear, s"/self-assessment/$saUtr/$taxYear")
    }
  }

  "Sandbox Self assessment tax year discovery" should {
    "return a 200 response with links to self-assessment" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear")
        .bodyHasLink("self-employments", s"/self-assessment/$saUtr/$taxYear/self-employments")
        .bodyHasLink("liabilities", s"/self-assessment/$saUtr/$taxYear/liabilities")
    }
  }

}
