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
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.LiabilityCalculationError
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes._
import uk.gov.hmrc.selfassessmentapi.domain.{Liability, LiabilityId, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoEmployment, MongoLiability, MongoSelfEmployment, MongoUnearnedIncome}
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LiabilityService(employmentRepo: EmploymentMongoRepository,
                       selfEmploymentRepo: SelfEmploymentMongoRepository,
                       unearnedIncomeRepo: UnearnedIncomeMongoRepository,
                       liabilityRepo: LiabilityMongoRepository,
                       liabilityCalculator: LiabilityCalculator) {

  def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[Either[LiabilityCalculationError, Liability]]] = {
    liabilityRepo
      .findBy(saUtr, taxYear)
      .map(_.map { mongoLiability =>
        mongoLiability.calculationError match {
          case Some(error) => Left(LiabilityCalculationError(error.code, error.message))
          case None => Right(mongoLiability.toLiability)
        }
      })
  }

  def calculate(saUtr: SaUtr, taxYear: TaxYear): Future[LiabilityId] = {
    for {
      emptyLiability <- liabilityRepo.save(MongoLiability.create(saUtr, taxYear))
      employments <- if (isSourceEnabled(Employments)) employmentRepo.findAll(saUtr, taxYear)
                    else Future.successful(Seq[MongoEmployment]())
      selfEmployments <- if (isSourceEnabled(SelfEmployments)) selfEmploymentRepo.findAll(saUtr, taxYear)
                        else Future.successful(Seq[MongoSelfEmployment]())
      unearnedIncomes <- if (isSourceEnabled(UnearnedIncomes)) unearnedIncomeRepo.findAll(saUtr, taxYear)
                        else Future.successful(Seq[MongoUnearnedIncome]())
      liability <- liabilityRepo.save(
                      calculateLiability(emptyLiability, employments, selfEmployments, unearnedIncomes))
    } yield liability.liabilityId
  }

  private[calculation] def isSourceEnabled(sourceType: SourceType) =
    FeatureSwitch(AppContext.featureSwitch).isEnabled(sourceType)

  private def calculateLiability(liability: MongoLiability,
                                 employments: Seq[MongoEmployment],
                                 selfEmployments: Seq[MongoSelfEmployment],
                                 unearnedIncomes: Seq[MongoUnearnedIncome]): MongoLiability = {
    liabilityCalculator.calculate(SelfAssessment(employments = employments,
                                                 selfEmployments = selfEmployments,
                                                 unearnedIncomes = unearnedIncomes),
                                  liability)
  }
}

object LiabilityService {

  private lazy val service = new LiabilityService(EmploymentRepository(),
                                                  SelfEmploymentRepository(),
                                                  UnearnedIncomeRepository(),
                                                  LiabilityRepository(),
                                                  LiabilityCalculator())

  def apply() = service
}
