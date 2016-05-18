package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json
import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.GoodsAndServicesOwnUse
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsGoodsAndServicesOwnUseControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify

  "Create self-employment-goods-and-services-for-own-use " should {

    "return a 201 when the resource is created" in {
      when()
        .put(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use", Some(toJson(GoodsAndServicesOwnUse(BigDecimal(1000)))))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
    }

    "return a 400 validation error" in {
      val request = Json.parse(
        """
          | {
          | "amount": 1234.99
          | }
        """.stripMargin)

      when()
        .put(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use", Some(request))
        .thenAssertThat()
        .statusIs(400)
        .body1(_ \\ "code").is("INVALID_MONETARY_AMOUNT_NO_PENCE")
        .body1(_ \\ "path").is("/amount")
    }
  }

  "Find self employment goods and services for own use" should {
    "return valid response" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
    }
  }

  "Modify an existing self employment goods and services for own use" should {
    "return 200 and a valid response when an existing self employment goods and services  is modified" in {
      when()
        .put(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use", Some(toJson(GoodsAndServicesOwnUse(BigDecimal(2000)))))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
    }
  }

  "Deleting a self employment goods and services " should {
    "return 204 if the goods and services exists" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
        .thenAssertThat()
        .statusIs(204)
    }

    "return 404 if the goods and services does not exist" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/self-employments/12345678/goods-and-services-own-use")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}
