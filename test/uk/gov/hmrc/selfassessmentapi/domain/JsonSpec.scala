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

import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec

class JsonSpec extends UnitSpec {

  def roundTripJson[T](json: T)(implicit format: Format[T]) = {
    val write = Json.toJson(json)
    val read = write.validate[T]
    read.asOpt shouldEqual Some(json)
  }

  def assertValidationError[T](o: T, expectedErrors: Map[ErrorCode, String], failureMessage: String)(implicit format: Format[T]): Unit = {
    val json = Json.toJson(o)(format)
    assertValidationError[T](json, expectedErrors, failureMessage)
  }

  def assertValidationError[T](json: JsValue, expectedErrors: Map[ErrorCode, String], failureMessage: String)(implicit format: Format[T]): Unit = {
    json.validate[T](format).fold(
      invalid => invalid.flatMap(_._2).map(error => (error.args.head, error.message)) should contain theSameElementsAs expectedErrors,
      valid => fail(failureMessage)
    )
  }

}
