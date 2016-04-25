package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.Employment
import uk.gov.hmrc.support.BaseFunctionalSpec

class CustomerResolverControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()

  "Customer Resolver" should {
    "return a 200 response with a link to /self-assessment/utr when the customer is enrolled in SA" in {
      val expectedJson = Json.toJson(Employment(s"Employments for utr: $saUtr"))
      given().userIsEnrolledInSa(saUtr)
        .when()
        .get("/")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/hal+json")
        .bodyHasLink("self-assessment", s"/self-assessment/$saUtr")
    }

    "return a 401 response the customer is not enrolled in SA" in {
      val expectedJson = Json.toJson(Employment(s"Employments for utr: $saUtr"))
      given().userIsNotEnrolledInSa
        .when()
        .get("/")
        .thenAssertThat()
        .statusIs(401)
    }
  }

}
