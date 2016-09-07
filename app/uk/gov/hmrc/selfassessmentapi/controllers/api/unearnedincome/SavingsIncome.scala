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

package uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonMarshaller
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson.enumFormat
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.SavingsIncomeType.SavingsIncomeType

object SavingsIncomeType extends Enumeration {
  type SavingsIncomeType = Value
  val InterestFromBanksTaxed, InterestFromBanksUntaxed = Value
  implicit val format = enumFormat(SavingsIncomeType, Some("Unearned Income Savings Income type is invalid"))
}

case class SavingsIncome(id: Option[String] = None, `type`: SavingsIncomeType, amount: BigDecimal)

object SavingsIncome extends JsonMarshaller[SavingsIncome] {

  implicit val writes = Json.writes[SavingsIncome]

  implicit val reads: Reads[SavingsIncome] = (
    Reads.pure(None) and
      (__ \ "type").read[SavingsIncomeType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (SavingsIncome.apply _)

  override def example(id: Option[SummaryId]) = SavingsIncome(id, SavingsIncomeType.InterestFromBanksTaxed, BigDecimal(1000.00))
}
