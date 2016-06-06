package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.domain.PensionContribution
import uk.gov.hmrc.support.BaseFunctionalSpec

class TaxYearValidationSpec extends BaseFunctionalSpec {

  "if the tax year in the path is valid for a sandbox request, they" should {
    "receive 200" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat().statusIs(200)
        .bodyHasPath("""pensionContributions \ ukRegisteredPension""", PensionContribution.example().ukRegisteredPension.get)
        .bodyHasPath("""pensionContributions \ retirementAnnuity""", PensionContribution.example().retirementAnnuity.get)
        .bodyHasPath("""pensionContributions \ employerScheme""", PensionContribution.example().employerScheme.get)
        .bodyHasPath("""pensionContributions \ overseasPensions""", PensionContribution.example().overseasPensions.get)
    }
  }

  "if the tax year is invalid for a sandbox request, they" should {
    "receive 400" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .statusIs(400)
        .body(_ \ "message").is("ERROR_TAX_YEAR_INVALID")
    }
  }

  "if the tax year in the path is valid for a live request, they" should {
    "receive 501" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat().statusIs(501)
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
        .body(_ \ "message").is("ERROR_TAX_YEAR_INVALID")
    }
  }

}
