package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.models.{Obligation, Obligations}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentObligationsResourceSpec extends BaseFunctionalSpec {
  "retrieveObligations" should {
    "return code 400 when nino is invalid" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get("/ni/abcd1234/self-employments/1/obligations")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 404 when self employment id does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments/1/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 200 containing a set of canned obligations with standard tax year 4 periods, none of which have been met" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/obligations").withHeaders("X-Test-Scenario", "NONE_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 200 containing a set of canned obligations with standard tax year 4 periods, all of which have been met" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/obligations").withHeaders("X-Test-Scenario", "ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations(firstMet = true, secondMet = true, thirdMet = true, fourthMet = true).toString)
    }

    "return code 400 with non standard tax year less than 3 periods" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment(accPeriodStart = "2017-04-01", accPeriodEnd = "2017-09-30")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/obligations")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 200 containing a set of canned obligations with standard tax year 4 periods, 1st of which has been met" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations(firstMet = true).toString)
    }

    "return code 200 containing a set of canned obligations with non standard tax year 3 periods, 1st which have been met" in {
      val expectedObligations = Obligations(
        Seq(
          Obligation(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-06-30"), true),
          Obligation(LocalDate.parse("2017-07-01"), LocalDate.parse("2017-09-30"), false),
          Obligation(LocalDate.parse("2017-10-01"), LocalDate.parse("2017-10-31"), false)
        ).sorted)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment(accPeriodStart = "2017-04-01", accPeriodEnd = "2017-10-31")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(expectedObligations).toString)
    }

    "return code 200 containing a set of canned obligations with non standard tax year 4 periods, 1st which have been met" in {
      val expectedObligations = Obligations(
        Seq(
          Obligation(LocalDate.parse("2017-05-01"), LocalDate.parse("2017-07-31"), true),
          Obligation(LocalDate.parse("2017-08-01"), LocalDate.parse("2017-10-31"), false),
          Obligation(LocalDate.parse("2017-11-01"), LocalDate.parse("2018-01-31"), false),
          Obligation(LocalDate.parse("2018-02-01"), LocalDate.parse("2018-04-30"), false)
        ).sorted)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment(accPeriodStart = "2017-05-01", accPeriodEnd = "2018-04-30")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(expectedObligations).toString)
    }


    "return code 200 containing a set of canned obligations with non standard tax year 5 periods, 1st which have been met" in {
      val expectedObligations = Obligations(
        Seq(
          Obligation(LocalDate.parse("2017-05-01"), LocalDate.parse("2017-07-31"), true),
          Obligation(LocalDate.parse("2017-08-01"), LocalDate.parse("2017-10-31"), false),
          Obligation(LocalDate.parse("2017-11-01"), LocalDate.parse("2018-01-31"), false),
          Obligation(LocalDate.parse("2018-02-01"), LocalDate.parse("2018-04-30"), false),
          Obligation(LocalDate.parse("2018-05-01"), LocalDate.parse("2018-07-31"), false)
        ).sorted)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment(accPeriodStart = "2017-05-01", accPeriodEnd = "2018-07-31")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(expectedObligations).toString)
    }


  }
}
