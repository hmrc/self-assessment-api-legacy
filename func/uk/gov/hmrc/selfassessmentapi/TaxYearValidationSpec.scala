package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

// FIXME: Refactor into live and sandbox tests

class TaxYearValidationSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map("Test.feature-switch.pensionContributions.enabled" -> true,
                                                                                         "Test.feature-switch.charitableGivings.enabled" -> true,
                                                                                         "Test.feature-switch.blindPerson.enabled" -> true,
                                                                                         "Test.feature-switch.studentLoan.enabled" -> true,
                                                                                         "Test.feature-switch.taxRefundedOrSetOff.enabled" -> true,
                                                                                         "Test.feature-switch.childBenefit.enabled" -> true))

  "the payload is invalid for a live request, they" should {
    "receive 400 if the dateBenefitStopped is before the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           | }
        """.stripMargin)
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(s"/nino/$nino/$taxYear", Some(payload))
        .thenAssertThat()
        .isValidationError("/taxYearProperties/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }

  "the payload is invalid for a live request, they" should {
    "receive 400 if the dateBenefitStopped is after the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2017-04-06"
           |  }
           | }
        """.stripMargin)
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(s"/nino/$nino/$taxYear", Some(payload))
        .thenAssertThat()
        .isValidationError("/taxYearProperties/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }


  "the payload is invalid for a sandbox request, they" should {
    "receive 400 if the dateBenefitStopped is before the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           | }
        """.stripMargin)
      when()
        .put(s"/sandbox/nino/$nino/$taxYear", Some(payload))
        .thenAssertThat()
        .isValidationError("/taxYearProperties/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }

  "the payload is invalid for a sandbox request, they" should {
    "receive 400 if the dateBenefitStopped is after the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2017-04-06"
           |  }
           | }
        """.stripMargin)
      when()
        .put(s"/sandbox/nino/$nino/$taxYear", Some(payload))
        .thenAssertThat()
        .isValidationError("/taxYearProperties/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }

  "if the tax year is invalid for a sandbox request, they" should {
    "receive 400" in {
      when()
        .get(s"/sandbox/nino/$nino/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .isBadRequest("TAX_YEAR_INVALID")
    }
  }

  "if the tax year in the path is valid for a live request, they" should {
    "receive 200" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/nino/$nino/$taxYear").withAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/nino/$nino/$taxYear")
        .bodyHasLink("self-employments", s"""/self-assessment/nino/$nino/$taxYear/self-employments""")
    }
  }

  "if the tax year is invalid for a live request, they" should {
    "receive 400" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/nino/$nino/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .isBadRequest("TAX_YEAR_INVALID")
    }
  }

  "update tax year properties" should {
    "return 400 and validation error if payload does not contain only Pension Contributions for a live request" ignore {
      val payload = Json.parse(
        s"""
           |
           | {
           |   "pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	 },
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2017-04-06"
           |   }
           |}
        """.stripMargin)
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(s"/nino/$nino/$taxYear", Some(payload))
        .thenAssertThat()
        .isValidationError("/taxYearProperties", "ONLY_PENSION_CONTRIBUTIONS_SUPPORTED")
    }
  }


  "if the live request is valid it" should {
    "update and retrieve the pension contributions tax year properties" ignore {

      val payload, expectedJson = Json.parse(
        s"""
           |{
           | 	"pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	}
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(s"/nino/$nino/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
      when()
        .get(s"/nino/$nino/$taxYear").withAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""/self-assessment/nino/$nino/$taxYear""")
        .bodyIs(expectedJson)
    }
  }
}
