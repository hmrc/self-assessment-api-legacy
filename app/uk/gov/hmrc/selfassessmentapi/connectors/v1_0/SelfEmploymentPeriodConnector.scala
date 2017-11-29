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

package uk.gov.hmrc.selfassessmentapi.connectors.v1_0

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.v1_0.{SelfEmploymentPeriod, SelfEmploymentPeriodUpdate}
import uk.gov.hmrc.selfassessmentapi.models.{SourceId, des}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.v1_0.SelfEmploymentPeriodResponse
import uk.gov.hmrc.selfassessmentapi.connectors._

import scala.concurrent.{ExecutionContext, Future}

object SelfEmploymentPeriodConnector {

  private lazy val baseUrl: String = AppContext.desUrl

  def create(nino: Nino, id: SourceId, selfEmploymentPeriod: SelfEmploymentPeriod)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SelfEmploymentPeriodResponse] =
    httpPost[des.selfemployment.v1_0.SelfEmploymentPeriod, SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries",
      des.selfemployment.v1_0.SelfEmploymentPeriod.from(selfEmploymentPeriod),
      SelfEmploymentPeriodResponse)

  def get(nino: Nino, id: SourceId, from: LocalDate, to: LocalDate)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SelfEmploymentPeriodResponse] =
    httpGet[SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summary-detail?from=$from&to=$to",
      SelfEmploymentPeriodResponse)

  def getAll(nino: Nino, id: SourceId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SelfEmploymentPeriodResponse] =
    httpGet[SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries",
      SelfEmploymentPeriodResponse)

  def update(nino: Nino, id: SourceId, from: LocalDate, to: LocalDate, update: SelfEmploymentPeriodUpdate)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SelfEmploymentPeriodResponse] =
    httpPut[des.selfemployment.v1_0.Financials, SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries?from=$from&to=$to",
      des.selfemployment.v1_0.Financials.from(update),
      SelfEmploymentPeriodResponse)

}
