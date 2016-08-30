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
import uk.gov.hmrc.selfassessmentapi.domain.{CapAt, PositiveOrZero, RoundDownToPennies}

trait TaxBand {
  def name: String
  def lowerBound: BigDecimal
  def upperBound: Option[BigDecimal]
  val chargedAt: BigDecimal = 0
  def width = upperBound.map(_ - lowerBound + 1).getOrElse(BigDecimal(Long.MaxValue))
  def allocate(income: BigDecimal) = if (income < width) income else width
  def allocate2(taxableIncome: BigDecimal) = RoundDownToPennies(CapAt(PositiveOrZero(taxableIncome - (lowerBound - 1)), PositiveOrZero(width)))
}

object TaxBand {

  implicit class TaxBandRangeCheck(val amount: BigDecimal) extends AnyVal {

    def isWithin(taxBand: TaxBand): Boolean =
      amount >= taxBand.lowerBound && taxBand.upperBound.forall(amount <= _)
  }

  case object NilTaxBand extends TaxBand {
    val name = "nilRate"
    val lowerBound = BigDecimal(0)
    val upperBound = None
  }

  case object BasicTaxBand extends TaxBand {
    val name = "basicRate"
    val lowerBound = BigDecimal(1)
    val upperBound = Some(BigDecimal(32000))
  }

  case object HigherTaxBand extends TaxBand {
    val name = "higherRate"
    val lowerBound = BigDecimal(32001)
    val upperBound = Some(BigDecimal(150000))
  }

  case object AdditionalHigherTaxBand extends TaxBand {
    val name = "additionalHigherRate"
    val lowerBound = BigDecimal(150001)
    val upperBound = None
  }

  case object SavingsStartingTaxBand extends TaxBand { 
    val name = "startingRate"
    val lowerBound = BigDecimal(0)
    val upperBound = None 
  }

  implicit val format: Format[TaxBand] = new Format[TaxBand] {

    def reads(json: JsValue): JsResult[TaxBand] = {
      json.as[String] match {
        case NilTaxBand.name => JsSuccess(NilTaxBand)
        case BasicTaxBand.name => JsSuccess(BasicTaxBand)
        case HigherTaxBand.name => JsSuccess(HigherTaxBand)
        case AdditionalHigherTaxBand.name => JsSuccess(AdditionalHigherTaxBand)
        case SavingsStartingTaxBand.name => JsSuccess(SavingsStartingTaxBand)
        case unknown => throw new IllegalStateException(s"Unknown tax band '$unknown'")
      }
    }

    def writes(band: TaxBand): JsValue = {
      band match {
        case NilTaxBand => JsString(NilTaxBand.name)
        case BasicTaxBand => JsString(BasicTaxBand.name)
        case HigherTaxBand => JsString(HigherTaxBand.name)
        case AdditionalHigherTaxBand => JsString(AdditionalHigherTaxBand.name)
        case SavingsStartingTaxBand => JsString(SavingsStartingTaxBand.name)
        case _ => throw new IllegalStateException(s"Unknown tax band '${band.name}'")
      }
    }
  }
}
