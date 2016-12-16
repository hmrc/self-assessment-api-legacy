package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.models.AnnualSummary

case class FHLPropertiesAnnualSummary(allowances: Option[Allowances],
                                      adjustments: Option[Adjustments]) extends AnnualSummary

object FHLPropertiesAnnualSummary {
  implicit val writes: Writes[FHLPropertiesAnnualSummary] = Json.writes[FHLPropertiesAnnualSummary]

  implicit val reads: Reads[FHLPropertiesAnnualSummary] = (
    (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments]
    ) (FHLPropertiesAnnualSummary.apply _)
}
