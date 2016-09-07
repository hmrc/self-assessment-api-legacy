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

package uk.gov.hmrc.selfassessmentapi.controllers.definition

import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._

object JsonFormatters {

  implicit val formatAPIStatus = EnumJson.enumFormat(APIStatus)
  implicit val formatAuthType = EnumJson.enumFormat(AuthType)
  implicit val formatHttpMethod = EnumJson.enumFormat(HttpMethod)
  implicit val formatResourceThrottlingTier = EnumJson.enumFormat(ResourceThrottlingTier)

  implicit val formatParameter = Json.format[Parameter]
  implicit val formatEndpoint = Json.format[Endpoint]
  implicit val formatAccess = Json.format[Access]
  implicit val formatAPIVersion = Json.format[APIVersion]
  implicit val formatAPIDefinition = Json.format[APIDefinition]
  implicit val formatScope = Json.format[Scope]
  implicit val formatDefinition = Json.format[Definition]

}

object EnumJson {

  def enumReads[E <: Enumeration](enum: E, valueMissingMessage: Option[String] = None): Reads[E#Value] = new Reads[E#Value] {

    def defaultValueMissingMessage(s: String)= s"Enumeration expected of type: '${enum.getClass}', but it does not contain '$s'"

    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) => {
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException =>
            JsError(JsPath(), ValidationError(valueMissingMessage.getOrElse(defaultValueMissingMessage(s)), NO_VALUE_FOUND))
        }
      }
      case _ => JsError(JsPath(), ValidationError("String value expected", INVALID_TYPE))
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E, valueMissingMessage: Option[String] = None): Format[E#Value] = {
    Format(enumReads(enum, valueMissingMessage), enumWrites)
  }

}
