package uk.gov.hmrc.selfassessmentapi.definition

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.definition.{Access, APIStatus, SelfAssessmentApiDefinition}
import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationWhiteListingEnabledSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map("Test.feature-switch.white-list.enabled" -> true,
    "Test.feature-switch.white-list.applicationIds" -> Seq("app-1-code", "app-2-code")))

  "When white-listing is enabled Request to /api/definition" should {
    "return 200 with the white-listing access section with application ids in the json response" in {
      given()
        .when()
        .get("/api/definition").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyHasPath(s""" api \\ versions(0) \\ access \\ type """, "PRIVATE")
        .bodyHasPath(s""" api \\ versions(0) \\ access \\ whitelistedApplicationIds(0) """, "app-1-code")
        .bodyHasPath(s""" api \\ versions(0) \\ access \\ whitelistedApplicationIds(1) """, "app-2-code")
    }
  }

}
