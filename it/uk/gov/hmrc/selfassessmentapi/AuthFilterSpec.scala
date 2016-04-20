package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthFilterSpec extends BaseFunctionalSpec {

  "if the user is not authorised for the resource they" should {
    "receive 401" in {
      given().userIsNotAuthorisedForTheResource("1234")
        .when()
        .get("/1234/employments")
        .thenAssertThat().statusIs(401)
    }
  }

  "if the user is authorised for the resource they" should {
    "receive a proper 200 response with body" in {
      given().userIsAuthorisedForTheResource("1234")
        .when()
        .get("/1234/employments")
        .thenAssertThat()
        .statusIs(200)
        .bodyIs("Hello employments utr: 1234")
    }
  }

}





