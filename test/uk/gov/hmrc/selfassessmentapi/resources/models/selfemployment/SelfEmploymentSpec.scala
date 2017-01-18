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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.domain
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, AccountingType, ErrorCode}

class SelfEmploymentSpec extends JsonSpec {

  "SelfEmployment JSON" should {
    "round ignore the id if it is provided by the user" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")), AccountingType.CASH, Some(LocalDate.now.minusDays(1)))
      val expectedOutput = input.copy(id = None)

      assertJsonIs(input, expectedOutput)
    }

    "return a COMMENCEMENT_DATE_NOT_IN_THE_PAST error when using a commencement date in the future" in {
      val input = SelfEmployment(None, AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")), AccountingType.CASH, Some(LocalDate.now.plusDays(1)))
      assertValidationErrorWithCode(input,
        "/commencementDate", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "return a INVALID_ACCOUNTING_PERIOD error when startDate < endDate" in {
      val input = SelfEmployment(None, AccountingPeriod(LocalDate.parse("2017-04-02"), LocalDate.parse("2017-04-01")), AccountingType.CASH, Some(LocalDate.now.minusDays(1)))
      assertValidationErrorWithCode(input,
        "/accountingPeriod", ErrorCode.INVALID_ACCOUNTING_PERIOD)
    }

    "return a DATE_NOT_IN_THE_PAST error when proving an accounting period with a start date that is before 2017-04-01" in {
      val input = SelfEmployment(None, AccountingPeriod(LocalDate.parse("2017-03-01"), LocalDate.parse("2017-03-03")), AccountingType.CASH, Some(LocalDate.now.minusDays(1)))
      assertValidationErrorWithCode(input,
        "/accountingPeriod/start", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "return a INVALID_VALUE error when providing an invalid accounting type" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "2017-04-01",
           |    "end": "2017-04-02"
           |  },
           |  "accountingType": "OHNO",
           |  "commencementDate": "2016-01-01"
           |}
         """.stripMargin

      assertValidationErrorsWithCode[SelfEmployment](Json.parse(json), Map("/accountingType" -> Seq(ErrorCode.INVALID_VALUE)))
    }

    "return a error when providing an empty commencementDate" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "2017-04-01",
           |    "end": "2017-04-02"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": ""
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json), Map("/commencementDate" -> Seq("error.expected.jodadate.format")))
    }

    "return a error when providing an non-ISO (i.e. YYYY-MM-DD) commencementDate" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "2017-04-01",
           |    "end": "2017-04-02"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": "01-01-2016"
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json), Map("/commencementDate" -> Seq("error.expected.jodadate.format")))
    }

    "return a error when providing non-ISO (i.e. YYYY-MM-DD) dates to the accountingPeriod" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "01-01-2016",
           |    "end": "02-01-2016"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": "2016-01-01"
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod/start" -> Seq("error.expected.jodadate.format"),
          "/accountingPeriod/end" -> Seq("error.expected.jodadate.format")))
    }

    "return a error when providing an empty SelfEmployment body" in {
      val json = "{}"

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod" -> Seq("error.path.missing"),
            "/accountingType" -> Seq("error.path.missing")))
    }

    "return a error when providing an empty accountingPeriod body" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {},
           |  "accountingType": "CASH",
           |  "commencementDate": "2016-01-01"
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod/start" -> Seq("error.path.missing"),
            "/accountingPeriod/end" -> Seq("error.path.missing")))
    }
  }
}
