/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.controllers.live

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.SourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.employment.EmploymentSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.furnishedholidaylettings.FurnishedHolidayLettingsSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.selfemployment.SelfEmploymentSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.ukproperty.UKPropertySourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.unearnedincome.UnearnedIncomeSourceHandler
import uk.gov.hmrc.selfassessmentapi.domain.SourceType
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes._

trait SourceTypeSupport extends uk.gov.hmrc.selfassessmentapi.controllers.SourceTypeSupport {
  def sourceHandler(sourceType: SourceType): SourceHandler[_] = sourceType match {
    case SelfEmployments => SelfEmploymentSourceHandler
    case UnearnedIncomes => UnearnedIncomeSourceHandler
    case FurnishedHolidayLettings => FurnishedHolidayLettingsSourceHandler
    case Employments => EmploymentSourceHandler
    case UKProperties => UKPropertySourceHandler
    case _ => throw new NotImplementedException(s"${sourceType.name} is not implemented")
  }
}
