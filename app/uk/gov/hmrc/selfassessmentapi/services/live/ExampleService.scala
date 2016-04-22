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

package uk.gov.hmrc.selfassessmentapi.services.live

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuditConnector
import uk.gov.hmrc.selfassessmentapi.connectors.ExampleBackendConnector
import uk.gov.hmrc.selfassessmentapi.domain.Example
import uk.gov.hmrc.selfassessmentapi.services.BaseExampleService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ExampleService extends BaseExampleService {
  val connector: ExampleBackendConnector

  override def fetchExample(saUtr: SaUtr)(implicit hc: HeaderCarrier): Future[Example] = {

    def auditResponse(): Unit = {
      MicroserviceAuditConnector.sendEvent(
        DataEvent("api-microservice-template", "ServiceResponseSent",
          tags = Map("transactionName" -> "fetchExample"),
          detail = Map("saUtr" -> saUtr.utr)))
    }

    connector.fetchExample(saUtr).map {
      ex => auditResponse()
        ex
    }
  }
}

object ExampleService extends ExampleService {
  override val connector = ExampleBackendConnector
}
