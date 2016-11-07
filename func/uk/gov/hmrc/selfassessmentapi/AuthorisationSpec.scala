package uk.gov.hmrc.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify

  "if the user is not authorised for the resource they" should {
    "receive 401" in {
      given()
        .userIsNotAuthorisedForTheResource(nino)
        .when()
        .get(s"/nino/$nino/$taxYear/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(401)
        .contentTypeIsJson()
        // TODO - check the body (and add whatever code is need to create that body)
    }
  }
}
