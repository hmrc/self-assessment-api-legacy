package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class EmptyResponseFilterSpec extends BaseFunctionalSpec {

  "Empty response filter should" should {
    "be applied when returning an HTTP 201 e.g.: creating a self-employment" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("X-Empty-Response", "true".r)
    }

    "be applied when returning an HTTP 409 e.g.: attempting to create a given self-employment more than once" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties())
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Jsons.Properties())
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .responseContainsHeader("X-Empty-Response", "true".r)
    }

  }
}
