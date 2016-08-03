package uk.gov.hmrc.selfassessmentapi.live

import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

class CustomerResolverControllerAuthDisabledSpec extends BaseFunctionalSpec {

  private val conf: Map[String, Map[String, Any]] =
    Map("auth" -> Map("enabled" -> false))

  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = conf)

  "Live Customer Resolver (customer resolution disabled" should {
    "return a 200 response with a link to /self-assessment/utr with a generated utr" in {
      given()
        .when()
        .get("/")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self-assessment", s"/self-assessment/.*".r)
        .noInteractionsWithExternalSystems()
    }
  }

}
