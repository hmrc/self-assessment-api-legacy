package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.self-employments.enabled" -> true,
    "Test.feature-switch.benefits.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
    "Test.feature-switch.employments.enabled" -> true,
    "Test.feature-switch.uk-properties.enabled" -> true))

  "Sandbox Self assessment tax years discovery" should {
    "return a 200 response with links to tax years" in {
      given()
        .when()
        .get(s"/sandbox/nino/$nino")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/nino/$nino")
        .bodyHasLink(taxYear, s"/self-assessment/nino/$nino/$taxYear")
    }
  }

  "Sandbox Self assessment tax year discovery" should {
    "return a 200 response with links to self-assessment" in {
      when()
        .get(s"/sandbox/nino/$nino/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/nino/$nino/$taxYear")
        .bodyHasLink("liability", s"/self-assessment/nino/$nino/$taxYear/liability")
        .bodyHasLinksForAllSourceTypes(nino, taxYear)
    }
  }

}
