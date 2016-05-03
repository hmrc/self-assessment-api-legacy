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

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, Json}

class SelfEmploymentSpec extends JsonSpec {

  "format" should {
    def roundTripJson(selfEmployment: SelfEmployment) = {
      val json = Json.toJson(selfEmployment)
      val read = json.validate[SelfEmployment]

      read.asOpt shouldEqual Some(selfEmployment)
    }

    "round trip SelfEmployment json when id present" in {
      roundTripJson(SelfEmployment(
        Some("id"), "self employment 1", new LocalDate(2016, 4, 22)))
    }

    "round trip SelfEmployment json with no id" in {
      roundTripJson(SelfEmployment(
        None, "self employment 1", new LocalDate(2016, 4, 22)))
    }
  }

  "validate" should {
    "reject name longer than 100 characters and commencement date after the present date" in {

      val se = SelfEmployment(name = "a" * 101, commencementDate = LocalDate.now().plusDays(1))
      val json = Json.toJson(se)

      val validationErrors = json.validate[SelfEmployment].asInstanceOf[JsError].errors.flatMap(x => x._2)

      validationErrors should contain theSameElementsAs Seq(ValidationError("commencement date should be in the past"), ValidationError("error.maxLength", 100))

    }
  }
}
