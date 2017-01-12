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

package uk.gov.hmrc.selfassessmentapi.resources.models.banks

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode

class BankSpec extends JsonSpec {

  "Bank JSON" should {

    "round ignore the id if it is provided by the user" in {
      val input = Bank(Some("myid"), "")
      val expectedOutput = input.copy(id = None)
      assertJsonIs(input, expectedOutput)
    }

    "return a MAX_FIELD_LENGTH_EXCEEDED error when account name is too long" in {
      val input = Bank(None, "A way toooooooooooooo long bank account name")
      assertValidationErrorWithCode(input, "/accountName", ErrorCode.MAX_FIELD_LENGTH_EXCEEDED)
    }

    "return a error when providing an empty Bank body" in {
      val json = "{}"

      assertValidationErrorsWithMessage[Bank](Json.parse(json),
        Map("/accountName" -> "error.path.missing",
            "/foreign" -> "error.path.missing"))
    }
  }
}
