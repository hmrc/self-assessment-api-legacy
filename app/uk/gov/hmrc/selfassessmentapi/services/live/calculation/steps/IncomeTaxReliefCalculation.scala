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

import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PropertyLocationType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.FurnishedHolidayLettingsMath._

object IncomeTaxReliefCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {
    liability.copy(
        allowancesAndReliefs =
          liability.allowancesAndReliefs.copy(incomeTaxRelief = Some(incomeTaxRelief(selfAssessment))))
  }

  def incomeTaxRelief(selfAssessment: SelfAssessment): BigDecimal = {
    selfEmploymentLossBroughtForward(selfAssessment.selfEmployments) +
    ukPropertiesLossBroughtForward(selfAssessment.ukProperties) +
    furnishedHolidayLettingsLossBroughtForward(selfAssessment.furnishedHolidayLettings)
  }

  private def ukPropertiesLossBroughtForward(ukProperties: Seq[MongoUKProperties]): BigDecimal = {
    roundUp(
        capAt(ukProperties.map(_.lossBroughtForward).sum,
              ukProperties.map(_.adjustedProfit).sum))
  }

  private def selfEmploymentLossBroughtForward(selfEmployments: Seq[MongoSelfEmployment]): BigDecimal = {
    roundUp(selfEmployments.map { selfEmployment =>
      capAt(selfEmployment.lossBroughtForward, selfEmployment.adjustedProfits)
    }.sum)
  }

  private def furnishedHolidayLettingsLossBroughtForward(lettings: Seq[MongoFurnishedHolidayLettings]) =
    toMapByPropertyLocation(lettings).map(pair => sumOfLossesBroughtForward(pair._2)).sum

  private def sumOfLossesBroughtForward(lettings: Seq[MongoFurnishedHolidayLettings]) = {
    val adjustedProfitSum = lettings.map {
      furnishedHolidayLetting => positiveOrZero(profitIncreases(furnishedHolidayLetting) - profitReductions(furnishedHolidayLetting))
    }.sum
    capAt(lettings.map(furnishedHolidayLetting => valueOrZero(furnishedHolidayLetting.adjustments.flatMap(_.lossBroughtForward))).sum, adjustedProfitSum)
  }

  private def toMapByPropertyLocation(lettings: Seq[MongoFurnishedHolidayLettings]) = {
    def toMap(map: Map[PropertyLocationType, List[MongoFurnishedHolidayLettings]], element: MongoFurnishedHolidayLettings) =
      map + (element.propertyLocation -> (element :: map(element.propertyLocation)))
    lettings.foldLeft(Map[PropertyLocationType, List[MongoFurnishedHolidayLettings]]().withDefaultValue(List()))(toMap)
  }
}
