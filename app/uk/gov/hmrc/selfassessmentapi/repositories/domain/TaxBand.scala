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
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math

sealed trait TaxBand extends Math {
  def name: String
  def id: String
  def chargedAt: BigDecimal
  def lowerBound: BigDecimal
  def upperBound: Option[BigDecimal]

  def width = upperBound.map(_ - lowerBound + 1).getOrElse(BigDecimal(Long.MaxValue))
}

object TaxBand {

  implicit class TaxBandRangeCheck(val amount: BigDecimal) extends AnyVal {

    def isWithin(taxBand: TaxBand): Boolean =
      amount >= taxBand.lowerBound && taxBand.upperBound.forall(amount <= _)
  }

  case object BasicTaxBand extends TaxBand {
    val id = "BasicTaxBandId"
    val name = "basicRate"
    val chargedAt = BigDecimal(20)
    val lowerBound = BigDecimal(1)
    val upperBound = Some(BigDecimal(32000))
  }

  case object HigherTaxBand extends TaxBand {
    val id = "HigherTaxBandId"
    val name = "higherRate"
    val chargedAt = BigDecimal(40)
    val lowerBound = BigDecimal(32001)
    val upperBound = Some(BigDecimal(150000))

  }

  case object AdditionalHigherTaxBand extends TaxBand {
    val id = "AdditionalHigherTaxBandId"
    val name = "additionalHigherRate"
    val chargedAt = BigDecimal(45)
    val lowerBound = BigDecimal(150001)
    val upperBound = None
  }

  case object SavingsStartingTaxBand extends TaxBand { 
    val id = "SavingsStartingTaxBandId"
    val name = "startingRate"
    val chargedAt = BigDecimal(0)
    val lowerBound = BigDecimal(0)
    val upperBound = None 
  }
  
  case object SavingsNilTaxBand extends TaxBand {
    val id = "SavingsNilTaxBandId"
    val name = "nilRate"
    val chargedAt = BigDecimal(0)
    val lowerBound = BigDecimal(0)
    val upperBound = None 
  }

  case object DividendsNilTaxBand extends TaxBand {
    val id = "DividendsNilTaxBandId"
    val name = "nilRate"
    val chargedAt = BigDecimal(0)
    val lowerBound = BigDecimal(0)
    val upperBound = None
  }

  case object DividendBasicTaxBand extends TaxBand {
    val id = "DividendBasicTaxBandId"
    val name = "basicRate"
    val chargedAt = BigDecimal(7.5)
    val lowerBound = BigDecimal(5001)
    val upperBound = Some(BigDecimal(32000))
  }

  case object DividendHigherTaxBand extends TaxBand {
    val id = "DividendHigherTaxBandId"
    val name = "higherRate"
    val chargedAt = BigDecimal(32.5)
    val lowerBound = BigDecimal(32001)
    val upperBound = Some(BigDecimal(150000))
  }

  case object DividendAdditionalHigherTaxBand extends TaxBand {
    val id = "DividendAdditionalHigherTaxBandID"
    val name = "additionalHigherRate"
    val chargedAt = BigDecimal(38.1)
    val lowerBound = BigDecimal(150001)
    val upperBound = None
  }

  implicit val format: Format[TaxBand] = new Format[TaxBand] {

    def reads(json: JsValue): JsResult[TaxBand] = {
      json.as[String] match {
        case BasicTaxBand.id => JsSuccess(BasicTaxBand)
        case HigherTaxBand.id => JsSuccess(HigherTaxBand)
        case AdditionalHigherTaxBand.id => JsSuccess(AdditionalHigherTaxBand)
        case SavingsStartingTaxBand.id => JsSuccess(SavingsStartingTaxBand)
        case SavingsNilTaxBand.id => JsSuccess(SavingsNilTaxBand)
        case DividendsNilTaxBand.id => JsSuccess(DividendsNilTaxBand)
        case DividendBasicTaxBand.id => JsSuccess(DividendBasicTaxBand)
        case DividendHigherTaxBand.id => JsSuccess(DividendHigherTaxBand)
        case DividendAdditionalHigherTaxBand.id => JsSuccess(DividendAdditionalHigherTaxBand)
        case unknown => throw new IllegalStateException(s"Unknown tax band '$unknown'")
      }
    }

    def writes(band: TaxBand): JsValue = {
      band match {
        case BasicTaxBand => JsString(BasicTaxBand.id)
        case HigherTaxBand => JsString(HigherTaxBand.id)
        case AdditionalHigherTaxBand => JsString(AdditionalHigherTaxBand.id)
        case SavingsStartingTaxBand => JsString(SavingsStartingTaxBand.id)
        case SavingsNilTaxBand => JsString(SavingsNilTaxBand.id)
        case DividendsNilTaxBand => JsString(DividendsNilTaxBand.id)
        case DividendBasicTaxBand => JsString(DividendBasicTaxBand.id)
        case DividendHigherTaxBand => JsString(DividendHigherTaxBand.id)
        case DividendAdditionalHigherTaxBand => JsString(DividendAdditionalHigherTaxBand.id)
        case _ => throw new IllegalStateException(s"Unknown tax band '${band.name}'")
      }
    }
  }
}
