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

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.{Liability, LiabilityId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoLiability, MongoSelfEmployment}
import uk.gov.hmrc.selfassessmentapi.repositories.live.{LiabilityMongoRepository, LiabilityRepository, SelfEmploymentMongoRepository, SelfEmploymentRepository}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LiabilityService(selfEmploymentRepository: SelfEmploymentMongoRepository, liabilityRepository: LiabilityMongoRepository) {

  private val calculationSteps = Seq(
    SelfEmploymentProfitCalculation,
    TotalIncomeCalculation
  )

  def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[Liability]] = {
    liabilityRepository.findBy(saUtr, taxYear).map(_.map(_.toLiability))
  }

  def calculate(saUtr: SaUtr, taxYear: TaxYear): Future[LiabilityId] = {
    for {
      liability <- liabilityRepository.save(MongoLiability.create(saUtr, taxYear))
      selfEmployments <- selfEmploymentRepository.findAll(saUtr, taxYear)
      _ <- calculateLiability(liability, selfEmployments)
    } yield liability.liabilityId
  }

  private def calculateLiability(liability: MongoLiability, selfEmployments: Seq[MongoSelfEmployment]) = {

    val selfAssessment = SelfAssessment(selfEmployments = selfEmployments)

    val updatedLiability = calculationSteps.foldLeft(liability)((liability, step) => step.run(selfAssessment, liability))

    liabilityRepository.save(updatedLiability)
  }
}

object LiabilityService {

  private lazy val service = new LiabilityService(SelfEmploymentRepository(), LiabilityRepository())

  def apply() = service
}