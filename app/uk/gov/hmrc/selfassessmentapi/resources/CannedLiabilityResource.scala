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
       |  "payFromAllEmployments": 100.25,
       |  "benefitsAndExpensesReceived": 100.25,
       |  "payFromAllEmploymentsAfterExpenses": 100.25,
       |  "interestReceivedFromUkBanksAndBuildingSocieties": 100.25,
       |  "dividendsFromUkCompanies": 100.25,
       |  "totalIncomeReceived": 100.25,
       |  "personalAllowance": 100.25,
       |  "totalIncomeOnWhichTaxIsDue": 100.25,
       |  "giftExtender": 100.25,
       |  "extendedBR": 100.25,
       |  "payPensionsProfitAtBRT": 100.25,
       |  "incomeTaxOnPayPensionsProfitAtBRT": 100.25,
       |  "interestReceivedAtStartingRate": 100.25,
       |  "incomeTaxOnInterestReceivedAtStartingRate": 100.25,
       |  "dividendsAtBRT": 100.25,
       |  "incomeTaxOnDividendsAtBRT": 100.25,
       |  "dividendsAtHRT": 100.25,
       |  "incomeTaxOnDividendsAtHRT": 100.25,
       |  "totalIncomeOnWhichTaxHasBeenCharged": 100.25,
       |  "incomeTaxCharged": 100.25,
       |  "taxCreditsOnDividendsFromUkCompanies": 100.25,
       |  "incomeTaxDueAfterDividendTaxCredits": 100.25,
       |  "highIncomeChildBenefitCharge": 100.25,
       |  "incomeTaxDue": 100.25,
       |  "employmentsPensionsAndBenefits": 100.25,
       |  "totalTaxDeducted": 100.25,
       |  "incomeTaxDueAfterDeductions": 100.25
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
