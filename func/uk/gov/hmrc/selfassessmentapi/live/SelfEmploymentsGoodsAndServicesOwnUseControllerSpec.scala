package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.GoodsAndServicesOwnUse
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsGoodsAndServicesOwnUseControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify

  "Create self-employment-goods-and-services-for-own-use" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use", Some(toJson(GoodsAndServicesOwnUse(amount = BigDecimal(1000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find self-employment-goods-and-services-for-own-use by Id" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find self-employment-goods-and-services-for-own-use" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Update a self-employment-goods-and-services-for-own-use" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/1234", Some(toJson(GoodsAndServicesOwnUse(amount = BigDecimal(2000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Delete a self-employment-goods-and-services-for-own-use" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }
}
