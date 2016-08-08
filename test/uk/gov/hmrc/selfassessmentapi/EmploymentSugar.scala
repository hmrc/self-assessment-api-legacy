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

package uk.gov.hmrc.selfassessmentapi

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.employment.BenefitType.BenefitType
import uk.gov.hmrc.selfassessmentapi.domain.employment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.domain.employment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.domain.{EmploymentIncome => _, SelfEmploymentIncome => _, _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

trait EmploymentSugar {

  this: UnitSpec =>

  def anEmployment(id: SourceId = BSONObjectID.generate.stringify, saUtr: SaUtr = generateSaUtr(), taxYear: TaxYear = taxYear) = MongoEmployment(BSONObjectID.generate, id, saUtr, taxYear, now, now)

  def income(`type`: IncomeType, amount: BigDecimal) = MongoEmploymentIncomeSummary(BSONObjectID.generate.stringify, `type`, amount)

  def expense(`type`: ExpenseType, amount: BigDecimal) = MongoEmploymentExpenseSummary(BSONObjectID.generate.stringify, `type`, amount)

  def benefit(`type`: BenefitType, amount: BigDecimal) = MongoEmploymentBenefitSummary(BSONObjectID.generate.stringify, `type`, amount)

  private def now = DateTime.now(DateTimeZone.UTC)
}
