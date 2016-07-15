package uk.gov.hmrc.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify

  "if the user is not authorised for the resource they" should {
    "receive 401" in {
      given()
        .userIsNotAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId")
        .thenAssertThat()
        // TODO - 401 Not Authenticated, should be 403 Forbidden
        .statusIs(401)
        // TODO - check the body (and add whatever code is need to create that body)
    }
  }
}
