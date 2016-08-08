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

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain._

object SummaryTypes {

  case object SavingsIncomes extends SummaryType {
    override val name = "savings"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(SavingsIncome.example(id))
    override val title = "Sample unearned income savings incomes"

    override def description(action: String) = s"$action a savings income for the specified source"

    override val fieldDescriptions = Seq(
      FullFieldDescription("unearned income", "type", "Enum", "Type of savings income"),
      PositiveMonetaryFieldDescription("unearned income", "amount")
    )
  }

  case object Dividends extends SummaryType {
    override val name = "dividends"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Dividend.example(id))
    override val title = "Sample unearned income dividends"

    override def description(action: String) = s"$action a dividend for the specified source"

    override val fieldDescriptions = Seq(
      FullFieldDescription("unearned income", "type", "Enum", "Type of dividends income"),
      PositiveMonetaryFieldDescription("unearned income", "amount")
    )
  }

  case object Benefits extends SummaryType {
    override val name = "benefits"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Benefit.example(id))
    override val title = "Sample unearned income benefits"

    override def description(action: String) = s"$action a benefit for the specified source"

    override val fieldDescriptions = Seq(
      FullFieldDescription("unearned income", "type", "Enum", "Type of benefit"),
      PositiveMonetaryFieldDescription("unearned income", "amount")
    )
  }

}
