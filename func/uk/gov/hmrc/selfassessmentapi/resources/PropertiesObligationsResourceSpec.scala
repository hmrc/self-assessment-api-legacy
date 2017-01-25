package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesObligationsResourceSpec extends BaseFunctionalSpec {
  "retrieveObligations" should {
    "return code 200 containing a set of canned obligations, all of which have not been met" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 200 containing a set of canned obligations of which only the first has been met" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations").withHeaders("X-Request-Scenario", "PROP_OB_FIRST_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations(firstMet = true).toString)
    }

    "return code 200 containing a set of canned obligations, all of which have been met" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations").withHeaders("X-Request-Scenario", "PROP_OB_ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations(firstMet = true, secondMet = true, thirdMet = true, fourthMet = true).toString)
    }

    "return code 404 when attempting to retrieve obligations for a properties business that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
