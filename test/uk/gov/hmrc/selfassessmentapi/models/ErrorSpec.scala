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

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class ErrorSpec extends JsonSpec {
  "from" should {
    "transform a single DES error into our error representation" in {
      val desError =
        Json.parse(
          s"""
             |{
             |  "code": "SERVICE_UNAVAILABLE",
             |  "reason": "Dependent systems are currently not responding"
             |}
            """.stripMargin)

      val expected =
        Json.parse(
          s"""
             |{
             |  "code": "SERVICE_UNAVAILABLE",
             |  "message": "Dependent systems are currently not responding",
             |  "path": ""
             |}
           """.stripMargin)

      Errors.Error.from(desError) shouldBe expected
    }

    "transform multiple DES errors into our error representation" in {
      val desError =
        Json.parse(
          s"""
             |{
             |  "failures": [
             |    {
             |      "code": "INVALID_BUSINESSID",
             |      "reason": "Submission has not passed validation. Invalid parameter businessId."
             |    },
             |    {
             |      "code": "INVALID_PAYLOAD",
             |      "reason": "Submission has not passed validation. Invalid Payload."
             |    }
             |  ]
             |}
            """.stripMargin)

      val expected =
        Json.parse(
          s"""
             |{
             |  "code": "BUSINESS_ERROR",
             |  "message": "Business validation error",
             |  "errors": [
             |      {
             |        "code": "INVALID_BUSINESSID",
             |        "message": "Submission has not passed validation. Invalid parameter businessId.",
             |        "path": ""
             |      },
             |      {
             |        "code": "INVALID_PAYLOAD",
             |        "message": "Submission has not passed validation. Invalid Payload.",
             |        "path": ""
             |      }
             |  ]
             |}
           """.stripMargin)

      Errors.Error.from(desError) shouldBe expected
    }
  }
}
