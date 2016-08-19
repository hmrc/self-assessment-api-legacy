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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.UnearnedIncomesSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.DividendsFromUKSources
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.DividendType._

class DividendsFromUKSourcesCalculationSpec extends UnitSpec {

  "run" should {

    "calculate dividends when there are no dividends from uk sources from unearned income source" in {

      val liability = aLiability()

      DividendsFromUKSourcesCalculation.run(SelfAssessment(), liability).getLiabilityOrFail shouldBe liability.copy(
          dividendsFromUKSources = Seq())
    }

    "calculate rounded down dividends when there are multiple dividends from uk sources from multiple unearned income source" in {

      val dividendUK1 = anUnearnedDividendIncomeSummary("dividendUK1", FromUKCompanies, 1000.50)
      val dividendOther1 = anUnearnedDividendIncomeSummary("dividendOtherUK1", OtherFromUKCompanies, 2000.99)

      val dividendUK2 = anUnearnedDividendIncomeSummary("dividendUK2", FromUKCompanies, 3000.50)
      val dividendOther2 = anUnearnedDividendIncomeSummary("dividendOtherUK2", OtherFromUKCompanies, 4000.999)

      val unearnedIncomes1 = income().copy(dividends = Seq(dividendUK1, dividendOther1))
      val unearnedIncomes2 = income().copy(dividends = Seq(dividendUK2, dividendOther2))

      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes1, unearnedIncomes2)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes1.sourceId, BigDecimal(3001)),
            DividendsFromUKSources(sourceId = unearnedIncomes2.sourceId, BigDecimal(7001)))
    }

    "calculate dividends when there is one uk dividend from a single unearned income source" in {
      val dividendUK = anUnearnedDividendIncomeSummary("dividendUK", FromUKCompanies, 1000)
      val unearnedIncomes = income().copy(dividends = Seq(dividendUK))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(1000)))
    }

    "calculate dividends when there are multiple uk dividends from a single unearned income source" in {
      val dividendUK1 = anUnearnedDividendIncomeSummary("dividendUK1", FromUKCompanies, 1000)
      val dividendUK2 = anUnearnedDividendIncomeSummary("dividendUK2", FromUKCompanies, 2000)
      val unearnedIncomes = income().copy(dividends = Seq(dividendUK1, dividendUK2))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(3000)))
    }

    "calculate round down dividends when there is one uk dividends from a single unearned income source" in {
      val dividendUK = anUnearnedDividendIncomeSummary("dividendUK", FromUKCompanies, 1000.50)
      val unearnedIncomes = income().copy(dividends = Seq(dividendUK))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(1000)))
    }

    "calculate round down dividends when there are uk dividends from a single unearned income source" in {
      val dividendUK1 = anUnearnedDividendIncomeSummary("dividendUK1", FromUKCompanies, 1000.90)
      val dividendUK2 = anUnearnedDividendIncomeSummary("dividendUK2", FromUKCompanies, 2000.99)
      val unearnedIncomes = income().copy(dividends = Seq(dividendUK1, dividendUK2))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(3001)))
    }

    "calculate dividends when there is one other uk dividends from a single unearned income source" in {
      val dividendOther = anUnearnedDividendIncomeSummary("dividendOther", OtherFromUKCompanies, 1000)
      val unearnedIncomes = income().copy(dividends = Seq(dividendOther))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(1000)))
    }

    "calculate dividends when there are multiple other uk dividends from a single unearned income source" in {
      val dividendOther1 = anUnearnedDividendIncomeSummary("dividendOther1", OtherFromUKCompanies, 1000)
      val dividendOther2 = anUnearnedDividendIncomeSummary("dividendOther2", OtherFromUKCompanies, 2000)
      val unearnedIncomes = income().copy(dividends = Seq(dividendOther1, dividendOther2))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(3000)))
    }

    "calculate rounded down dividends when there is one other uk dividends from a single unearned income source" in {
      val dividendOther = anUnearnedDividendIncomeSummary("dividendOther", OtherFromUKCompanies, 1000.50)
      val unearnedIncomes = income().copy(dividends = Seq(dividendOther))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(1000)))
    }

    "calculate rounded down dividends when there are multiple other uk dividends from a single unearned income source" in {
      val dividendOther1 = anUnearnedDividendIncomeSummary("dividendOther1", OtherFromUKCompanies, 1000.50)
      val dividendOther2 = anUnearnedDividendIncomeSummary("dividendOther2", OtherFromUKCompanies, 2000.99)
      val unearnedIncomes = income().copy(dividends = Seq(dividendOther1, dividendOther2))
      val liability = aLiability()

      DividendsFromUKSourcesCalculation
        .run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)), liability)
        .getLiabilityOrFail
        .dividendsFromUKSources shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncomes.sourceId, BigDecimal(3001)))
    }

  }
}
