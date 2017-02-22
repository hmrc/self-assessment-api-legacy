package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.Jsons.CannedCalculation.eta
import uk.gov.hmrc.selfassessmentapi.resources.Jsons.Errors.invalidRequest
import uk.gov.hmrc.support.BaseFunctionalSpec

class CannedCalculationResourceSpec extends BaseFunctionalSpec {
  "requestCalculation" should {

    "return 202 containing a Location header, along with an ETA for the calculation to be ready" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.CannedCalculation.request()).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(202)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/calculations/\\w+".r)
        .bodyIsLike(eta(5).toString())
    }

    "return 400 when attempting to request calculation without specifying a tax year" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Json.obj()).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(invalidRequest("MANDATORY_FIELD_MISSING" -> "/taxYear"))
    }

    "return 400 when attempting to request calculation with invalid tax year" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.CannedCalculation.request("2011-12")).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(invalidRequest("TAX_YEAR_INVALID" -> "/taxYear"))
    }
  }

  "retrieveCalculation" should {
    "return 200 containing a calculation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.CannedCalculation.request()).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.CannedCalculation().toString)
    }

    "return 404 when attempting to retrieve a calculation using an invalid id" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/calculations/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
