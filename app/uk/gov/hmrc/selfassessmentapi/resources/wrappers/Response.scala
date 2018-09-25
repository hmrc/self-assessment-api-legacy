/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.contexts.FilingOnlyAgent
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode.{DesErrorCode, _}
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, MultiDesError}
import uk.gov.hmrc.selfassessmentapi.models.{Errors, PeriodSummary, des}
import uk.gov.hmrc.selfassessmentapi.resources.AuthRequest

trait Response {
  val logger: Logger = Logger(this.getClass)

  def underlying: HttpResponse

  def json: JsValue = underlying.json

  val status: Int = underlying.status

  private def logResponse(): Unit =
    logger.error(s"DES error occurred with status code ${underlying.status} and body ${underlying.body}")

  def filter[A](pf: PartialFunction[Int, Result])(implicit request: AuthRequest[A]): Result =
    (status / 100, request.authContext) match {
      case (4, FilingOnlyAgent(_, _)) =>
        logResponse()
        BadRequest(toJson(Errors.InvalidRequest))
      case (4, _) | (5, _) =>
        logResponse()
        (pf orElse errorMapping)(status)
      case _ => ((pf andThen addCorrelationHeader) orElse errorMapping)(status)
    }

  private def addCorrelationHeader(result: Result) =
    underlying
      .header("CorrelationId")
      .fold(result)(correlationId => result.withHeaders("X-CorrelationId" -> correlationId))

  val errorMap: Map[des.DesErrorCode.Value, Option[Errors.Error]] =
    Map(
      INVALID_NINO -> Some(Errors.NinoInvalid),
      INVALID_PAYLOAD -> Some(Errors.InvalidRequest),
      INVALID_INCOMESOURCEID -> None,
      INVALID_BUSINESSID -> None,
      INVALID_INCOME_SOURCE -> None,
      INVALID_TYPE -> None,
      INVALID_IDENTIFIER -> None,
      INVALID_CALCID -> None,
      INVALID_ORIGINATOR_ID -> Some(Errors.InternalServerError),
      INVALID_DATE_FROM -> Some(Errors.InternalServerError),
      INVALID_DATE_TO -> Some(Errors.InternalServerError),
      INVALID_STATUS -> Some(Errors.InternalServerError),
      INVALID_TAX_YEAR -> Some(Errors.InternalServerError),
      INVALID_DATE_RANGE -> Some(Errors.InternalServerError),
      NOT_FOUND_INCOME_SOURCE -> None,
      INVALID_PERIOD -> Some(Errors.InvalidPeriod),
      NOT_CONTIGUOUS_PERIOD -> Some(Errors.NotContiguousPeriod),
      OVERLAPS_IN_PERIOD -> Some(Errors.OverlappingPeriod),
      NOT_ALIGN_PERIOD -> Some(Errors.MisalignedPeriod),
      SERVER_ERROR -> Some(Errors.InternalServerError),
      SERVICE_UNAVAILABLE -> Some(Errors.InternalServerError)
    )

