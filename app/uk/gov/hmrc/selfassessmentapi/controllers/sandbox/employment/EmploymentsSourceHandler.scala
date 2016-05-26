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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.employment

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.{SourceHandler, SummaryHandler, employment}
import uk.gov.hmrc.selfassessmentapi.domain.employment.Employment
import uk.gov.hmrc.selfassessmentapi.domain.{SourceTypes, SummaryType, _}

object EmploymentsSourceHandler extends SourceHandler[Employment] {
  override implicit val reads = Employment.reads
  override implicit val writes = Employment.writes
  override def example(id: SourceId) = Employment.example.copy(id = Some(id))
  override val listName = SourceTypes.Employments.name
  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = None
}
