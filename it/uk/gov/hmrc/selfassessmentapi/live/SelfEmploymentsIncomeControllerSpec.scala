package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncome
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsIncomeControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()
  val selfEmploymentId = BSONObjectID.generate.stringify
  val selfEmploymentIncomeId = BSONObjectID.generate.stringify

  "Create self-employment-income" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/self-employments/$selfEmploymentId/incomes", Some(toJson(SelfEmploymentIncome(None, "2016/17", "Turnover", BigDecimal(1000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find self-employment-income by id" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/self-employments/$selfEmploymentId/incomes/$selfEmploymentIncomeId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find all self-employment-incomes" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/self-employments/$selfEmploymentId/incomes")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Update self-employment-income" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/self-employments/$selfEmploymentId/incomes/$selfEmploymentIncomeId", Some(toJson(SelfEmploymentIncome(None, "2016/17", "Other", BigDecimal(2000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Delete self-employment" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/self-employments/$selfEmploymentId/incomes/$selfEmploymentIncomeId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }
}
