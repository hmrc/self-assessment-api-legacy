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

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncomeType.SelfEmploymentIncomeType


object SelfEmploymentIncomeType extends Enumeration {
  type SelfEmploymentIncomeType = Value
  val TURNOVER, OTHER = Value
}


case class SelfEmploymentIncome(id: Option[SelfEmploymentIncomeId] = None,
                                taxYear: String,
                                incomeType: SelfEmploymentIncomeType,
                                amount: BigDecimal)


object SelfEmploymentIncome {

  private def taxYearValidator = Reads.of[String].filter(ValidationError("tax year must be 2016-17", ErrorCode("TAX_YEAR_INVALID")))(_ == "2016-17")

  private def amountValidator = Reads.of[BigDecimal].filter(ValidationError("amount cannot have more than 2 decimal values", ErrorCode("INVALID_MONETARY_AMOUNT")))(_.scale < 3)

  implicit val seIncomeTypes = EnumJson.enumFormat(SelfEmploymentIncomeType)
  implicit val seIncomeWrites = Json.writes[SelfEmploymentIncome]
  implicit val seIncomeReads: Reads[SelfEmploymentIncome] = (
    (__ \ "id").readNullable[SelfEmploymentId] and
      (__ \ "taxYear").read[String](taxYearValidator) and
      (__ \ "incomeType").read[SelfEmploymentIncomeType] and
      (__ \ "amount").read[BigDecimal](amountValidator)
    ) (SelfEmploymentIncome.apply _)
}
