package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentSourceHandlerLimitEnabledSpec extends BaseFunctionalSpec {
  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.source-limits.self-employments" -> true))

  "create" should {
    "limit the number of self-employments that can be created to one for each UTR in the live environment" in {

      val selfEmployment =
        s"""
           |{
           |  "commencementDate" : "2016-01-01",
           |  "allowances" : {
           |    "annualInvestmentAllowance" : 1000.0,
           |    "capitalAllowanceMainPool" : 150.0,
           |    "capitalAllowanceSpecialRatePool" : 5000.5,
           |    "restrictedCapitalAllowance" : 400.0,
           |    "businessPremisesRenovationAllowance" : 600.0,
           |    "enhancedCapitalAllowance" : 50.0,
           |    "allowancesOnSales" : 3399.99
           |  },
           |  "adjustments" : {
           |    "includedNonTaxableProfits" : 50.0,
           |    "basisAdjustment" : 20.1,
           |    "overlapReliefUsed" : 500.0,
           |    "accountingAdjustment" : 10.5,
           |    "averagingAdjustment" : -400.99,
           |    "lossBroughtForward" : 10000.0,
           |    "outstandingBusinessIncome" : 50.0
           |  }
           |}
         """.stripMargin

      val nino2 = NinoGenerator().nextNino()

      given()
        .userIsAuthorisedForTheResource(nino)
      when()
        .post(s"/nino/$nino/$taxYear/self-employments", Some(Json.parse(selfEmployment)))
        .thenAssertThat()
        .statusIs(201)
      given()
        .userIsAuthorisedForTheResource(nino)
      .when()
        .post(s"/nino/$nino/$taxYear/self-employments", Some(Json.parse(selfEmployment)))
        .thenAssertThat()
        .statusIs(400)
      given()
        .userIsAuthorisedForTheResource(nino2)
      .when()
        .post(s"/nino/$nino2/$taxYear/self-employments", Some(Json.parse(selfEmployment)))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}

class SelfEmploymentSourceHandlerLimitDisabledSpec extends BaseFunctionalSpec {
  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.source-limits.self-employments" -> false))

  "create" should {
    "prevent the creation of self employment sources for any UTR" in {

      val selfEmployment =
        s"""
           |{
           |  "commencementDate" : "2016-01-01",
           |  "allowances" : {
           |    "annualInvestmentAllowance" : 1000.0,
           |    "capitalAllowanceMainPool" : 150.0,
           |    "capitalAllowanceSpecialRatePool" : 5000.5,
           |    "restrictedCapitalAllowance" : 400.0,
           |    "businessPremisesRenovationAllowance" : 600.0,
           |    "enhancedCapitalAllowance" : 50.0,
           |    "allowancesOnSales" : 3399.99
           |  },
           |  "adjustments" : {
           |    "includedNonTaxableProfits" : 50.0,
           |    "basisAdjustment" : 20.1,
           |    "overlapReliefUsed" : 500.0,
           |    "accountingAdjustment" : 10.5,
           |    "averagingAdjustment" : -400.99,
           |    "lossBroughtForward" : 10000.0,
           |    "outstandingBusinessIncome" : 50.0
           |  }
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
      when()
        .post(s"/nino/$nino/$taxYear/self-employments", Some(Json.parse(selfEmployment)))
        .thenAssertThat()
        .statusIs(201)
      when()
        .post(s"/nino/$nino/$taxYear/self-employments", Some(Json.parse(selfEmployment)))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}
