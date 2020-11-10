/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.TaxCalculationConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.audit.TaxCalculationTrigger
import uk.gov.hmrc.selfassessmentapi.models.calculation.CalculationRequest
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.TaxCalculationResponse
import uk.gov.hmrc.selfassessmentapi.services.{AuditData, AuditService, AuthorisationService}
import uk.gov.hmrc.utils.IdGenerator

import scala.concurrent.ExecutionContext

class TaxCalculationResource @Inject()(
                                        override val appContext: AppContext,
                                        override val authService: AuthorisationService,
                                        connector: TaxCalculationConnector,
                                        auditService: AuditService,
                                        cc: ControllerComponents,
                                        val idGenerator: IdGenerator
                                      )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  private val cannedEtaResponse =
    s"""
       |{
       |  "etaSeconds": 5
       |}
     """.stripMargin

  def requestCalculation(nino: Nino): Action[JsValue] =
    APIAction(nino, SourceType.Calculation).async(parse.json) { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[TaxCalculationResource][requestCalculation] " +
        s"Request for tax calculation with correlationId : $correlationID")

      validate[CalculationRequest, TaxCalculationResponse](request.body) { req =>
        connector.requestCalculation(nino, req.taxYear)
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(response) =>
          auditService.audit(makeTaxCalcTriggerAudit(nino, request.authContext, response))
          response.filter {
            case 200 => logger.warn(message = s"[TaxCalculationResource][requestCalculation] " +
              s"Success response with correlationId : ${correlationId(response)}")
              Accepted(Json.parse(cannedEtaResponse))
                .withHeaders(
                  LOCATION -> response.calcId
                    .map(id => s"/self-assessment/ni/$nino/calculations/$id")
                    .getOrElse(""))
            case 400 if response.isInvalidNino => logger.warn(message = s"[TaxCalculationResource][requestCalculation] " +
              s"BAD Request error with correlationId : ${correlationId(response)}")
              BadRequest(Json.toJson(Errors.NinoInvalid))
            case 400 if response.isInvalidRequest =>
              logger.warn("[TaxCalculationResource] [requestCalculation] DES returned INVALID_REQUEST. This could be due to;" +
                s"\n1. No valid income sources at backend\n2. No income submissions exist at backend with correlationId ${correlationId(response)}")
              unhandledResponse(response.status, logger)
            case _ => unhandledResponse(response.status, logger)
          }
      } recoverWith exceptionHandling
    }

  private def makeTaxCalcTriggerAudit(nino: Nino, authCtx: AuthContext, response: TaxCalculationResponse)(
    implicit request: Request[JsValue]): AuditData[TaxCalculationTrigger] =
    AuditData(
      detail = TaxCalculationTrigger(
        httpStatus = response.status,
        nino = nino,
        taxYear = request.body.as[CalculationRequest].taxYear,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        calculationId = response.status / 100 match {
          case 2 => Some(response.calcId.getOrElse(""))
          case _ => None
        },
        responsePayload = response.status match {
          case 202 | 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = "trigger-tax-calculation"
    )
}
