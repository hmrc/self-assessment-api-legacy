/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.des

import ai.x.play.json.Jsonx
import play.api.libs.json._

case class TaxCalculation(profitFromSelfEmployment: Option[BigDecimal],
                          profitFromUkLandAndProperty: Option[BigDecimal],
                          interestReceivedFromUkBanksAndBuildingSocieties: Option[BigDecimal],
                          dividendsFromUkCompanies: Option[BigDecimal],
                          totalIncomeReceived: Option[BigDecimal],
                          personalAllowance: Option[BigDecimal],
                          totalIncomeOnWhichTaxIsDue: Option[BigDecimal],
                          payPensionsProfitAtBRT: Option[BigDecimal],
                          incomeTaxOnPayPensionsProfitAtBRT: Option[BigDecimal],
                          payPensionsProfitAtHRT: Option[BigDecimal],
                          incomeTaxOnPayPensionsProfitAtHRT: Option[BigDecimal],
                          payPensionsProfitAtART: Option[BigDecimal],
                          incomeTaxOnPayPensionsProfitAtART: Option[BigDecimal],
                          interestReceivedAtStartingRate: Option[BigDecimal],
                          incomeTaxOnInterestReceivedAtStartingRate: Option[BigDecimal],
                          interestReceivedAtZeroRate: Option[BigDecimal],
                          incomeTaxOnInterestReceivedAtZeroRate: Option[BigDecimal],
                          interestReceivedAtBRT: Option[BigDecimal],
                          incomeTaxOnInterestReceivedAtBRT: Option[BigDecimal],
                          interestReceivedAtHRT: Option[BigDecimal],
                          incomeTaxOnInterestReceivedAtHRT: Option[BigDecimal],
                          interestReceivedAtART: Option[BigDecimal],
                          incomeTaxOnInterestReceivedAtART: Option[BigDecimal],
                          dividendsAtZeroRate: Option[BigDecimal],
                          incomeTaxOnDividendsAtZeroRate: Option[BigDecimal],
                          dividendsAtBRT: Option[BigDecimal],
                          incomeTaxOnDividendsAtBRT: Option[BigDecimal],
                          dividendsAtHRT: Option[BigDecimal],
                          incomeTaxOnDividendsAtHRT: Option[BigDecimal],
                          dividendsAtART: Option[BigDecimal],
                          incomeTaxOnDividendsAtART: Option[BigDecimal],
                          totalIncomeOnWhichTaxHasBeenCharged: Option[BigDecimal],
                          incomeTaxDue: Option[BigDecimal],
                          incomeTaxCharged: Option[BigDecimal],
                          taxCreditsOnDividendsFromUkCompanies: Option[BigDecimal],
                          incomeTaxDueAfterDividendTaxCredits: Option[BigDecimal],
                          allowance: Option[BigDecimal],
                          limitBRT: Option[BigDecimal],
                          limitHRT: Option[BigDecimal],
                          rateBRT: Option[BigDecimal],
                          rateHRT: Option[BigDecimal],
                          rateART: Option[BigDecimal],
                          limitAIA: Option[BigDecimal],
                          allowanceBRT: Option[BigDecimal],
                          interestAllowanceHRT: Option[BigDecimal],
                          interestAllowanceBRT: Option[BigDecimal],
                          dividendAllowance: Option[BigDecimal],
                          dividendBRT: Option[BigDecimal],
                          dividendHRT: Option[BigDecimal],
                          dividendART: Option[BigDecimal],
                          proportionAllowance: Option[BigDecimal],
                          proportionLimitBRT: Option[BigDecimal],
                          proportionLimitHRT: Option[BigDecimal],
                          proportionalTaxDue: Option[BigDecimal],
                          proportionInterestAllowanceBRT: Option[BigDecimal],
                          proportionInterestAllowanceHRT: Option[BigDecimal],
                          proportionDividendAllowance: Option[BigDecimal],
                          proportionPayPensionsProfitAtART: Option[BigDecimal],
                          proportionIncomeTaxOnPayPensionsProfitAtART: Option[BigDecimal],
                          proportionPayPensionsProfitAtBRT: Option[BigDecimal],
                          proportionIncomeTaxOnPayPensionsProfitAtBRT: Option[BigDecimal],
                          proportionPayPensionsProfitAtHRT: Option[BigDecimal],
                          proportionIncomeTaxOnPayPensionsProfitAtHRT: Option[BigDecimal],
                          proportionInterestReceivedAtZeroRate: Option[BigDecimal],
                          proportionIncomeTaxOnInterestReceivedAtZeroRate: Option[BigDecimal],
                          proportionInterestReceivedAtBRT: Option[BigDecimal],
                          proportionIncomeTaxOnInterestReceivedAtBRT: Option[BigDecimal],
                          proportionInterestReceivedAtHRT: Option[BigDecimal],
                          proportionIncomeTaxOnInterestReceivedAtHRT: Option[BigDecimal],
                          proportionInterestReceivedAtART: Option[BigDecimal],
                          proportionIncomeTaxOnInterestReceivedAtART: Option[BigDecimal],
                          proportionDividendsAtZeroRate: Option[BigDecimal],
                          proportionIncomeTaxOnDividendsAtZeroRate: Option[BigDecimal],
                          proportionDividendsAtBRT: Option[BigDecimal],
                          proportionIncomeTaxOnDividendsAtBRT: Option[BigDecimal],
                          proportionDividendsAtHRT: Option[BigDecimal],
                          proportionIncomeTaxOnDividendsAtHRT: Option[BigDecimal],
                          proportionDividendsAtART: Option[BigDecimal],
                          proportionIncomeTaxOnDividendsAtART: Option[BigDecimal])

object TaxCalculation {
  implicit val format: Format[TaxCalculation] = Jsonx.formatCaseClass[TaxCalculation]
}
