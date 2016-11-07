package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.support.BaseFunctionalSpec

class CustomerResolverControllerSpec extends BaseFunctionalSpec {

  "Live Customer Resolver (customer resolution enabled)" should {
    "return a 200 response with a link to /self-assessment/nino when the customer is enrolled in SA" in {
      given()
        .userIsEnrolledInSa(nino)
      .when()
        .get("/")
      .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self-assessment", s"/self-assessment/nino/$nino")
    }

    "return a 401 response the customer is not enrolled in SA" in {
      given()
        .userIsNotEnrolledInSa
      .when()
        .get("/")
      .thenAssertThat()
        // TODO this error is incorrect against W3C spec, it should be 403
        .statusIs(401)
        // TODO check body for correct format
    }
  }

}
