package uk.gov.hmrc.selfassessmentapi.definition

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.definition.{Access, APIStatus, SelfAssessmentApiDefinition}
import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationWhiteListingEnabledSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map("Test.white-listing.enabled" -> true,
    "Test.white-listing.applicationIds" -> Seq("db4def70-3ae9-4c19-a1b2-70f02d0bd830", "7193670c-f7a3-467d-aac1-b6231beb87a1",
      "6dfac0f3-370b-4306-8f51-d82e5c276faa")))

  "When white-listing is enabled Request to /api/definition" should {
    "return 200 with the white-listing access section with application ids in the json response" in {
      given()
        .when()
        .get("/api/definition").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyHasPath(s""" api \\ versions(0) \\ access \\ type """, "PRIVATE")
        .bodyHasPath(s""" api \\ versions(0) \\ access \\ whitelistedApplicationIds(0) """, "db4def70-3ae9-4c19-a1b2-70f02d0bd830")
        .bodyHasPath(s""" api \\ versions(0) \\ access \\ whitelistedApplicationIds(1) """, "7193670c-f7a3-467d-aac1-b6231beb87a1")
        .bodyHasPath(s""" api \\ versions(0) \\ access \\ whitelistedApplicationIds(2) """, "6dfac0f3-370b-4306-8f51-d82e5c276faa")
    }
  }

}
