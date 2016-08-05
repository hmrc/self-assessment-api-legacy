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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, TestApplication}

import scala.concurrent.ExecutionContext.Implicits.global

class LiabilityServiceSpec extends TestApplication with SelfEmploymentSugar {

  private val saUtr = generateSaUtr()
  private val liabilityRepo = new LiabilityMongoRepository()
  private val employmentRepo = new EmploymentMongoRepository
  private val selfEmploymentRepo = new SelfEmploymentMongoRepository()
  private val unearnedIncomeRepo = new UnearnedIncomeMongoRepository()
  private val liabilityCalculator = mock[LiabilityCalculator]
  private val service = new LiabilityService(employmentRepo, selfEmploymentRepo, unearnedIncomeRepo, liabilityRepo, liabilityCalculator)

  "find" should {

    "return liability for given utr and tax year" in {
      val mongoLiability = aLiability(saUtr, taxYear)
      await(liabilityRepo.save(mongoLiability))

      await(service.find(saUtr, taxYear)) shouldBe Some(mongoLiability.toLiability)
    }

    "return None if there is no liability for given utr and tax year" in {

      await(service.find(saUtr, taxYear)) shouldBe None
    }
  }

  "calculate" should {

    "create liability and trigger the calculation if liability for given utr and tax year does not exist" in {

      val selfEmployment = aSelfEmployment(saUtr = saUtr, taxYear = taxYear)
      val anUnearnedIncome = anUnearnedIncomes(saUtr = saUtr, taxYear = taxYear)

      await(selfEmploymentRepo.insert(selfEmployment))
      await(unearnedIncomeRepo.insert(anUnearnedIncome))

      val liabilityAfterCalculation = aLiability(saUtr, taxYear)

      when(liabilityCalculator.calculate(eqTo(SelfAssessment(selfEmployments = Seq(selfEmployment), unearnedIncomes = Seq(anUnearnedIncome))), any[MongoLiability])).thenReturn(liabilityAfterCalculation)

      await(service.calculate(saUtr, taxYear))

      await(liabilityRepo.findBy(saUtr, taxYear)) shouldBe Some(liabilityAfterCalculation)

      verify(liabilityCalculator).calculate(any[SelfAssessment], any[MongoLiability])
    }

    "replace existing liability and trigger the calculation if liability for given utr and tax year does exist" in {

      await(liabilityRepo.save(aLiability(saUtr, taxYear)))

      val liabilityAfterCalculation = aLiability(saUtr, taxYear).copy(totalIncomeReceived = Some(1000))

      when(liabilityCalculator.calculate(any[SelfAssessment], any[MongoLiability])).thenReturn(liabilityAfterCalculation)

      await(service.calculate(saUtr, taxYear))

      await(liabilityRepo.findBy(saUtr, taxYear)) shouldBe Some(liabilityAfterCalculation)
    }

    "get employment sources from repository when Employment source is switched on" in {
      val employmentRepo = mock[EmploymentMongoRepository]
      val service = spy(new LiabilityService(employmentRepo, selfEmploymentRepo, unearnedIncomeRepo, liabilityRepo, liabilityCalculator))

      when(service.isSourceEnabled(Employments)).thenReturn(true)
      when(employmentRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(employmentRepo).findAll(saUtr, taxYear)
    }

    "not get employment sources from repository when Employment source is switched off" in {
      val employmentRepo = mock[EmploymentMongoRepository]
      val service = spy(new LiabilityService(employmentRepo, selfEmploymentRepo, unearnedIncomeRepo, liabilityRepo, liabilityCalculator))

      when(service.isSourceEnabled(Employments)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(employmentRepo)
    }

    "get self employment sources from repository when Self Employment source is switched on" in {
      val selfEmploymentRepo = mock[SelfEmploymentMongoRepository]
      val service = spy(new LiabilityService(employmentRepo, selfEmploymentRepo, unearnedIncomeRepo, liabilityRepo, liabilityCalculator))

      when(service.isSourceEnabled(SelfEmployments)).thenReturn(true)
      when(selfEmploymentRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(selfEmploymentRepo).findAll(saUtr, taxYear)
    }

    "not get self employment sources from repository when Self Employment source is switched off" in {
      val selfEmploymentRepo = mock[SelfEmploymentMongoRepository]
      val service = spy(new LiabilityService(employmentRepo, selfEmploymentRepo, unearnedIncomeRepo, liabilityRepo, liabilityCalculator))

      when(service.isSourceEnabled(SelfEmployments)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(selfEmploymentRepo)
    }

    "get unearned incomes sources from repository when Unearned Incomes source is switched on" in {
      val unearnedIncomeRepo = mock[UnearnedIncomeMongoRepository]
      val service = spy(new LiabilityService(employmentRepo, selfEmploymentRepo, unearnedIncomeRepo, liabilityRepo, liabilityCalculator))

      when(service.isSourceEnabled(UnearnedIncomes)).thenReturn(true)
      when(unearnedIncomeRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(unearnedIncomeRepo).findAll(saUtr, taxYear)
    }

    "not get unearned incomes sources from repository when Unearned Incomes source is switched off" in {
      val unearnedIncomeRepo = mock[UnearnedIncomeMongoRepository]
      val service = spy(new LiabilityService(employmentRepo, selfEmploymentRepo, unearnedIncomeRepo, liabilityRepo, liabilityCalculator))

      when(service.isSourceEnabled(UnearnedIncomes)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(unearnedIncomeRepo)
    }
  }
}
