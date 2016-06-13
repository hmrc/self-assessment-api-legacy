package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.support.BaseFunctionalSpec

class SourceControllerSpec extends BaseFunctionalSpec {
  "Live source controller" should {
    "return a 404 error when source type is invalid" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/blah")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
