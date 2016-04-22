package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()

  "if the user is not authorised for the resource they" should {
    "receive 401" in {
      given().userIsNotAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/employments")
        .thenAssertThat().statusIs(401)
    }
  }

  "if the user is authorised for the resource they" should {
    "receive a proper 200 response with body" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/employments")
        .thenAssertThat()
        .statusIs(200)
    }
  }

}
