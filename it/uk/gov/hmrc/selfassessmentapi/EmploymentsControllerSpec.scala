package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class EmploymentsControllerSpec extends BaseFunctionalSpec {

  "if the user is authorised for the resource they" should {
    "receive a proper 200 response with body" in {
      given().userIsAuthorisedForTheResource("1234")
        .when()
        .get("/1234/employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/json; charset=utf-8")
        .bodyIs("""{"message":"Employments for utr: 1234"}""")
    }
  }

}
