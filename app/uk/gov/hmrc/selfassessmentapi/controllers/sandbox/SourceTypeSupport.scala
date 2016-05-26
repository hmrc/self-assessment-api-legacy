package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import uk.gov.hmrc.selfassessmentapi.domain.{SourceType, SourceTypes}

trait SourceTypeSupport {

  def sourceHandler(sourceType: SourceType): SourceHandler[_] = sourceType match {
    case SourceTypes.SelfEmployments => SelfEmploymentSourceHandler
    case SourceTypes.FurnishedHolidayLettings => FurnishedHolidayLettingsSourceHandler
    case SourceTypes.UKProperty => UKPropertySourceHandler
  }

}
