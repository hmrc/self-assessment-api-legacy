package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  "creating a property business" should {
    "return code 201 containing a location header when creating a uk property business" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Properties().toString())
    }

    "return code 409 when attempting to create the same property business more than once" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .bodyIsLike(Jsons.Errors.businessError("ALREADY_EXISTS" -> ""))
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 400 when attempting to create a property business with invalid information" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties("OOPS")).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest("INVALID_VALUE" -> "/accountingType"))
    }
  }

  "amending a property business" should {
    "return code 204 when updating property business information" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.Properties("ACCRUAL")).at(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Properties("ACCRUAL").toString())
    }

    "return code 400 when updating a property business with invalid information" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.Properties("OOPS")).at(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest("INVALID_VALUE" -> "/accountingType").toString)
    }

    "return code 404 when updating a property business that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Jsons.Properties()).at(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieving a property business" should {
    "return code 404 when accessing a property business which doesn't exists" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }
  }




}
