package uk.gov.hmrc.selfassessmentapi.live

import org.joda.time.LocalDate
import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = BSONObjectID.generate.stringify

  "Create self-employment" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(toJson(SelfEmployment(None, "name", LocalDate.now.minusDays(1)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find self-employment by id" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Find all self-employments" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Update self-employment" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId", Some(toJson(SelfEmployment(None, "name", LocalDate.now.minusDays(1)))))
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "Delete self-employment" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }
}
