package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentExpenseCategory._
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentExpense, SelfEmploymentExpenseCategory}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsExpenseControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify
  val seExpenseId = BSONObjectID.generate.stringify

  "Create self-employment-expense" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/self-employments/$selfEmploymentId/expenses", Some(toJson(SelfEmploymentExpense(None, "2016-17", CISPayments, BigDecimal(1000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find self-employment-expense by id" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/self-employments/$selfEmploymentId/expenses/$seExpenseId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find all self-employment-expenses" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/self-employments/$selfEmploymentId/expenses")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Update self-employment-expense" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/self-employments/$selfEmploymentId/expenses/$seExpenseId", Some(toJson(SelfEmploymentExpense(None, "2016-17", CISPayments, BigDecimal(2000)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Delete self-employment" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/self-employments/$selfEmploymentId/expenses/$seExpenseId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }
}
