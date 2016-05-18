package uk.gov.hmrc.selfassessmentapi.sandbox

import java.util.UUID

import play.api.libs.json.Json
import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentExpense
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentExpenseType._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsExpenseControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = UUID.randomUUID()
  val selfEmploymentIncomeId = UUID.randomUUID()

  "Create self-employment-expense " should {

    "return a 201 when the resource is created" in {
      when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses", Some(toJson(SelfEmploymentExpense(None, CISPayments, BigDecimal(1000)))))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/.+".r)
    }

    "return a 400 validation error" in {


      val request = Json.parse("""
        | {
        | "type" : "Blablabla",
        | "amount": 1234.989
        | }
      """.stripMargin)

      when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses", Some(request))
        .thenAssertThat()
        .statusIs(400)
        .body1(_ \\ "code").is("NO_VALUE_FOUND", "INVALID_MONETARY_AMOUNT")
        .body1(_ \\ "path").is("/type","/amount")
    }
  }

  "Find self employment by id" should {
    "return valid response" in {
      val seExpenseId = "id"
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/$seExpenseId")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/$seExpenseId")
    }
  }

  "Find self employments " should {
    "return multiple expenses in the response" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses")
        .bodyHasPath("""_embedded \ expenses(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/1234")
        .bodyHasPath("""_embedded \ expenses(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/5678")
        .bodyHasPath("""_embedded \ expenses(2) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/4321")
    }
  }

  "Modify an existing self employment expense" should {
    "return 200 and a valid response when an existing self employment expense is modified" in {
      when()
        .put(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/1234", Some(toJson(SelfEmploymentExpense(Some("1234"), CISPayments, BigDecimal(1000)))))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/1234")
    }
  }

  "Deleting a self employment expense" should {
    "return 204" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/self-employments/$selfEmploymentId/expenses/7777")
        .thenAssertThat()
        .statusIs(204)
    }
  }

}
