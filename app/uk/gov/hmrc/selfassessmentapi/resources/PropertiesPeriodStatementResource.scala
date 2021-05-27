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

import cats.implicits._
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.utils.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesPeriodStatementConnector
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.{Declaration, Errors, Period, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.utils.ResourceHelper
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.EmptyResponse
import uk.gov.hmrc.selfassessmentapi.services.{AuditService, AuthorisationService}
import uk.gov.hmrc.utils.IdGenerator

import scala.concurrent.ExecutionContext.Implicits._


class PropertiesPeriodStatementResource @Inject()(
                                                   override val appContext: AppContext,
                                                   override val authService: AuthorisationService,
                                                   statementConnector: PropertiesPeriodStatementConnector,
                                                   auditService: AuditService,
                                                   resourceHelper: ResourceHelper,
                                                   cc: ControllerComponents,
                                                   val idGenerator: IdGenerator
                                                 ) extends BaseResource(cc) {


  def finaliseEndOfPeriodStatement(nino: Nino, start: LocalDate, end: LocalDate): Action[JsValue] =
    APIAction(nino, SourceType.Properties).async(parse.json) { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[PropertiesPeriodStatementResource][finaliseEndOfPeriodStatement] " +
        s"with correlationId : $correlationID")

      val accountingPeriod = Period(start, end)

      {
        for {
          _ <- resourceHelper.validatePeriodDates(accountingPeriod)
          declaration <- validateJson[Declaration](request.body)
          _ <- authorise(declaration) { case _ if (!declaration.finalised) => Errors.NotFinalisedDeclaration }

          desResponse <- execute[EmptyResponse] { _ => statementConnector.create(nino, accountingPeriod, getRequestDateTimestamp) }
        } yield desResponse
      } onDesSuccess { desResponse =>
        auditService.audit(resourceHelper.buildAuditEvent(nino, "", accountingPeriod, request.authContext, desResponse))
        desResponse.filter {
          case 204 => logger.warn(message = s"[PropertiesPeriodStatementResource][finaliseEndOfPeriodStatement] " +
            s"Success response with correlationId : ${correlationId(desResponse)}")
            NoContent
          case 400 if desResponse.errorCodeIs(EARLY_SUBMISSION) => logger.warn(message = s"[PropertiesPeriodStatementResource][finaliseEndOfPeriodStatement] " +
            s"Error response EARLY_SUBMISSION with correlationId : ${correlationId(desResponse)}")
            Forbidden(Errors.businessJsonError(Errors.EarlySubmission))
          case 403 if desResponse.errorCodeIs(PERIODIC_UPDATE_MISSING) => logger.warn(message = s"[PropertiesPeriodStatementResource][finaliseEndOfPeriodStatement] " +
            s"Error response PERIODIC_UPDATE_MISSING with correlationId : ${correlationId(desResponse)}")
            Forbidden(Errors.businessJsonError(Errors.PeriodicUpdateMissing))
          case 403 if desResponse.errorCodeIs(NON_MATCHING_PERIOD) => logger.warn(message = s"[PropertiesPeriodStatementResource][finaliseEndOfPeriodStatement] " +
            s"Error response NON_MATCHING_PERIOD with correlationId : ${correlationId(desResponse)}")
            Forbidden(Errors.businessJsonError(Errors.NonMatchingPeriod))
          case 403 if desResponse.errorCodeIs(ALREADY_SUBMITTED) => logger.warn(message = s"[PropertiesPeriodStatementResource][finaliseEndOfPeriodStatement] " +
            s"Error response ALREADY_SUBMITTED with correlationId : ${correlationId(desResponse)}")
            Forbidden(Errors.businessJsonError(Errors.AlreadySubmitted))
        }
      } recoverWith exceptionHandling
    }
}
