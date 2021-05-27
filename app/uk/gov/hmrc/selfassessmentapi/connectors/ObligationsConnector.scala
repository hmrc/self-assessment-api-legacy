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
import uk.gov.hmrc.utils.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.ObligationsResponse

import scala.concurrent.{ExecutionContext, Future}


class ObligationsConnector @Inject()(
                                      override val http: DefaultHttpClient,
                                      override val appContext: AppContext
                                    ) extends BaseConnector {

  val baseUrl: String = appContext.desUrl

  def get(nino: Nino, regime: String, params: Option[ObligationQueryParams] = None)
         (implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[ObligationsResponse] = {
    val (from, to) =
      params match {
        case Some(ObligationQueryParams(Some(from), Some(to))) => (from, to)
        case _ => (ObligationQueryParams().from.get, ObligationQueryParams().to.get)
      }
    httpGet[ObligationsResponse](baseUrl + s"/enterprise/obligation-data/nino/$nino/ITSA?from=${from}&to=${to}", ObligationsResponse)
  }

}
