package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "retrieve liability" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "delete liability" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

}
