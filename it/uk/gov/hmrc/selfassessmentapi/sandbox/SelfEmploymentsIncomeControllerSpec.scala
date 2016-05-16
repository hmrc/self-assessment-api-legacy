package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncome
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncomeType._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsIncomeControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify
  val selfEmploymentIncomeId = BSONObjectID.generate.stringify

  "Create self-employment-income with Amount with less than or equal to 2 decimal values " should {
    "return a 201 when the resource is created" in {
      given()
        .when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes", Some(toJson(SelfEmploymentIncome(None, taxYear, TURNOVER, BigDecimal(1000.99)))))
        .thenAssertThat()
        .statusIs(201)
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes/.+".r)
    }
  }

  "Create self-employment-income with Amount with more than 2 decimal values" should {
    "return a 400 validation error" in {
      given()
        .when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes", Some(toJson(SelfEmploymentIncome(None, taxYear, TURNOVER, BigDecimal(-1000.12)))))
        .thenAssertThat()
        .statusIs(400)
    }
  }

  "Retrieve an existent self-employment income id" should {
    "return a HAL resource" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes/$selfEmploymentIncomeId")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes/$selfEmploymentIncomeId")
    }
  }

  "Retrieve all self-employment incomes" should {
    "return all incomes as HAL resources" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes")
        .bodyHasPath("""_embedded \ incomes(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes/1234")
        .bodyHasPath("""_embedded \ incomes(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/incomes/5678")
    }
  }

}
