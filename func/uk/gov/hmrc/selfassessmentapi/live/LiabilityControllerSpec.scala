package uk.gov.hmrc.selfassessmentapi.live

import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

// FIXME: Suite aborts if all tests are ignored. Remove constructor to re-enable these tests.
// Temporary fix is to add a constructor, causing the whole class
// to be ignored by ScalaTest.
class LiabilityControllerSpec(ignored: String) extends BaseFunctionalSpec {

  /*override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.self-employments.enabled" -> true,
    "Test.feature-switch.benefits.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
    "Test.feature-switch.employments.enabled" -> true,
    "Test.feature-switch.uk-properties.enabled" -> true))
*/
  "request liability" in {

    "return a 202 response with a link to retrieve the liability" in {
      given()
        .userIsAuthorisedForTheResource(nino)
      .when()
        .post(s"/ni/$nino/liability/$taxYear")
      .thenAssertThat()
        .statusIs(202)
    }
  }

  "retrieve liability" in {

    "return a not found response when a liability has not been requested" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/liability/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return a 200 response when retrieving the result of a request to perform a liability calculation" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(s"/ni/$nino/liability/$taxYear")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/ni/$nino/liability/$taxYear")
        .thenAssertThat()
        .statusIs(200)
    }
  }
}
