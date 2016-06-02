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

package uk.gov.hmrc.selfassessmentapi.domain.unearnedincome

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.DividendType.DividendType

object DividendType extends Enumeration {
  type DividendType = Value
  val FromUKCompanies, OtherFromUkCompanies = Value
}

case class Dividend(id: Option[String] = None, `type`: DividendType, amount: BigDecimal)

object Dividend extends BaseDomain[Dividend] {

  implicit val savingsIncomeTypes = EnumJson.enumFormat(DividendType, Some("Dividend type is invalid"))
  implicit val writes = Json.writes[Dividend]

  implicit val reads: Reads[Dividend] = (
    Reads.pure(None) and
      (__ \ "type").read[DividendType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (Dividend.apply _)

  override def example(id: Option[SummaryId] = None) = Dividend(id, DividendType.FromUKCompanies, BigDecimal(1000.00))
}
