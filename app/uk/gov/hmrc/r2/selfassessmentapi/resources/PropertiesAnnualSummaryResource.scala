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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import uk.gov.hmrc.utils.Nino
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.connectors.PropertiesAnnualSummaryConnector
import uk.gov.hmrc.r2.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.r2.selfassessmentapi.models.audit.AnnualSummaryUpdate
import uk.gov.hmrc.r2.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary, PropertiesAnnualSummary, PropertyType}
import uk.gov.hmrc.r2.selfassessmentapi.models.{ErrorResult, SourceType, TaxYear}
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.PropertiesAnnualSummaryResponse
import uk.gov.hmrc.r2.selfassessmentapi.services.{AuditData, AuditService, AuthorisationService}
import uk.gov.hmrc.utils.IdGenerator

import scala.concurrent.{ExecutionContext, Future}


class PropertiesAnnualSummaryResource @Inject()(
                                                 override val appContext: AppContext,
                                                 override val authService: AuthorisationService,
                                                 connector: PropertiesAnnualSummaryConnector,
                                                 auditService: AuditService,
                                                 cc: ControllerComponents,
                                                 val idGenerator: IdGenerator
                                               )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  def updateFHLAnnualSummary(nino: Nino, taxYear: TaxYear): Action[JsValue] = updateAnnualSummary(nino, PropertyType.FHL, taxYear)

  def updateOtherAnnualSummary(nino: Nino, taxYear: TaxYear): Action[JsValue] = updateAnnualSummary(nino, PropertyType.OTHER, taxYear)

  def updateAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.Properties, Some("annual")).async(parse.json) { implicit request =>

      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[PropertiesAnnualSummaryResource][updateAnnualSummary] " +
        s"Update property annual summary with correlationId : $correlationID")

      validateProperty(propertyId, request.body, connector.update(nino, propertyId, taxYear, _)) map {
        case Left(errorResult) => logger.warn(message = s"[PropertiesAnnualSummaryResource][updateAnnualSummary] " +
          s"Error occurred with correlationId : $correlationID")
          handleErrors(errorResult).withHeaders("X-CorrelationId" -> correlationID)
        case Right(response) =>
          auditService.audit(makeAnnualSummaryUpdateAudit(nino, propertyId, taxYear, request.authContext, response))
          response.filter {
            case 200 =>
              logger.warn(message = s"[PropertiesAnnualSummaryResource][updateAnnualSummary] " +
                s"Successfully updated property annual summary with status 200 and correlationId : ${correlationId(response)}")
              NoContent
            case 204 => logger.warn(message = s"[PropertiesAnnualSummaryResource][updateAnnualSummary] " +
              s"Successfully updated property annual summary with status 204 and correlationId : ${correlationId(response)}")
              NoContent
            case 404 if response.errorCodeIs(DesErrorCode.NOT_FOUND_PROPERTY) =>
              logger.warn(s"[PropertiesAnnualSummaryResource] [updateAnnualSummary #$propertyId] - DES Returned: ${DesErrorCode.NOT_FOUND_PROPERTY} " +
                s"CorrelationId: ${correlationId(response)}")
              NotFound
            case 410 if response.errorCodeIs(DesErrorCode.GONE) =>
              logger.warn(s"[PropertiesAnnualSummaryResource] [updateAnnualSummary #$propertyId] - DES Returned: ${DesErrorCode.GONE} " +
              s"CorrelationId: ${correlationId(response)}")
              NoContent
          }
      } recoverWith exceptionHandling
    }

  def retrieveFHLAnnualSummary(nino: Nino, taxYear: TaxYear): Action[AnyContent] = retrieveAnnualSummary(nino, PropertyType.FHL, taxYear)

  def retrieveOtherAnnualSummary(nino: Nino, taxYear: TaxYear): Action[AnyContent] = retrieveAnnualSummary(nino, PropertyType.OTHER, taxYear)

  def retrieveAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("annual")).async { implicit request =>

      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[PropertiesAnnualSummaryResource][retrieveAnnualSummary] " +
        s"Retrieve property annual summary with correlationId : $correlationID")

      connector.get(nino, propertyId, taxYear).map { response =>
        response.filter {
          case 200 =>
            response.annualSummary match {
              case Some(other: OtherPropertiesAnnualSummary) =>
                logger.warn(message = s"[PropertiesAnnualSummaryResource][retrieveAnnualSummary] " +
                s"Successfully retrieved other property annual summary with correlationId : ${correlationId(response)}")
                Ok(Json.toJson(other))
              case Some(fhl: FHLPropertiesAnnualSummary) =>
                logger.warn(message = s"[PropertiesAnnualSummaryResource][retrieveAnnualSummary] " +
                  s"Successfully retrieved fhl property annual summary with correlationId : ${correlationId(response)}")
                Ok(Json.toJson(fhl))
              case None => NotFound
            }
          case 404 =>
            logger.warn(
              s"[PropertiesAnnualSummaryResource] [retrieveAnnualSummary#$propertyId]\n" +
                s"Received from DES:\n ${response.underlying.body}\n" +
                s"CorrelationId: ${correlationId(response)}")
            NotFound
        }
      } recoverWith exceptionHandling
    }

  private def validateProperty(propertyId: PropertyType,
                               body: JsValue,
                               f: PropertiesAnnualSummary => Future[PropertiesAnnualSummaryResponse]): Future[Either[ErrorResult, PropertiesAnnualSummaryResponse]] =
    propertyId match {
      case PropertyType.OTHER => validate[OtherPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body)(f)
      case PropertyType.FHL => validate[FHLPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body)(f)
    }

  private def makeAnnualSummaryUpdateAudit(nino: Nino,
                                           id: PropertyType,
                                           taxYear: TaxYear,
                                           authCtx: AuthContext,
                                           response: PropertiesAnnualSummaryResponse)
                                          (implicit request: Request[JsValue]): AuditData[AnnualSummaryUpdate] =
    AuditData(
      detail = AnnualSummaryUpdate(
        httpStatus = response.status,
        nino = nino,
        sourceId = id.toString,
        taxYear = taxYear,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        transactionReference = response.status match {
          case 200 => response.transactionReference
          case _ => None
        },
        requestPayload = request.body,
        responsePayload = response.status match {
          case 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = s"$id-property-annual-summary-update"
    )
}
