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

package uk.gov.hmrc.selfassessmentapi.services

import play.api.libs.json.{Format, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Failure
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.selfassessmentapi.models.audit.{AuditDetail, ExtendedAuditDetail}
import uk.gov.hmrc.utils.Logging

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AuditService @Inject()(auditConnector: AuditConnector) extends Logging {

  def audit[T <: AuditDetail](
      auditData: AuditData[T])(implicit hc: HeaderCarrier, fmt: Format[T], request: Request[_], ec: ExecutionContext): Future[AuditResult] =
    sendEvent(makeEvent(auditData.detail, auditData.transactionName), auditConnector)

  def makeEvent[T <: AuditDetail](detail: T, transactionName: String)(implicit hc: HeaderCarrier,
                                                                      fmt: Format[T],
                                                                      request: Request[_]): ExtendedDataEvent =
    ExtendedDataEvent(
      auditSource = "self-assessment-api",
      auditType = detail.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, request.path),
      detail = Json.toJson(detail),
      generatedAt = Instant.now()
    )

  def sendEvent(event: ExtendedDataEvent, connector: AuditConnector)(implicit ec: ExecutionContext): Future[AuditResult] =
    try {
      connector.sendExtendedEvent(event)
    } catch {
      case NonFatal(ex) =>
        val msg = s"An exception [$ex] occurred in the Audit service while sending event [$event]"
        logger.warn(msg)
        Future.successful(Failure(msg, Some(ex)))
    }


  def extendedAudit[T <: ExtendedAuditDetail](extendedAuditData: ExtendedAuditData[T])
                                             (implicit hc: HeaderCarrier,
                                              fmt: Format[T],
                                              request: Request[_],
                                              ec: ExecutionContext): Future[AuditResult] = {

    val event = ExtendedDataEvent(
      auditSource = "self-assessment-api",
      auditType = extendedAuditData.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(extendedAuditData.transactionName, request.path),
      detail = Json.toJson(extendedAuditData.detail),
      generatedAt = Instant.now()
    )

    auditConnector.sendExtendedEvent(event)
  }
}


case class AuditData[T <: AuditDetail](detail: T, transactionName: String)

case class ExtendedAuditData[T <: ExtendedAuditDetail](detail: T, transactionName: String, auditType: String)
