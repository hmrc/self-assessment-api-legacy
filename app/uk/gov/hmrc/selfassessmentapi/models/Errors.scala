/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models

import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode.ErrorCode

object Errors {

  implicit val errorDescWrites: Writes[Error] = Json.writes[Error]
  implicit val badRequestWrites: Writes[BadRequest] = new Writes[BadRequest] {
    override def writes(req: BadRequest): JsValue = {
      Json.obj("code" -> req.code, "message" -> req.message, "errors" -> req.errors)
    }
  }

  implicit val businessErrorWrites: Writes[BusinessError] = new Writes[BusinessError] {
    override def writes(req: BusinessError) =
      Json.obj("code" -> req.code, "message" -> req.message, "errors" -> req.errors)
  }

  implicit val internalServerErrorWrites: Writes[InternalServerError] = new Writes[InternalServerError] {
    override def writes(req: InternalServerError): JsValue =
      Json.obj("code" -> req.code, "message" -> req.message)
  }

  case class Error(code: String, message: String, path: Option[String])

  case class BadRequest(errors: Seq[Error], message: String) {
    val code = "INVALID_REQUEST"
  }

  case class BusinessError(errors: Seq[Error], message: String) {
    val code = "BUSINESS_ERROR"
  }

  case class InternalServerError(message: String) {
    val code = "INTERNAL_SERVER_ERROR"
  }

  object NinoInvalid extends Error("NINO_INVALID", "The provided Nino is invalid", None)
  object InvalidRequest extends Error("INVALID_REQUEST", "Invalid request", None)
  object BothExpensesSupplied extends Error("BOTH_EXPENSES_SUPPLIED", "Elements: expenses and consolidatedElements cannot be both specified at the same time", None)
  object NotAllowedConsolidatedExpenses extends Error("NOT_ALLOWED_CONSOLIDATED_EXPENSES", "The submission contains consolidated expenses but the accumulative turnover amount exceeds the threshold", Some(""))
  object InvalidPeriod extends Error("INVALID_PERIOD", "The period 'from' date should come before the 'to' date", Some(""))
  object NotUnder16 extends Error("NOT_UNDER_16", "The Individual's age is equal to or greater than 16 years old on the 6th April of current tax year.", Some("/nonFinancials/class4NicInfo/exemptionCode"))
  object NotOverStatePension extends Error("NOT_OVER_STATE_PENSION", "The Individual's age is less than their State Pension age on the 6th April of current tax year.", Some("/nonFinancials/class4NicInfo/exemptionCode"))
  object MissingExemptionIndicator extends Error("INVALID_VALUE", "Exemption code must be present only if the exempt flag is set to true", Some("/nonFinancials/class4NicInfo"))
  object MandatoryFieldMissing extends Error("MANDATORY_FIELD_MISSING", "Exemption code value must be present if the exempt flag is set to true", Some("/nonFinancials/class4NicInfo"))
  object NotContiguousPeriod extends Error("NOT_CONTIGUOUS_PERIOD", "Periods should be contiguous.", Some(""))
  object OverlappingPeriod extends Error("OVERLAPPING_PERIOD", "Period overlaps with existing periods.", Some(""))
  object MisalignedPeriod extends Error("MISALIGNED_PERIOD", "Period is not within the accounting period.", Some(""))
  object ClientNotSubscribed extends Error("CLIENT_NOT_SUBSCRIBED", "The client is not subscribed to MTD", None)
  object AgentNotAuthorized extends Error("AGENT_NOT_AUTHORIZED", "The agent is not authorized", None)
  object AgentNotSubscribed extends Error("AGENT_NOT_SUBSCRIBED", "The agent is not subscribed to agent services", None)
  object BadToken extends Error("UNAUTHORIZED", "Bearer token is missing or not authorized", None)
  object BadRequest extends Error("INVALID_REQUEST", "Invalid request", None)
  object InternalServerError extends Error("INTERNAL_SERVER_ERROR", "An internal server error occurred", None)
  object NotFinalisedDeclaration extends Error("NOT_FINALISED", "The statement cannot be accepted without a declaration it is finalised.", Some("/finalised"))
  object PeriodicUpdateMissing extends Error("PERIODIC_UPDATE_MISSING", "End-of-period statement cannot be accepted until all periodic updates have been submitted.", None)
  object InvalidDateRange extends Error("INVALID_DATE_RANGE", "The start date must be the same day or before the from date.", None)
  object EarlySubmission extends Error("EARLY_SUBMISSION", "You cannot submit a statement before the end of your accounting period.", None)
  object NonMatchingPeriod extends Error("NON_MATCHING_PERIOD", "You cannot submit your end-of-period statement for a period that does not match your accounting period.", None)

  def badRequest(validationErrors: ValidationErrors) = BadRequest(flattenValidationErrors(validationErrors), "Invalid request")
  def badRequest(error: Error) = BadRequest(Seq(error), "Invalid request")
  def badRequest(message: String) = BadRequest(Seq.empty, message)

  def businessError(error: Error): BusinessError = businessError(Seq(error))
  def businessError(errors: Seq[Error]): BusinessError = BusinessError(errors, "Business validation error")

  private def flattenValidationErrors(validationErrors: ValidationErrors): Seq[Error] = {
    validationErrors.flatMap { validationError =>
      val (path, errors) = validationError
      errors.map { err =>
        err.args match {
          case Seq(head, _*) if head.isInstanceOf[ErrorCode] =>
            Error(head.asInstanceOf[ErrorCode].toString, err.message, Some(path.toString()))
          case _ => convertErrorMessageToCode(err, path.toString())
        }
      }
    }
  }

  /*
   * Converts a Play error without an error code into an Error that contains an error code
   * based on the content of the error message.
   */
  private def convertErrorMessageToCode(playError: ValidationError, errorPath: String): Error = {
    playError.message match {
      case "error.expected.jodadate.format" => Error("INVALID_DATE", "please provide a date in ISO format (i.e. YYYY-MM-DD)", Some(errorPath))
      case "error.path.missing" => Error("MANDATORY_FIELD_MISSING", "a mandatory field is missing", Some(errorPath))
      case "error.expected.numberformatexception" => Error("INVALID_NUMERIC_VALUE", "please provide a numeric field", Some(errorPath))
      case "error.expected.jsstring" => Error("INVALID_STRING_VALUE", "please provide a string field", Some(errorPath))
      case "error.expected.jsboolean" => Error("INVALID_BOOLEAN_VALUE", "please provide a valid boolean field", Some(errorPath))
      case _ => Error("UNMAPPED_PLAY_ERROR", playError.message, Some(errorPath))
    }
  }

}
