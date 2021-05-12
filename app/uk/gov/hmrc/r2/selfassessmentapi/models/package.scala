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

package uk.gov.hmrc.r2.selfassessmentapi

import play.api.libs.json._

package object models extends JodaReads with JodaWrites {

  //type BigDecimal = BigDecimal
  type SourceId = String
  type PropertyId = String
  type PeriodId = String
  type SummaryId = String
  type ValidationErrors = Seq[(JsPath, Seq[JsonValidationError])]

  private val MAX_AMOUNT =    BigDecimal("99999999999999.98")

  //added to correct the max amounts for R2 fields
  private val MAX_AMOUNT_R2 = BigDecimal("99999999999.99")

  /**
    * Asserts that amounts must have a maximum of two decimal places
    */
  val amountValidator: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(
      JsonValidationError("Amount should be a number less than 99999999999999.98 with up to 2 decimal places", ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount.scale < 3 && amount <= MAX_AMOUNT)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places
    */
  val nonNegativeAmountValidator: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(JsonValidationError("Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount >= 0 && amount.scale < 3 && amount <= MAX_AMOUNT)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places for release 2
    */
  val nonNegativeAmountValidatorR2: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(JsonValidationError("Amount should be a non-negative number less than 99999999999.99 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount >= 0 && amount.scale < 3 && amount <= MAX_AMOUNT_R2)

  /**
    * Asserts that amounts must have a maximum of two decimal places
    */
  val positiveOrNegativeAmountValidator: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(
      JsonValidationError("Amount should be a number between -99999999999.99 and 99999999999.99 with up to 2 decimal places",
        ErrorCode.INVALID_MONETARY_AMOUNT))(
        amount => amount >= -99999999999.99 && amount.scale < 3 && amount <= 99999999999.99)

}