  private def errorMapping: PartialFunction[Int, Result] = {
    case 400 if errorCodeIsOneOf(INVALID_NINO, INVALID_IDVALUE)    => BadRequest(toJson(Errors.NinoInvalid))
    case 400 if errorCodeIsOneOf(INVALID_PAYLOAD) => BadRequest(toJson(Errors.InvalidRequest))
    case 400
        if errorCodeIsOneOf(NOT_FOUND_NINO,
                            INVALID_BUSINESSID,
                            INVALID_INCOME_SOURCE,
                            INVALID_INCOMESOURCEID,
                            INVALID_TYPE,
                            INVALID_IDENTIFIER,
                            INVALID_CALCID) =>
      NotFound
    case 400
        if errorCodeIsOneOf(INVALID_ORIGINATOR_ID,
                            INVALID_DATE_FROM,
                            INVALID_DATE_TO,
                            INVALID_STATUS,
                            INVALID_TAX_YEAR) =>
      InternalServerError(toJson(Errors.InternalServerError))
    case 403 if errorCodeIsOneOf(NOT_UNDER_16) => Forbidden(toJson(Errors.businessError(Errors.NotUnder16)))
    case 403 if errorCodeIsOneOf(NOT_OVER_STATE_PENSION) => Forbidden(toJson(Errors.businessError(Errors.NotOverStatePension)))
    case 403 if errorCodeIsOneOf(MISSING_EXEMPTION_INDICATOR) => BadRequest(toJson(Errors.badRequest(Errors.MissingExemptionIndicator)))
    case 403 if errorCodeIsOneOf(MISSING_EXEMPTION_REASON) => BadRequest(toJson(Errors.badRequest(Errors.MandatoryFieldMissing)))
    case 403 if errorCodeIsOneOf(INVALID_DATE_RANGE) => InternalServerError(toJson(Errors.InternalServerError))
    case 403 if errorCodeIsOneOf(INVALID_TAX_CALCULATION_ID) => Forbidden(toJson(Errors.businessError(Errors.InvalidTaxCalculationId)))
    case 403                                         => NotFound
    case 404                                         => NotFound
    case 409 if errorCodeIsOneOf(INVALID_PERIOD)     =>
      //This has been superceded by InvalidCreatePeriod in the PropertiesPeriodResource.
      BadRequest(toJson(Errors.badRequest(Errors.InvalidPeriod)))
    case 409 if errorCodeIsOneOf(NOT_CONTIGUOUS_PERIOD) =>
      Forbidden(toJson(Errors.businessError(Errors.NotContiguousPeriod)))
    case 409 if errorCodeIsOneOf(OVERLAPS_IN_PERIOD) =>
      Forbidden(toJson(Errors.businessError(Errors.OverlappingPeriod)))
    case 409 if errorCodeIsOneOf(BOTH_EXPENSES_SUPPLIED) => BadRequest(toJson(Errors.badRequest(Errors.BothExpensesSupplied)))
    case 409 if errorCodeIsOneOf(NOT_ALIGN_PERIOD) => Forbidden(toJson(Errors.businessError(Errors.MisalignedPeriod)))
    case 409 if errorCodeIsOneOf(NOT_ALLOWED_SIMPLIFIED_EXPENSES) => Forbidden(toJson(Errors.businessError(Errors.NotAllowedConsolidatedExpenses)))
    case 409
        if isMultiDesError && errorCodesContainOneOf(NOT_CONTIGUOUS_PERIOD, OVERLAPS_IN_PERIOD, NOT_ALIGN_PERIOD) =>
      val apiErrors = desErrorsToApiErrors(json.asOpt[MultiDesError].get.failures)
      Forbidden(toJson(Errors.businessError(apiErrors)))
    case 500 if errorCodeIsOneOf(SERVER_ERROR)        => InternalServerError(toJson(Errors.InternalServerError))
    case 503 if errorCodeIsOneOf(SERVICE_UNAVAILABLE) => InternalServerError(toJson(Errors.InternalServerError))
    case _                                            => InternalServerError(toJson(Errors.InternalServerError))
  }

  def errorCodeIs(errorCode: DesErrorCode): Boolean =
    json.asOpt[DesError].exists(_.code == errorCode)

  def errorCodeIsOneOf(errorCodes: DesErrorCode*): Boolean =
    json.asOpt[DesError].exists(errorCode => errorCodes.contains(errorCode.code))

  def isMultiDesError: Boolean =
    json.asOpt[MultiDesError].isDefined

  def errorCodesContainOneOf(errorCodes: DesErrorCode*): Boolean =
    json.asOpt[MultiDesError].exists(_.failures.map(_.code).intersect(errorCodes).nonEmpty)

  def desErrorsToApiErrors(desErrors: Seq[DesError]): Seq[Errors.Error] =
    desErrors.flatMap(e => errorMap(e.code))
}
