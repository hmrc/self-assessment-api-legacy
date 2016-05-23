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

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncomeType.SelfEmploymentIncomeType

object SelfEmploymentIncomeType extends Enumeration {
  type SelfEmploymentIncomeType = Value
  val Turnover, Other = Value
}


case class SelfEmploymentIncome(id: Option[SummaryId] = None,
                                `type`: SelfEmploymentIncomeType,
                                amount: BigDecimal)

object SelfEmploymentIncome {

  implicit val seIncomeTypes = EnumJson.enumFormat(SelfEmploymentIncomeType, Some("Self Employment Income type is invalid"))
  implicit val seIncomeWrites = Json.writes[SelfEmploymentIncome]
  implicit val seIncomeReads: Reads[SelfEmploymentIncome] = (
    Reads.pure(None) and
      (__ \ "type").read[SelfEmploymentIncomeType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (SelfEmploymentIncome.apply _)

  lazy val example: SelfEmploymentIncome = SelfEmploymentIncome(None, SelfEmploymentIncomeType.Turnover, BigDecimal(1000))
}
