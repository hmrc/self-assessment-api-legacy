package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.employment.UkTaxPaid
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {

    "return a 202 response with a link to retrieve the liability" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/liability")
      .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liability""".r)
    }
  }

  "retrieve liability" should {

    "return a not found response when a liability has not been requested" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .isNotFound
    }

    "return a 200 response when retrieving the result of a request to perform a liability calculation" in {

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(200)
    }

    "return an HTTP 403 response if an error occurred in the liability calculation" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/employments", Some(Employments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = -1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = -2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(403)
    }
  }
}
