package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.Employment
import uk.gov.hmrc.support.BaseFunctionalSpec

class EmploymentsControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()

  "if the user is authorised for the resource they" should {
    "receive a proper 200 response with body" in {
      val expectedJson = Json.toJson(Employment(s"Employments for utr: $saUtr"))

      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/${saUtr.utr}/employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/hal+json")
        .bodyIs(expectedJson)
    }
  }

}
