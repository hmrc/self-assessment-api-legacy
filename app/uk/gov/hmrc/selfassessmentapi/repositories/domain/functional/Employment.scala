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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoEmployment

object Employment {

  object TotalProfit {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.employments.map(Profit(_)).sum
  }

  object Profit {
    def apply(employment: MongoEmployment) = RoundDown(PositiveOrZero(Total(employment.incomes) + Total(employment.benefits) - Total(employment.expenses)))
  }

  object Incomes {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.employments.map { employment =>
      EmploymentIncome(sourceId = employment.sourceId, pay = Total(employment.incomes), benefitsAndExpenses = Total(employment.benefits),
        allowableExpenses = CapAt(Total(employment.expenses), Total(employment.incomes) + Total(employment.benefits)), total = Profit(employment))
    }
  }

  object TotalTaxPaid {
    def apply(selfAssessment: SelfAssessment) = PositiveOrZero(selfAssessment.employments.map(TaxPaid(_)).sum)
  }

  object TaxPaid {
    def apply(employment: MongoEmployment) = RoundUpToPennies(employment.ukTaxPaid.map(_.amount).sum)
  }

  object TaxesPaid {
    def apply(selfAssessment: SelfAssessment) = {
      selfAssessment.employments.map { employment =>
        UkTaxPaidForEmployment(employment.sourceId, Employment.TaxPaid(employment))
      }
    }
  }

}
