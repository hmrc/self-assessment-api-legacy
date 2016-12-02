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

package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.resources.models.{PropertyLocation, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.models.SourceType.SourceType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertiesPeriod
import uk.gov.hmrc.selfassessmentapi.services.{PeriodService, PropertiesService}

object PropertiesResource extends PeriodResource[PropertyLocation, PropertiesPeriod, Properties] with BaseResource {
  override val context: String = AppContext.apiGatewayLinkContext
  override val service: PeriodService[PropertyLocation, PropertiesPeriod, Properties] = PropertiesService()
  override val sourceType: SourceType = SourceType.Properties
}
