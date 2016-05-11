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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import java.util.UUID

import play.api.hal.HalLink
import play.api.libs.json.Json._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain._

object LiabilityController extends uk.gov.hmrc.selfassessmentapi.controllers.LiabilityController {

  override val context: String = AppContext.apiGatewayContext

  override def requestLiability(utr: SaUtr, taxPeriod: Option[String]) = Action { request =>
    val liabilityId = UUID.randomUUID().toString
      val links = Seq(
        HalLink("self", liabilityHref(utr, liabilityId))
      )
      Accepted(halResource(JsObject(Nil), links))
  }

  override def retrieveLiability(utr: SaUtr, liabilityId: String) = Action { request =>
    val liability = createLiability(utr, liabilityId)
    val links = Seq(
      HalLink("self", liabilityHref(utr, liabilityId))
    )
    Ok(halResource(Json.toJson(liability), links))
  }

  def createLiability(utr: SaUtr, id: String): Liability =
    Liability(
      id = Some(id),
      taxPeriod = "2015-16-Q1",
      income = Income(
        incomes = Seq(
          Amount("self-employment-profit", BigDecimal(92480)),
          Amount("uk-interest-received", BigDecimal(93)),
          Amount("uk-dividends", BigDecimal(466))
        ),
        totalIncomeReceived = BigDecimal(93039),
        personalAllowance = BigDecimal(9440),
        totalTaxableIncome = BigDecimal(83599)
      ),
      incomeTax = CalculatedAmount(
        calculations = Seq(
          Calculation("pay-pensions-profits", BigDecimal(32010), BigDecimal(20), BigDecimal(6402)),
          Calculation("pay-pensions-profits", BigDecimal(41030), BigDecimal(40), BigDecimal(16412)),
          Calculation("interest-received", BigDecimal(0), BigDecimal(10), BigDecimal(0)),
          Calculation("interest-received", BigDecimal(0), BigDecimal(20), BigDecimal(0)),
          Calculation("interest-received", BigDecimal(93), BigDecimal(40), BigDecimal(37.2)),
          Calculation("dividends", BigDecimal(0), BigDecimal(10), BigDecimal(0)),
          Calculation("dividends", BigDecimal(466), BigDecimal(32.5), BigDecimal(151.45))
        ),
        total = BigDecimal(23002.65)
      ),
      credits = Seq(
        Amount("dividend", BigDecimal(46.6)),
        Amount("interest-charged", BigDecimal(12.25))
      ),
      class4Nic = CalculatedAmount(
        calculations = Seq(
          Calculation("class-4-nic", BigDecimal(33695), BigDecimal(9), BigDecimal(3032.55)),
          Calculation("class-4-nic", BigDecimal(41030), BigDecimal(2), BigDecimal(820.60))
        ),
        total = BigDecimal(3853.15)
      ),
      totalTaxDue = BigDecimal(25796.95)
    )


  override def deleteLiability(utr: SaUtr, liabilityId: String) = Action { request =>
    NoContent
  }

  override def find(saUtr: SaUtr): Action[AnyContent] = Action { request =>
    val result= Seq(createLiability(saUtr, "1234"), createLiability(saUtr, "4321"), createLiability(saUtr, "7777"))
    val liabilities = toJson(
      result.map(liability => halResource(obj(), Seq(HalLink("self", liabilityHref(saUtr, liability.id.get)))))
    )
    Ok(halResourceList("liabilities", liabilities, liabilitiesHref(saUtr)))
  }
}
