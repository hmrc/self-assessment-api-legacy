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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import uk.gov.hmrc.selfassessmentapi.domain.FlooredAt
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand

object TaxBands {

  case class SavingsStartingTaxBand(startingSavingsRate: BigDecimal) extends TaxBand {
    override def name: String = ""
    override val chargedAt = BigDecimal(0)
    override val upperBound = Some(lowerBound - 1 + startingSavingsRate)
    override lazy val lowerBound = BigDecimal(1)
    override def toString = s"SavingsStartingTaxBand($lowerBound:${upperBound.get})"
  }

  case class NilTaxBand(precedingTaxBand: Option[TaxBand] = None, bandWidth: BigDecimal) extends TaxBand {
    override def name: String = ""
    override val chargedAt = BigDecimal(0)
    override val upperBound = Some(lowerBound - 1 + bandWidth)
    override lazy val lowerBound = precedingTaxBand.flatMap(_.upperBound).getOrElse(BigDecimal(0)) + 1
    override def toString = s"NilTaxBand($lowerBound:${upperBound.get})"
  }

  case class BasicTaxBand(precedingTaxBand: Option[TaxBand] = None, reductionInUpperBound: BigDecimal = 0,
                          override val chargedAt: BigDecimal = 20) extends TaxBand {
    override def name: String = ""
    override val upperBound = Some(FlooredAt(BigDecimal(32000) - reductionInUpperBound, lowerBound - 1))
    override lazy val lowerBound = precedingTaxBand.flatMap(_.upperBound).getOrElse(BigDecimal(0)) + 1
    override def toString = s"BasicTaxBand($lowerBound:${upperBound.get})"
  }

  case class HigherTaxBand(precedingTaxBand: BasicTaxBand = BasicTaxBand(), reductionInUpperBound: BigDecimal = 0,
                           override val chargedAt: BigDecimal = 40) extends TaxBand {
    override def name: String = ""
    override val upperBound = Some(FlooredAt(BigDecimal(150000) - reductionInUpperBound, lowerBound - 1))
    override lazy val lowerBound = precedingTaxBand.upperBound.getOrElse(BigDecimal(0)) + 1
    override def toString = s"HigherTaxBand($lowerBound:${upperBound.get})"
  }

  case class AdditionalHigherTaxBand(precedingTaxBand: HigherTaxBand = HigherTaxBand(),
                                     override val chargedAt: BigDecimal = 45) extends TaxBand {
    override def name: String = ""
    override val upperBound = Some(BigDecimal(Integer.MAX_VALUE))
    override lazy val lowerBound = precedingTaxBand.upperBound.getOrElse(BigDecimal(0)) + 1
    override def toString = s"AdditionalHigherTaxBand($lowerBound:${upperBound.get})"
  }
}
