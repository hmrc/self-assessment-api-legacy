package uk.gov.hmrc.selfassessmentapi.domain

import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.resources.models.{AnnualSummary, PeriodId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary, PropertiesPeriod}

trait PropertiesBucket extends PeriodValidator[PropertiesPeriod] {
  val periods: Map[PeriodId, PropertiesPeriod]
  val annualSummaries: Map[TaxYear, AnnualSummary]
}

case class FHLPropertiesBucket(periods: Map[PeriodId, PropertiesPeriod],
                               annualSummaries: Map[TaxYear, FHLPropertiesAnnualSummary]) extends PropertiesBucket

case class OtherPropertiesBucket(periods: Map[PeriodId, PropertiesPeriod],
                                 annualSummaries: Map[TaxYear, OtherPropertiesAnnualSummary]) extends PropertiesBucket

object FHLPropertiesBucket {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.annualSummaryFHLMapFormat

  implicit val reads: Reads[FHLPropertiesBucket] = Json.reads[FHLPropertiesBucket]
  implicit val writes: Writes[FHLPropertiesBucket] = Json.writes[FHLPropertiesBucket]
}

object OtherPropertiesBucket {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.annualSummaryOtherMapFormat

  implicit val reads: Reads[OtherPropertiesBucket] = Json.reads[OtherPropertiesBucket]
  implicit val writes: Writes[OtherPropertiesBucket] = Json.writes[OtherPropertiesBucket]
}