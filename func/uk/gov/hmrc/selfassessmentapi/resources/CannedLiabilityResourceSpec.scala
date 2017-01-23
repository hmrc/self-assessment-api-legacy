package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class CannedLiabilityResourceSpec extends BaseFunctionalSpec {
  "requestLiability" should {
    "return 202 containing a Location header, along with an ETA for the calculation to be ready" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(s"/ni/$nino/liability-calculation")
        .thenAssertThat()
        .statusIs(202)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/liability-calculation/\\w+".r)
        .bodyIsLike(Jsons.CannedLiability.eta(5).toString())
    }
  }

  "retrieveLiability" should {
    "return 200 containing a liability calculation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(s"/ni/$nino/liability-calculation")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.CannedLiability().toString)
    }

    "return 404 when attempting to retrieve a liability using an invalid id" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/liability-calculation/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
