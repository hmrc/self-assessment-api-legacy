package uk.gov.hmrc.selfassessmentapi.sandbox

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()

  "Sandbox Self assessment discovery" should {
    "return a 200 response with links to self-assessment" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr")
    }
  }

}
