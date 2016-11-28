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

package uk.gov.hmrc.selfassessmentapi.resources.models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class SelfEmploymentAdjustments(includedNonTaxableProfits: Option[BigDecimal] = None,
                                     basisAdjustment: Option[BigDecimal] = None,
                                     overlapReliefUsed: Option[BigDecimal] = None,
                                     accountingAdjustment: Option[BigDecimal] = None,
                                     averagingAdjustment: Option[BigDecimal] = None,
                                     lossBroughtForward: Option[BigDecimal] = None,
                                     outstandingBusinessIncome: Option[BigDecimal] = None)

object SelfEmploymentAdjustments {
  implicit val writes: Writes[SelfEmploymentAdjustments] = Json.writes[SelfEmploymentAdjustments]

  implicit val reads: Reads[SelfEmploymentAdjustments] = (
    (__ \ "includedNonTaxableProfits").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "basisAdjustment").readNullable[BigDecimal](amountValidator) and
      (__ \ "overlapReliefUsed").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "accountingAdjustment").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "averagingAdjustment").readNullable[BigDecimal](amountValidator) and
      (__ \ "lossBroughtForward").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "outstandingBusinessIncome").readNullable[BigDecimal](positiveAmountValidator)
    ) (SelfEmploymentAdjustments.apply _)

  lazy val example = SelfEmploymentAdjustments(
    includedNonTaxableProfits = Some(BigDecimal(50.00)),
    basisAdjustment = Some(BigDecimal(20.10)),
    overlapReliefUsed = Some(BigDecimal(500.00)),
    accountingAdjustment = Some(BigDecimal(10.50)),
    averagingAdjustment = Some(BigDecimal(-400.99)),
    lossBroughtForward = Some(BigDecimal(10000.00)),
    outstandingBusinessIncome = Some(BigDecimal(50.00)))
}
