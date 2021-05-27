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

package uk.gov.hmrc.selfassessmentapi.resources

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.utils.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.ObligationsConnector
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.resources.Audit.makeObligationsRetrievalAudit
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.services.AuditService
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService
import uk.gov.hmrc.utils.IdGenerator

import scala.concurrent.ExecutionContext


class SelfEmploymentObligationsResource @Inject()(
                                                   override val appContext: AppContext,
                                                   override val authService: AuthorisationService,
                                                   obligationsConnector: ObligationsConnector,
                                                   auditService: AuditService,
                                                   cc: ControllerComponents,
                                                   val idGenerator: IdGenerator
                                                 )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  private val incomeSourceType = "ITSB"

  def retrieveObligations(nino: Nino, id: SourceId, params: ObligationQueryParams): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments, Some("obligations")).async(parse.empty) { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[SelfEmploymentObligationsResource][retrieveObligations] " +
        s"with correlationId : $correlationID")

      obligationsConnector.get(nino, incomeSourceType, Some(params)).map { response =>
        auditService.audit(
          makeObligationsRetrievalAudit(nino, Some(id), request.authContext, response, SelfEmploymentRetrieveObligations))
        response.filter {
          case 200 =>
            logger.warn(message = s"[SelfEmploymentObligationsResource][retrieveObligations] " +
              s"Success response status 200 with correlationId : ${correlationId(response)}")
            logger.debug("Self-employment obligations from DES = " + Json.stringify(response.json))
            response.obligations(incomeSourceType, Some(id)) match {
              case Right(obj) => logger.warn(message = s"[SelfEmploymentObligationsResource][retrieveObligations] " +
                s"Success response status 200 and valid body with correlationId : ${correlationId(response)}")
                obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
              case Left(ex) =>
                logger.warn("[SelfEmploymentObligationsResource][retrieveObligations] Invalid body response with " +
                  s"correlationId ${correlationId(response)} and message : ${ex.msg}")
                InternalServerError(Json.toJson(Errors.InternalServerError))
            }
        }
      } recoverWith exceptionHandling
    }
}
