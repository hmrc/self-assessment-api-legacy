package uk.gov.hmrc.selfassessmentapi.live

import java.util.UUID

import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentExpenseType.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncomeType._
import uk.gov.hmrc.selfassessmentapi.domain.{BalancingCharge, BalancingChargeType, SelfEmploymentExpense, SelfEmploymentIncome}
import uk.gov.hmrc.support.BaseFunctionalSpec

class NotImplementedSelfEmploymentSummariesSpec extends BaseFunctionalSpec {

  val selfEmploymentId = UUID.randomUUID()
  val balancingChargeId = UUID.randomUUID()

  "create summaries" should {
    "not be implemented" in {
      Map("balancingcharges" -> toJson(BalancingCharge(None, BalancingChargeType.Other, BigDecimal(100.00))),
        "incomes" -> toJson(SelfEmploymentIncome(None, Turnover, BigDecimal(1000))),
        "expenses" -> toJson(SelfEmploymentExpense(None, CISPayments, BigDecimal(1000)))).foreach {
        case (summaryType, summaryJson) =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(Some(summaryJson))
            .to(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/$summaryType")
            .thenAssertThat()
            .resourceIsNotImplemented()
      }
    }
  }

  "get summaries" should {
    "not be implemented" in {
      Seq("balancingcharges", "incomes", "expenses").foreach { summaryType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/$summaryType/1234")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "delete summaries" should {
    "not be implemented" in {
      Seq("balancingcharges", "incomes", "expenses").foreach { summaryType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .delete(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/$summaryType/1234")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "update summaries" should {
    "not be implemented" in {
      Map("balancingcharges" -> toJson(BalancingCharge(None, BalancingChargeType.Other, BigDecimal(100.00))),
        "incomes" -> toJson(SelfEmploymentIncome(None, Turnover, BigDecimal(1000))),
        "expenses" -> toJson(SelfEmploymentExpense(None, CISPayments, BigDecimal(1000)))).foreach {
        case (summaryType, summaryJson) =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .put(Some(summaryJson))
            .at(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/$summaryType/1234")
            .thenAssertThat()
            .resourceIsNotImplemented()
      }
    }
  }

  "get all summaries" should {
    "not be implemented" in {
      Seq("balancingcharges", "incomes", "expenses").foreach { summaryType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId/$summaryType")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }

    }
  }

}
