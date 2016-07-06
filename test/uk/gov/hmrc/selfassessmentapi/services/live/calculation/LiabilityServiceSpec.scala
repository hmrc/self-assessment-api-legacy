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
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability
import uk.gov.hmrc.selfassessmentapi.repositories.live.{LiabilityMongoRepository, SelfEmploymentMongoRepository}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.{MongoEmbeddedDatabase, SelfEmploymentSugar}

import scala.concurrent.ExecutionContext.Implicits.global

class LiabilityServiceSpec extends MongoEmbeddedDatabase with BeforeAndAfterEach with MockitoSugar with SelfEmploymentSugar {

  private val saUtr = generateSaUtr()
  private val liabilityRepo = new LiabilityMongoRepository()
  private val selfEmploymentRepo = new SelfEmploymentMongoRepository()
  private val liabilityCalculator = mock[LiabilityCalculator]
  private val service = new LiabilityService(selfEmploymentRepo, liabilityRepo, liabilityCalculator)

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

      await(selfEmploymentRepo.insert(selfEmployment))

      val liabilityAfterCalculation = aLiability(saUtr, taxYear)

      when(liabilityCalculator.calculate(eqTo(SelfAssessment(Seq(selfEmployment))), any[MongoLiability])).thenReturn(liabilityAfterCalculation)

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
  }
}
