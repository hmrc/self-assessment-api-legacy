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
      Array(BigDecimal(1000), BigDecimal(1000.5), BigDecimal(1000.99)).foreach { amount =>
        given().userIsAuthorisedForTheResource(saUtr)
          .when()
          .post(s"/sandbox/$saUtr/self-employments/$selfEmploymentId/incomes", Some(toJson(SelfEmploymentIncome(None, "2016-17", TURNOVER, amount))))
          .thenAssertThat()
          .statusIs(201)
          .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments/$selfEmploymentId/incomes/.+".r)
      }
    }
  }

  "Create self-employment-income with Amount with more than 2 decimal values " should {
    "return a 400 validation error" in {
      Array(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345)).foreach { amount =>
        given().userIsAuthorisedForTheResource(saUtr)
          .when()
          .post(s"/sandbox/$saUtr/self-employments/$selfEmploymentId/incomes", Some(toJson(SelfEmploymentIncome(None, "2016-17", TURNOVER, amount))))
          .thenAssertThat()
          .statusIs(400)
      }
    }
  }
}
