package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()

  "Live Self assessment discovery" should {
    "return a 200 response with a links when the customer is authorised" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/hal+json")
        .bodyHasLink("self", s"/self-assessment/$saUtr")
    }

    "return a 401 response the customer is not authorised" in {
      given().userIsNotAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr")
        .thenAssertThat()
        .statusIs(401)
    }
  }

}
