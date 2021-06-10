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
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentStatementConnector
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.utils.EopsObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.services.{AuditService, AuthorisationService}
import uk.gov.hmrc.utils.{IdGenerator, Nino}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class SelfEmploymentStatementResource @Inject()(
                                                 override val appContext: AppContext,
                                                 override val authService: AuthorisationService,
                                                 statementConnector: SelfEmploymentStatementConnector,
                                                 auditService: AuditService,
                                                 cc: ControllerComponents,
                                                 val idGenerator: IdGenerator
                                               ) extends BaseResource(cc) {

  def retrieveObligationsById(nino: Nino, id: SourceId, params: EopsObligationQueryParams): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments, Some("statements")).async(parse.empty) { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = "[SelfEmploymentStatementResource][retrieveObligationsById] " +
        s"with correlationId : $correlationID")

      val selfEmploymentPattern = "^[A-Za-z0-9]{15}$"
      if (id.matches(selfEmploymentPattern)) {
        statementConnector.get(nino, params).map { response =>
          response.filter {
            case 200 =>
              logger.warn(message = "[SelfEmploymentStatementResource][retrieveObligationsById] " +
                s"Success response with status 200 and correlationId : ${correlationId(response)}")
              logger.debug("Self-employment statements from DES = " + Json.stringify(response.json))
              response.retrieveEOPSObligation(id) match {
                case Right(obj) => logger.warn(message = "[SelfEmploymentStatementResource][retrieveObligationsById] " +
                  s"Success response with valid body and correlationId : ${correlationId(response)}")
                  obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
                case Left(ex) =>
                  logger.warn("[SelfEmploymentStatementResource][retrieveObligationsById] " +
                    s"Invalid body response with correlationId : ${correlationId(response)} and message ${ex.msg}")
                  InternalServerError(Json.toJson(Errors.InternalServerError))
              }
            case 400 if response.errorCodeIsOneOf(INVALID_STATUS, INVALID_REGIME, INVALID_IDTYPE) =>
              logger.warn("[SelfEmploymentStatementResource][retrieveObligationsById] " +
                s"Error response from DES with one of the INVALID_STATUS, INVALID_REGIME, INVALID_IDTYPE error codes and correlationId : ${correlationId(response)}")
              InternalServerError(Json.toJson(Errors.InternalServerError))
            case 400 if response.errorCodeIsOneOf(INVALID_DATE_TO, INVALID_DATE_FROM) =>
              logger.warn("[SelfEmploymentStatementResource][retrieveObligationsById] " +
                s"Error response from DES with one of the INVALID_DATE_TO, INVALID_DATE_FROM error codes and correlationId : ${correlationId(response)}")
              BadRequest(Json.toJson(Errors.InvalidDate))
            case 400 if response.errorCodeIsOneOf(INVALID_DATE_RANGE) =>
              logger.warn("[SelfEmploymentStatementResource][retrieveObligationsById] " +
                s"Error response from DES with INVALID_DATE_RANGE error code and correlationId : ${correlationId(response)}")
              BadRequest(Json.toJson(Errors.InvalidDateRange_2))
            case 400 if response.errorCodeIsOneOf(INVALID_IDNUMBER) =>
              logger.warn("[SelfEmploymentStatementResource][retrieveObligationsById] " +
                s"Error response from DES with INVALID_IDNUMBER error code and correlationId : ${correlationId(response)}")
              BadRequest(Json.toJson(Errors.NinoInvalid))
            case 403 if response.errorCodeIs(NOT_FOUND_BPKEY) =>
              logger.warn("[SelfEmploymentStatementResource][retrieveObligationsById] " +
                s"Error response from DES with NOT_FOUND_BPKEY error code and correlationId : ${correlationId(response)}")
              NotFound
          }
        } recoverWith exceptionHandling
      } else {
        Future.successful(BadRequest(Json.toJson(Errors.SelfEmploymentIDInvalid)))
      }
    }
}
