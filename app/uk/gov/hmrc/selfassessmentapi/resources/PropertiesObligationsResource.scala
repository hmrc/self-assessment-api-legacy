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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.utils.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.ObligationsConnector
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.Audit.makeObligationsRetrievalAudit
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.services.AuditService
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService
import uk.gov.hmrc.utils.IdGenerator

import scala.concurrent.ExecutionContext


class PropertiesObligationsResource @Inject()(
                                               override val appContext: AppContext,
                                               override val authService: AuthorisationService,
                                               connector: ObligationsConnector,
                                               auditService: AuditService,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator
                                             )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  private val incomeSourceType = "ITSP"

  def retrieveObligations(nino: Nino, params: ObligationQueryParams): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("obligations")).async { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[PropertiesObligationsResource][retrieveObligations] " +
        s"retrieve property obligations with correlationId : $correlationID")

      connector.get(nino, incomeSourceType, Some(params)).map { response =>
        auditService.audit(makeObligationsRetrievalAudit(nino, None, request.authContext, response, UkPropertiesRetrieveObligations))
        response.filter {
          case 200 =>
            logger.info("Properties obligations from DES = " + Json.stringify(response.json))
            response.obligations(incomeSourceType) match {
              case Right(obj) => logger.warn(message = s"[PropertiesObligationsResource][retrieveObligations] " +
                s"Success response with correlationId : $correlationID")
                obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
              case Left(ex) =>
                logger.warn(s"[PropertiesObligationsResource][retrieveObligations] " +
                  s"Error response with correlationId : $correlationID and message \n : ${ex.msg}")
                InternalServerError(Json.toJson(Errors.InternalServerError))
            }
        }
      } recoverWith exceptionHandling
    }
}
