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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.employment

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.SourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SummaryHandler
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes.Employments
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.employment.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.domain.employment.{Benefit, Employment, Expense, Income, UKTaxPaid}
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.SandboxSourceRepository

import scala.concurrent.Future

object EmploymentsSourceHandler extends SourceHandler(Employment, Employments.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(Incomes.name, Income))
      case Expenses => Some(SummaryHandler(Expenses.name, Expense))
      case Benefits => Some(SummaryHandler(Benefits.name, Benefit))
      case UKTaxesPaid => Some(SummaryHandler(UKTaxesPaid.name, UKTaxPaid))
      case _ => None
    }
  }

  override val repository = new SandboxSourceRepository[Employment] {
    override def example(id: SourceId): Employment = Employment.example().copy(id = Some(id))
    override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[Employment]] = ???
  }
}
