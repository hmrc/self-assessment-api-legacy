package uk.gov.hmrc.selfassessmentapi.live

import java.util.UUID

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.{BalancingCharge, BalancingChargeType}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentBalancingChargesSpec extends BaseFunctionalSpec {

  val selfEmploymentId = UUID.randomUUID()
  val balancingChargeId = UUID.randomUUID()

  "create balancing charge" should {
    "not be implemented" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(Some(Json.toJson(BalancingCharge(None, BalancingChargeType.Other, BigDecimal(100.00)))))
        .to(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancingcharges")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "get balancing charge" should {
    "not be implemented" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancingcharges/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "delete balancing charge" should {
    "not be implemented" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancingcharges/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "update balancing charge" should {
    "not be implemented" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(Some(Json.toJson(BalancingCharge(None, BalancingChargeType.Other, BigDecimal(100.00)))))
        .at(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancingcharges/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "get all balancing charges" should {
    "not be implemented" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancingcharges")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

}
