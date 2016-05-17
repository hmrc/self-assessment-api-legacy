package uk.gov.hmrc.selfassessmentapi.sandbox

import java.util.UUID

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.{BalancingCharge, BalancingChargeCategory}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentBalancingChargesSpec extends BaseFunctionalSpec {

  val selfEmploymentId = UUID.randomUUID()
  val balancingChargeId = UUID.randomUUID()

  "self employment balancing charges" should {

    "be created for valid input" in {
      when()
        .post(Some(Json.toJson(BalancingCharge(None, BalancingChargeCategory.OTHER, BigDecimal(100.00)))))
        .to(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges")
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/.+".r)
    }

    "not be created for invalid amount" in {
      when()
        .post(Some(Json.toJson(BalancingCharge(None, BalancingChargeCategory.OTHER, BigDecimal(100.123)))))
        .to(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .body1(_ \\ "code").is("INVALID_MONETARY_AMOUNT")
    }

    "be available for an existing balancing charge id" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/$balancingChargeId")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/$balancingChargeId")
        .body(_ \ "id").is(s"$balancingChargeId")
        .body(_ \ "category").is("OTHER")
        .body(_ \ "amount").is(1000.45)
    }

    "not be available for a non-existent balancing charge id" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/9999")
        .thenAssertThat()
        .statusIs(404)
    }

    "be deleted for valid balancing charge id" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/$balancingChargeId")
        .thenAssertThat()
        .statusIs(204)
    }

    "be not deleted for valid balancing charge id" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/9999")
        .thenAssertThat()
        .statusIs(404)
    }

    "be updated for valid input" in {
      when()
        .put(Some(Json.toJson(BalancingCharge(None, BalancingChargeCategory.OTHER, BigDecimal(100.00)))))
        .at(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/$balancingChargeId")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/$balancingChargeId")
    }

    "not be updated for invalid input" in {
      when()
        .put(Some(Json.toJson(BalancingCharge(None, BalancingChargeCategory.OTHER, BigDecimal(-100.00)))))
        .at(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/$balancingChargeId")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .body1(_ \\ "code").is("INVALID_MONETARY_AMOUNT")
    }

    "not be updated for non-existent balancing charge id" in {
      when()
        .put(Some(Json.toJson(BalancingCharge(None, BalancingChargeCategory.OTHER, BigDecimal(100.00)))))
        .at(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/9999")
        .thenAssertThat()
        .statusIs(404)
    }

    "return a valid response when retrieving list of self employments" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges")
        .bodyHasPath("""_embedded \ balancing-charges(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/1234")
        .bodyHasPath("""_embedded \ balancing-charges(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges/5678")
    }

  }

}
