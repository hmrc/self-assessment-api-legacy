package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentObligationsResourceSpec extends BaseFunctionalSpec {
  "retrieveObligations" should {
    "return code 400 when nino is invalid" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().invalidNinoFor(nino)
        .when()
        .get("/ni/abcd1234/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 404 when self employment id does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.obligationNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when the user has a non-standard tax year with a duration of less than 3 periods" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.obligationTaxYearTooShort(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidObligation)
    }

    "return code 200 with a set of obligations" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.returnObligationsFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "forward the GovTestScenario header to DES" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.receivesObligationsTestHeader(nino, "ALL_MET")
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations").withHeaders(GovTestScenarioHeader, "ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }
  }
}
