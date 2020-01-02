/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.properties.Properties
import org.joda.time.LocalDate

class AccountingTypeSpec extends JsonSpec {
  "Properties" should {
    "round trip" in {
      roundTripJson(Properties(AccountingPeriod(new LocalDate, new LocalDate())))
    }
  }

  "validate" should {

  def properties(propertiesType: String = "FHL", accPeriodStart: String = "2017-04-06",
              accPeriodEnd: String = "2018-04-05"): JsValue = {
      Json.parse("{}")
    }


    "pass when provided all valid accounting types" in {
      Seq(properties(), properties())
        .foreach(assertValidationPasses(_))
    }
  }
}
