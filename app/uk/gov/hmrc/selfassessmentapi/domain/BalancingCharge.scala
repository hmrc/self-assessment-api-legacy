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
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain.BalancingChargeCategory.BalancingChargeCategory

object BalancingChargeCategory extends Enumeration {
  type BalancingChargeCategory = Value
  val BPRA, OTHER = Value
}

case class BalancingCharge(id: Option[String] = None, category: BalancingChargeCategory, amount: BigDecimal)

object BalancingCharge {

  implicit val balancingChargeCategory = EnumJson.enumFormat(BalancingChargeCategory)
  implicit val balancingChargeWrites = Json.writes[BalancingCharge]

  implicit val balancingChangeReads: Reads[BalancingCharge] = (
    Reads.pure(None) and
      (__ \ "category").read[BalancingChargeCategory] and
      (__ \ "amount").read[BigDecimal](amountValidator)
    ) (BalancingCharge.apply _)
}
