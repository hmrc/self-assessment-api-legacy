package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {
    "return a 202 response with a link to retrieve the liability" in {
      when()
        .post(s"/sandbox/nino/$nino/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/nino/$nino/$taxYear/liability""".r)
    }
  }

  "retrieve liability" should {
    "return a 200 response" in {
      when()
        .get(s"/sandbox/nino/$nino/$taxYear/liability")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/nino/$nino/$taxYear/liability""".r)
    }

  }

}
