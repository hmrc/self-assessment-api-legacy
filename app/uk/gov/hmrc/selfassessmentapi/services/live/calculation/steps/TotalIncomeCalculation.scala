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

import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability

object TotalIncomeCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {

    val (profitsFromSelfEmployment, taxableProfits) = liability.profitFromSelfEmployments.map(income => (income.profit, income.taxableProfit)).unzip
    val nonSavingsIncomeReceived = profitsFromSelfEmployment.sum
    val savingsIncomeReceived = liability.interestFromUKBanksAndBuildingSocieties.map(_.totalInterest).sum
    val dividendsIncomeReceived = liability.dividendsFromUKSources.map(_.totalDividend).sum
    val totalIncomeReceived = nonSavingsIncomeReceived + savingsIncomeReceived + dividendsIncomeReceived

    liability.copy(nonSavingsIncomeReceived = Some(nonSavingsIncomeReceived), totalIncomeReceived = Some(totalIncomeReceived), totalTaxableIncome = Some(taxableProfits.sum))
  }
}
