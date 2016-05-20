package uk.gov.hmrc.selfassessmentapi.live

import java.util.UUID

import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.support.BaseFunctionalSpec

class NotImplementedSummariesControllerSpec extends BaseFunctionalSpec {

  val sourceId = UUID.randomUUID()
  val summaryId = UUID.randomUUID()

  "create summaries" should {
    "not be implemented" in {
     SourceTypes.types.foreach { source =>
       source.summaryTypes.foreach { summary =>
         given()
           .userIsAuthorisedForTheResource(saUtr)
           .when()
           .post(Some(summary.example))
           .to(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}")
           .thenAssertThat()
           .resourceIsNotImplemented()
       }
     }
    }
  }

  "get summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        source.summaryTypes.foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}/$summaryId")
            .thenAssertThat()
            .resourceIsNotImplemented()
        }
      }
    }
  }

  "delete summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        source.summaryTypes.foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .delete(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}/$summaryId")
            .thenAssertThat()
            .resourceIsNotImplemented()
        }
      }
    }
  }

  "update summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        source.summaryTypes.foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .put(Some(summary.example))
            .at(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}/$summaryId")
            .thenAssertThat()
            .resourceIsNotImplemented()
        }
      }
    }
  }

  "get all summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        source.summaryTypes.foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}")
            .thenAssertThat()
            .resourceIsNotImplemented()
        }
      }
    }
  }

}
