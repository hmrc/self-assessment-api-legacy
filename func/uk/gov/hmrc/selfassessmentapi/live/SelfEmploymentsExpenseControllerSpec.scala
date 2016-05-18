package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentExpenseType._
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentExpense, SelfEmploymentExpenseType}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsExpenseControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify
  val seExpenseId = BSONObjectID.generate.stringify

  "Create self-employment-expense" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses", Some(toJson(SelfEmploymentExpense(None, CISPayments, BigDecimal(1000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find self-employment-expense by id" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/$seExpenseId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find all self-employment-expenses" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Update self-employment-expense" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/$seExpenseId", Some(toJson(SelfEmploymentExpense(None, CISPayments, BigDecimal(2000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Delete self-employment" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/$seExpenseId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }
}
