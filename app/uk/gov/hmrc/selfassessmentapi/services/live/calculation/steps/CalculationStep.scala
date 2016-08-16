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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.repositories.domain._

trait CalculationStep extends Math {

  def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability

  protected def applyDeductions(amount: BigDecimal, deductions: BigDecimal): (BigDecimal, BigDecimal) = {
    (positiveOrZero(amount - deductions), positiveOrZero(deductions - amount))
  }

  protected def allocateToTaxBands(income: BigDecimal, taxBands: Seq[TaxBandState]): Seq[TaxBandAllocation] = taxBands match {

    case taxBand :: otherBands =>
      val allocatedToThisBand = taxBand allocate income
      Seq(TaxBandAllocation(allocatedToThisBand, taxBand.taxBand)) ++ allocateToTaxBands(income - allocatedToThisBand, otherBands)

    case Nil => Nil
  }
}

case class TaxBandState(taxBand: TaxBand, available: BigDecimal) {

  def allocate(income: BigDecimal): BigDecimal = if (income < available) income else available
}

case class SelfAssessment(employments: Seq[MongoEmployment] = Seq(),selfEmployments: Seq[MongoSelfEmployment] = Seq(),
                          unearnedIncomes: Seq[MongoUnearnedIncome] = Seq(), ukProperties: Seq[MongoUKProperties] = Seq(),
                          furnishedHolidayLettings: Seq[MongoFurnishedHolidayLettings] = Seq())

case class PropertyNotComputedException(property: String) extends IllegalStateException(s"Cannot run calculation step as required property $property has not been computed yet")
