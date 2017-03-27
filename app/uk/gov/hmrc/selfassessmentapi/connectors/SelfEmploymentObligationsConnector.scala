/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.connectors

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.SourceId
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentObligationsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentObligationsConnector {
  private lazy val baseUrl: String = AppContext.desUrl

  private implicit def httpResponse2SeResponse(fut: Future[HttpResponse]): Future[SelfEmploymentObligationsResponse] =
    fut.map(SelfEmploymentObligationsResponse(_))

  def get(nino: Nino, id: SourceId)(implicit hc: HeaderCarrier): Future[SelfEmploymentObligationsResponse] =
    httpGet(baseUrl + s"/ni/$nino/self-employments/$id/obligations")

}
