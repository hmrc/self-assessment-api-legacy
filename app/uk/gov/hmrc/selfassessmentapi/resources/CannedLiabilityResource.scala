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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.resources.models.{SourceId, SourceType}

import scala.concurrent.Future

object CannedLiabilityResource extends BaseController {

  private val magicId = "123abc"
  private val featureSwitch = FeatureSwitchAction(SourceType.Liability)

  private val cannedEtaResponse =
    s"""
       |{
       |  "etaSeconds": 5
       |}
     """.stripMargin

  private val cannedLiabilityResponse =
    s"""
       |{
       |  "profitFromSelfEmployment": 100.25,
       |  "profitFromUkLandAndProperty": 100.25,
       |  "interestReceivedFromUkBanksAndBuildingSocieties": 100.25,
       |  "dividendsFromUkCompanies": 100.25,
       |  "totalIncomeReceived": 100.25,
       |  "personalAllowance": 100.25,
       |  "totalIncomeOnWhichTaxIsDue": 100.25,
       |  "payPensionsProfitAtBRT": 100.25,
       |  "incomeTaxOnPayPensionsProfitAtBRT": 100.25,
       |  "payPensionsProfitAtHRT": 100.25,
       |  "incomeTaxOnPayPensionsProfitAtHRT": 100.25,
       |  "payPensionsProfitAtART": 100.25,
       |  "incomeTaxOnPayPensionsProfitAtART": 100.25,
       |  "interestReceivedAtStartingRate": 100.25,
       |  "incomeTaxOnInterestReceivedAtStartingRate": 100.25,
       |  "interestReceivedAtZeroRate": 100.25,
       |  "incomeTaxOnInterestReceivedAtZeroRate": 100.25,
       |  "interestReceivedAtBRT": 100.25,
       |  "incomeTaxOnInterestReceivedAtBRT": 100.25,
       |  "interestReceivedAtHRT": 100.25,
       |  "incomeTaxOnInterestReceivedAtHRT": 100.25,
       |  "interestReceivedAtART": 100.25,
       |  "incomeTaxOnInterestReceivedAtART": 100.25,
       |  "dividendsAtZeroRate": 100.25,
       |  "incomeTaxOnDividendsAtZeroRate": 100.25,
       |  "dividendsAtBRT": 100.25,
       |  "incomeTaxOnDividendsAtBRT": 100.25,
       |  "dividendsAtHRT": 100.25,
       |  "incomeTaxOnDividendsAtHRT": 100.25,
       |  "dividendsAtART": 100.25,
       |  "incomeTaxOnDividendsAtART": 100.25,
       |  "totalIncomeOnWhichTaxHasBeenCharged": 100.25,
       |  "incomeTaxDue": 100.25,
       |  "incomeTaxCharged": 100.25,
       |  "taxCreditsOnDividendsFromUkCompanies": 100.25,
       |  "incomeTaxDueAfterDividendTaxCredits": 100.25,
       |  "incomeTaxOverPaid": 100.25,
       |  "allowance": 100.25,
       |  "limitBRT": 100.25,
       |  "limitHRT": 100.25,
       |  "rateBRT": 100.25,
       |  "rateHRT": 100.25,
       |  "rateART": 100.25,
       |  "limitAIA": 100.25,
       |  "allowanceBRT": 100.25,
       |  "interestAllowanceHRT": 100.25,
       |  "interestAllowanceBRT": 100.25,
       |  "dividendAllowance": 100.25,
       |  "dividendBRT": 100.25,
       |  "dividendHRT": 100.25,
       |  "dividendART": 100.25,
       |  "proportionAllowance": 100.25,
       |  "proportionLimitBRT": 100.25,
       |  "proportionLimitHRT": 100.25,
       |  "proportionalTaxDue": 100.25,
       |  "proportionInterestAllowanceBRT": 100.25,
       |  "proportionInterestAllowanceHRT": 100.25,
       |  "proportionDividendAllowance": 100.25,
       |  "proportionPayPensionsProfitAtART": 100.25,
       |  "proportionIncomeTaxOnPayPensionsProfitAtART": 100.25,
       |  "proportionPayPensionsProfitAtBRT": 100.25,
       |  "proportionIncomeTaxOnPayPensionsProfitAtBRT": 100.25,
       |  "proportionPayPensionsProfitAtHRT": 100.25,
       |  "proportionIncomeTaxOnPayPensionsProfitAtHRT": 100.25,
       |  "proportionInterestReceivedAtZeroRate": 100.25,
       |  "proportionIncomeTaxOnInterestReceivedAtZeroRate": 100.25,
       |  "proportionInterestReceivedAtBRT": 100.25,
       |  "proportionIncomeTaxOnInterestReceivedAtBRT": 100.25,
       |  "proportionInterestReceivedAtHRT": 100.25,
       |  "proportionIncomeTaxOnInterestReceivedAtHRT": 100.25,
       |  "proportionInterestReceivedAtART": 100.25,
       |  "proportionIncomeTaxOnInterestReceivedAtART": 100.25,
       |  "proportionDividendsAtZeroRate": 100.25,
       |  "proportionIncomeTaxOnDividendsAtZeroRate": 100.25,
       |  "proportionDividendsAtBRT": 100.25,
       |  "proportionIncomeTaxOnDividendsAtBRT": 100.25,
       |  "proportionDividendsAtHRT": 100.25,
       |  "proportionIncomeTaxOnDividendsAtHRT": 100.25,
       |  "proportionDividendsAtART": 100.25,
       |  "proportionIncomeTaxOnDividendsAtART": 100.25
       |}
     """.stripMargin

  def requestLiability(nino: Nino): Action[AnyContent] = featureSwitch.asyncFeatureSwitch {
    Future.successful {
      Accepted(Json.parse(cannedEtaResponse))
        .withHeaders("Location" -> s"/self-assessment/ni/$nino/liability-calculation/$magicId")
    }
  }

  def retrieveLiability(nino: Nino, liabilityId: SourceId): Action[AnyContent] = featureSwitch.asyncFeatureSwitch {
    Future.successful {
      if (liabilityId == magicId) Ok(Json.parse(cannedLiabilityResponse))
      else NotFound
    }
  }
}
