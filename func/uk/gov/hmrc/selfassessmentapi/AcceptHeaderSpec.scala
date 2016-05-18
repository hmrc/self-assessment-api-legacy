package uk.gov.hmrc.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.support.BaseFunctionalSpec

class AcceptHeaderSpec extends BaseFunctionalSpec {
  val selfEmploymentId = BSONObjectID.generate.stringify

  "if the valid content type header is sent in the request, they" should {
    "receive 200" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId").withAcceptHeader()
        .thenAssertThat().statusIs(200)
    }
  }

  "if the valid content type header is missing in the request, they" should {
    "receive 406" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(406)
        .body(_ \ "code").is("ACCEPT_HEADER_INVALID")
        .body(_ \ "message").is("The accept header is missing or invalid")
    }
  }

}
