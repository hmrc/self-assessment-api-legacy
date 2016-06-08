/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.domain

object ErrorCode extends Enumeration {
  type ErrorCode = Value
  val MAX_FIELD_LENGTH_EXCEEDED,
  INVALID_MONETARY_AMOUNT,
  INVALID_TAX_DEDUCTION_AMOUNT,
  COMMENCEMENT_DATE_NOT_IN_THE_PAST,
  MISSING_REGISTRATION_AUTHORITY,
  MAX_MONETARY_AMOUNT,
  NO_VALUE_FOUND,
  MAXIMUM_AMOUNT_EXCEEDED,
  UNDEFINED_REQUIRED_ELEMENT,
  INVALID_TYPE = Value
}

