package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.self-employments.enabled" -> true,
    "Test.feature-switch.unearned-incomes.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
    "Test.feature-switch.employments.enabled" -> true,
    "Test.feature-switch.uk-properties.enabled" -> true))

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
      when()
        .get(s"/sandbox/$saUtr/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear")
        .bodyHasLink("liability", s"/self-assessment/$saUtr/$taxYear/liability")
        .bodyHasLinksForAllSourceTypes(saUtr, taxYear)
    }
  }

}
