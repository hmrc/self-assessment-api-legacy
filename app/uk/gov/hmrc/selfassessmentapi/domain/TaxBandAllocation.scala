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

import uk.gov.hmrc.selfassessmentapi.repositories.domain.functional.TaxBand

case class TaxBandAllocation(amount: BigDecimal, taxBand: TaxBand) {

  def toTaxBandSummary(chargedAt: BigDecimal) =
    uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary(taxBand.name, amount, s"$chargedAt%", tax(chargedAt))

  def toTaxBandSummary = uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary(taxBand.name, amount, s"${taxBand.chargedAt}%", tax(taxBand.chargedAt))

  def tax(chargedAt: BigDecimal): BigDecimal = RoundDownToPennies(amount * chargedAt / 100)

  def available: BigDecimal = PositiveOrZero(taxBand.width - amount)

  def +(other: TaxBandAllocation) = {
    require(taxBand == other.taxBand)
    TaxBandAllocation(amount + other.amount, taxBand)
  }
}
