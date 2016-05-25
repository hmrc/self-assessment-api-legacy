package uk.gov.hmrc.selfassessmentapi.sandbox

import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  "Sandbox Self assessment tax years discovery" should {
    "return a 200 response with links to tax years" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr")
        .bodyHasLink(taxYear, s"/self-assessment/$saUtr/$taxYear")
    }
  }

  "Sandbox Self assessment tax year discovery" should {
    "return a 200 response with links to self-assessment" in {
      val assertions = given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear")
        .thenAssertThat()

      assertions.statusIs(200)
      assertions.contentTypeIsHalJson()
      assertions.bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear")
      assertions.bodyHasLink("liabilities", s"/self-assessment/$saUtr/$taxYear/liabilities")

        SourceTypes.types.foreach { sourceType =>
          assertions.bodyHasLink(sourceType.name, s"/self-assessment/$saUtr/$taxYear/${sourceType.name}")
        }
    }
  }

}
