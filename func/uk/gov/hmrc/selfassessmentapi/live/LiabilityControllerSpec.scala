package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.selfassessmentapi.domain
import uk.gov.hmrc.selfassessmentapi.domain.TaxYearProperties
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.employment.UkTaxPaid
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SourceType.UKProperties
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SourceType.UnearnedIncomes
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.{Dividend, SavingsIncome}
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {

    "return a 202 response with a link to retrieve the liability" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/liability")
      .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liability""".r)
    }
  }

  "retrieve liability" should {

    "return a not found response when a liability has not been requested" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .isNotFound
    }

    "return a 200 response with liability details" in {

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 60000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 10000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 80000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 6000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments", Some(Employments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments", Some(Employments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 3000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes", Some(UnearnedIncomes.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/savings", Some(toJson(SavingsIncome.example().copy(amount = 800))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/savings", Some(toJson(SavingsIncome.example().copy(amount = 1600))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/dividends", Some(toJson(Dividend.example().copy(amount = 1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/dividends", Some(toJson(Dividend.example().copy(amount = 2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/uk-properties", Some(UKProperties.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/uk-properties/%sourceId%/incomes", Some(toJson(domain.ukproperty.Income.example().copy(amount = 15000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/uk-properties/%sourceId%/taxes-paid", Some(toJson(domain.ukproperty.TaxPaid.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear", Some(toJson(TaxYearProperties.example().copy(charitableGivings = None, blindPerson = None, studentLoan = None, taxRefundedOrSetOff = None, childBenefit = None))))
        .thenAssertThat()
        .statusIs(200)
        .when()
        .post(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(200)
    }

    "return an HTTP 403 response if an error occurred in the liability calculation" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/employments", Some(Employments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = -1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = -2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(403)
    }
  }
}
