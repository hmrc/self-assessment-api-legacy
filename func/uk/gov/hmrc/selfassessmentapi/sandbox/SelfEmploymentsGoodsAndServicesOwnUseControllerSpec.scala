package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json
import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.GoodsAndServicesOwnUse
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsGoodsAndServicesOwnUseControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify
  val goodsAndServiceOwnUseId = BSONObjectID.generate.stringify

  "Create self-employment-goods-and-services-own-use " should {

    "return a 201 when the resource is created" in {
      when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use", Some(toJson(GoodsAndServicesOwnUse(amount = BigDecimal(1000.75)))))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/.+".r)
    }

    "return a 400 validation error" in {
      val request = Json.parse(
        """
          | {
          | "amount": 1234.999
          | }
        """.stripMargin)

      when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use", Some(request))
        .thenAssertThat()
        .statusIs(400)
        .body1(_ \\ "code").is("INVALID_MONETARY_AMOUNT")
        .body1(_ \\ "path").is("/amount")
    }
  }

  "Find a self-employment-goods-and-services-own-use by Id" should {
    "return valid response" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/$goodsAndServiceOwnUseId")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/.+".r)
    }
  }

  "Find all self-employment-goods-and-services-own-use " should {
    "return multiple expenses in the response" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use")
        .bodyHasPath("""_embedded \ goods-and-services-own-use(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/.+".r)
        .bodyHasPath("""_embedded \ goods-and-services-own-use(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/.+".r)
    }
  }

  "Modify a self-employment-goods-and-services-own-use" should {
    "return 200 and a valid response when an existing self employment goods and services is modified" in {
      when()
        .put(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/$goodsAndServiceOwnUseId", Some(toJson(GoodsAndServicesOwnUse(amount = BigDecimal(2000.99)))))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/.+".r)
    }
  }

  "Deleting a self-employment-goods-and-services-own-use " should {
    "return 204 if the goods and services exists" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/goods-and-services-own-use/$goodsAndServiceOwnUseId")
        .thenAssertThat()
        .statusIs(204)
    }
  }

}
