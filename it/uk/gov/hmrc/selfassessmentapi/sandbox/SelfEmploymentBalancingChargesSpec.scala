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
  }

  "self employment balancing charges" should {
    "not be created for invalid amount" in {
      when()
        .post(Some(Json.toJson(BalancingCharge(None, BalancingChargeCategory.OTHER, BigDecimal(100.123)))))
        .to(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/balancing-charges")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .body1(_ \\ "code").is("INVALID_MONETARY_AMOUNT")
    }
  }

}
