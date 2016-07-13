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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import play.api.libs.json._

sealed trait TaxBand {
  def name: String
  def chargedAt: BigDecimal
  def lowerBound: BigDecimal
  def upperBound: Option[BigDecimal]
}

object TaxBand {

  implicit class TaxBandRangeCheck(val amount: BigDecimal) {

    def isWithin(taxBand: TaxBand): Boolean =
      amount >= taxBand.lowerBound && taxBand.upperBound.map(u => amount <= u).getOrElse(true)
  }

  case object BasicTaxBand extends TaxBand {
    val name = "basicRate"
    val chargedAt = BigDecimal(20)
    val lowerBound = BigDecimal(1)
    val upperBound = Some(BigDecimal(32000))
  }
  case object HigherTaxBand extends TaxBand {
    val name = "higherRate"
    val chargedAt = BigDecimal(40)
    val lowerBound = BigDecimal(32001)
    val upperBound = Some(BigDecimal(150000))
  }
  case object AdditionalHigherTaxBand extends TaxBand {
    val name = "additionalHigherRate"
    val chargedAt = BigDecimal(45)
    val lowerBound = BigDecimal(150001)
    val upperBound = None
  }

  implicit val format: Format[TaxBand] = new Format[TaxBand] {

    def reads(json: JsValue): JsResult[TaxBand] = {
      json.as[String] match {
        case BasicTaxBand.name => JsSuccess(BasicTaxBand)
        case HigherTaxBand.name => JsSuccess(HigherTaxBand)
        case AdditionalHigherTaxBand.name => JsSuccess(AdditionalHigherTaxBand)
        case unknown => throw new IllegalStateException(s"Unknown tax band '$unknown'")
      }
    }

    def writes(band: TaxBand): JsValue = {
      band match {
        case BasicTaxBand => JsString(BasicTaxBand.name)
        case HigherTaxBand => JsString(HigherTaxBand.name)
        case AdditionalHigherTaxBand => JsString(AdditionalHigherTaxBand.name)
        case _ => throw new IllegalStateException(s"Unknown tax band '${band.name}'")
      }
    }
  }
}
