/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.obligations.ObligationsQueryParams
import uk.gov.hmrc.selfassessmentapi.resources.utils.EopsObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentStatementResponse
import uk.gov.hmrc.utils.Nino

import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentStatementConnector @Inject()(
                                                  override val http: DefaultHttpClient,
                                                  override val appContext: AppContext
                                                ) extends BaseConnector {
  private lazy val baseUrl = appContext.desUrl

  def get(nino: Nino, params: EopsObligationQueryParams)
         (implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[SelfEmploymentStatementResponse] = {
    val queryString = (params.from, params.to) match {
      case (None, None) => s"?from=${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"
      case (Some(f), Some(t)) => s"?from=$f&to=$t"
      case (Some(f), None) => s"?from=$f"
      case (None, Some(t)) => s"?to=$t"
    }
    httpGet[SelfEmploymentStatementResponse](baseUrl + s"/enterprise/obligation-data/nino/${nino.nino}/ITSA$queryString", SelfEmploymentStatementResponse)
  }
}
