package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class TaxYearValidationSpec extends BaseFunctionalSpec {

  "if the tax year in the path is valid for a sandbox request, they" should {
    "receive 200" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat().statusIs(200)
    }
  }

  "if the tax year is invalid for a sandbox request, they" should {
    "receive 400" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .statusIs(400)
        .body(_ \ "code").is("TAX_YEAR_INVALID")
        .body(_ \ "message").is("The provided Tax Year is invalid")
    }
  }

  "if the tax year in the path is valid for a live request, they" should {
    "receive 200" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat().statusIs(200)
    }
  }

  "if the tax year is invalid for a live request, they" should {
    "receive 400" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .statusIs(400)
        .body(_ \ "code").is("TAX_YEAR_INVALID")
        .body(_ \ "message").is("The provided Tax Year is invalid")
    }
  }

}
