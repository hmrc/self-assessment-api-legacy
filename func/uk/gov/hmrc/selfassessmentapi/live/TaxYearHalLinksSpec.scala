package uk.gov.hmrc.selfassessmentapi.live

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec

class TaxYearHalLinksSpec extends BaseFunctionalSpec {

  private val conf: Map[String, Any] = Map("Test.feature-switch.self-employments.enabled" -> false)

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "Request to discover tax year" should {
    "only have Hal links for enabled sources" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/nino/$nino/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLinksForEnabledSourceTypes(nino, taxYear)
    }
  }

}
