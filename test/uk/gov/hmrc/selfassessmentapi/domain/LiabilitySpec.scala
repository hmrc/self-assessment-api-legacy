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

import org.json.{JSONArray, JSONObject}
import org.skyscreamer.jsonassert.JSONAssert._
import org.skyscreamer.jsonassert.JSONCompareMode._
import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.BasicTaxBand
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

class LiabilitySpec extends JsonSpec {

  "EmploymentIncome" should {
    roundTripJson(EmploymentIncome("abc", 5000, 50, 50, 4950))
  }

  "SelfEmploymentIncome" should {
    "correctly map to JSON" in {
      roundTripJson(SelfEmploymentIncome("abc", 500, 500))
    }
  }

  "FurnishedHolidayLettingIncome" should {
    "correctly map to JSON" in {
      roundTripJson(FurnishedHolidayLettingIncome("abc", 500))
    }
  }

  "InterestFromUKBanksAndBuildingSocieties" should {
    "correctly map to JSON" in {
      roundTripJson(InterestFromUKBanksAndBuildingSocieties("abc", 500))
    }
  }

  "DividendsFromUKSources" should {
    "correctly map to JSON" in {
      roundTripJson(DividendsFromUKSources("abc", 500))
    }
  }

  "TaxBandAllocation" should {
    "correctly map to JSON" in {
      roundTripJson(TaxBandAllocation(500, BasicTaxBand))
    }
  }

  "AllowancesAndReliefs" should {
    "correctly map to JSON" in {
      roundTripJson(AllowancesAndReliefs(Some(500), Some(500), None, Some(500), None))
    }
  }

  "MongoTaxDeducted" should {
    "correctly map to JSON" in {
      roundTripJson(MongoTaxDeducted(
        interestFromUk = 500,
        totalDeductionFromUkProperties =
          500,
        deductionFromUkProperties = Seq(TaxPaidForUkProperty(sourceId = "abc", taxPaid = 500)),
        ukTaxPaid = 500,
        ukTaxesPaidForEmployments = Seq(MongoUkTaxPaidForEmployment("abc", 500))))
    }
  }

  "UKPropertyIncome" should {
    "correctly map to JSON" in {
      roundTripJson(UkPropertyIncome("abc", 500))
    }
  }

  private implicit class JsValueComparator(jsValue: JsValue) {
    def bodyIs(expectedBody: String) = {
      jsValue match {
        case arr: JsArray => assertEquals(expectedBody, new JSONArray(arr.toString()), STRICT)
        case _ => assertEquals(expectedBody, new JSONObject(jsValue.toString()), STRICT)
      }
      this
    }
  }
}
