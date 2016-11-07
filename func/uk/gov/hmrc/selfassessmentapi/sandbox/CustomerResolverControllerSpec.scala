package uk.gov.hmrc.selfassessmentapi.sandbox

import uk.gov.hmrc.support.BaseFunctionalSpec

class CustomerResolverControllerSpec extends BaseFunctionalSpec {

  private val validNinoFormat = "[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\\d{2} ?\\d{2} ?\\d{2} ?[A-D]{1}"

  "Sandbox Customer Resolver" should {
    "return a 200 response with a link to /self-assessment/nino/<nino>" in {
      when()
        .get("/sandbox")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self-assessment", s"""^/self-assessment/nino/$validNinoFormat""".r)
    }
  }

}
