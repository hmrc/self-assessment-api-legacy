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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.selfemployment

import uk.gov.hmrc.selfassessmentapi.controllers.SourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SummaryHandler
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Income, _}
import uk.gov.hmrc.selfassessmentapi.domain.{SummaryType, _}
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.SandboxSourceRepository

object SelfEmploymentSourceHandler extends SourceHandler(SelfEmployment, SelfEmployments.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(Incomes.name, Income))
      case Expenses => Some(SummaryHandler(Expenses.name, Expense))
      case BalancingCharges => Some(SummaryHandler(BalancingCharges.name, BalancingCharge))
      case GoodsAndServicesOwnUses => Some(SummaryHandler(GoodsAndServicesOwnUses.name, GoodsAndServicesOwnUse))
      case _ => None
    }
  }
  override val repository  = new SandboxSourceRepository[SelfEmployment] {
    override implicit val writes = SelfEmployment.writes
    override def example(id: SourceId) = SelfEmployment.example().copy(id = Some(id))

  }
}
