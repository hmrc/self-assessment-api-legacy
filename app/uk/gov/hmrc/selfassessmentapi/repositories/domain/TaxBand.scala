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

import uk.gov.hmrc.selfassessmentapi.controllers.api.{CapAt, FlooredAt, PositiveOrZero, RoundDownToPennies}

sealed trait TaxBand {
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

  case class SavingsStartingTaxBand(startingSavingsRate: BigDecimal) extends TaxBand {
    override def name: String = "startingRate"
    override val chargedAt = BigDecimal(0)
    override val upperBound = Some(lowerBound - 1 + startingSavingsRate)
    override lazy val lowerBound = BigDecimal(1)
    override def toString = s"SavingsStartingTaxBand($lowerBound:${upperBound.get})"
  }

  case class NilTaxBand(precedingTaxBand: Option[TaxBand] = None, bandWidth: BigDecimal = 0) extends TaxBand {
    override def name: String = "nilRate"
    override val chargedAt = BigDecimal(0)
    override val upperBound = Some(lowerBound - 1 + bandWidth)
    override lazy val lowerBound = precedingTaxBand.flatMap(_.upperBound).getOrElse(BigDecimal(0)) + 1
    override def toString = s"NilTaxBand($lowerBound:${upperBound.get})"
  }

  case class BasicTaxBand(precedingTaxBand: Option[TaxBand] = None, reductionInUpperBound: BigDecimal = 0,
                          override val chargedAt: BigDecimal = 20) extends TaxBand {
    private val defaultUpperBound = 32000
    override def name: String = "basicRate"
    override val upperBound = Some(FlooredAt(defaultUpperBound - reductionInUpperBound, lowerBound - 1))
    override lazy val lowerBound = precedingTaxBand.flatMap(_.upperBound).getOrElse(BigDecimal(0)) + 1
    override def toString = s"BasicTaxBand($lowerBound:${upperBound.get})"
  }

  case class HigherTaxBand(precedingTaxBand: BasicTaxBand = BasicTaxBand(), reductionInUpperBound: BigDecimal = 0,
                           override val chargedAt: BigDecimal = 40) extends TaxBand {
    private val defaultUpperBound = 150000
    override def name: String = "higherRate"
    override val upperBound = Some(FlooredAt(defaultUpperBound - reductionInUpperBound, lowerBound - 1))
    override lazy val lowerBound = precedingTaxBand.upperBound.getOrElse(BigDecimal(0)) + 1
    override def toString = s"HigherTaxBand($lowerBound:${upperBound.get})"
  }

  case class AdditionalHigherTaxBand(precedingTaxBand: HigherTaxBand = HigherTaxBand(),
                                     override val chargedAt: BigDecimal = 45) extends TaxBand {
    override def name: String = "additionalHigherRate"
    override val upperBound = Some(BigDecimal(Integer.MAX_VALUE))
    override lazy val lowerBound = precedingTaxBand.upperBound.getOrElse(BigDecimal(0)) + 1
    override def toString = s"AdditionalHigherTaxBand($lowerBound:${upperBound.get})"
  }
}
