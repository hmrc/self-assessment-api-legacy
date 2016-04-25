package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class AcceptHeaderSpec extends BaseFunctionalSpec {
  val saUtr = generateSaUtr()

  "if the valid content type header is sent in the request, they" should {
    "receive 200" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/employments").withAcceptHeader()
        .thenAssertThat().statusIs(200)
    }
  }

  "if the valid content type header is missing in the request, they" should {
    "receive 406" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/employments").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(406)
        .body(_ \ "code").is("ACCEPT_HEADER_INVALID")
        .body(_ \ "message").is("The accept header is missing or invalid")
    }
  }

}
