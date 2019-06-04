/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.json.{Format, JsResult, JsValue}
import uk.gov.hmrc.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.models.banks.BankAnnualSummary

/**
  * Provides a suite of JSON formats for objects used throughout the codebase.
  */
object JsonFormatters {

  sealed abstract class MapEnumFormat[K <: Enumeration#Value, V: Format] extends Format[Map[K, V]] {
    override def writes(o: Map[K, V]): JsValue = {
      val mapped = o.map {
        case (k, v) => k.toString -> v
      }
      play.api.libs.json.Writes.mapWrites[V].writes(mapped)
    }

    override def reads(json: JsValue): JsResult[Map[K, V]]
  }

  object DividendsFormatters {
    import uk.gov.hmrc.selfassessmentapi.models.dividends

    implicit val annualSummaryMapFormat: Format[Map[TaxYear, dividends.Dividends]] = new Format[Map[TaxYear, dividends.Dividends]] {
      override def writes(o: Map[TaxYear, dividends.Dividends]): JsValue = {
        play.api.libs.json.Writes.mapWrites[dividends.Dividends].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, dividends.Dividends]] = {
        play.api.libs.json.Reads.mapReads[dividends.Dividends].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

  object BankFormatters {

    implicit val annualSummaryMapFormat: Format[Map[TaxYear, BankAnnualSummary]] = new Format[Map[TaxYear, BankAnnualSummary]] {
      override def writes(o: Map[TaxYear, BankAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[BankAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, BankAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[BankAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

}
