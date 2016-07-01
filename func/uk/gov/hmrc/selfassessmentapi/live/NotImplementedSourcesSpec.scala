package uk.gov.hmrc.selfassessmentapi.live

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.live.SourceController
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes
import uk.gov.hmrc.support.BaseFunctionalSpec

class NotImplementedSourcesSpec extends BaseFunctionalSpec {

  val sourceId = BSONObjectID.generate.stringify

  val notImplementedTypes  = {
    val sourceTypes = SourceTypes.types -- SourceController.supportedSourceTypes
    if (sourceTypes.isEmpty)
      throw new Exception("All source types have been implemented in live, this test can now be deleted")
    else sourceTypes
  }
  
  "Create source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .post(s"/$saUtr/$taxYear/${source.name}", Some(source.example()))
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Find source by id" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${source.name}/$sourceId")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Find all source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${source.name}")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Update source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${source.name}/$sourceId", Some(source.example()))
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Delete source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .delete(s"/$saUtr/$taxYear/${source.name}/$sourceId")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }
}
