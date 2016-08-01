package uk.gov.hmrc.selfassessmentapi.live

import java.util.UUID

import uk.gov.hmrc.selfassessmentapi.domain.{SummaryType, _}
import uk.gov.hmrc.support.BaseFunctionalSpec

class NotImplementedSummariesSpec extends BaseFunctionalSpec {

  private val sourceId = UUID.randomUUID()
  private val summaryId = UUID.randomUUID()

  val notImplementedSummaries =
    Map[SourceType, Set[SummaryType]](
      SourceTypes.UnearnedIncomes -> Set(unearnedincome.SummaryTypes.Benefits)
    ).withDefaultValue(Set[SummaryType]())

  "create summaries" should {
    "not be implemented" in {
     SourceTypes.types.foreach { source: SourceType =>
       notImplementedSummaries(source).foreach { summary =>
         given()
           .userIsAuthorisedForTheResource(saUtr)
           .when()
           .post(Some(summary.example()))
           .to(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}")
           .thenAssertThat()
           .isNotImplemented
       }
     }
    }
  }

  "get summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        notImplementedSummaries(source).foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}/$summaryId")
            .thenAssertThat()
            .isNotImplemented
        }
      }
    }
  }

  "delete summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        notImplementedSummaries(source).foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .delete(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}/$summaryId")
            .thenAssertThat()
            .isNotImplemented
        }
      }
    }
  }

  "update summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        notImplementedSummaries(source).foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .put(Some(summary.example()))
            .at(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}/$summaryId")
            .thenAssertThat()
            .isNotImplemented
        }
      }
    }
  }

  "get all summaries" should {
    "not be implemented" in {
      SourceTypes.types.foreach { source =>
        notImplementedSummaries(source).foreach { summary =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"/$saUtr/$taxYear/${source.name}/$sourceId/${summary.name}")
            .thenAssertThat()
            .isNotImplemented
        }
      }
    }
  }

}
