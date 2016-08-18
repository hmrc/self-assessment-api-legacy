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
import uk.gov.hmrc.selfassessmentapi.controllers.{LiabilityCalculationError, LiabilityError}
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes._
import uk.gov.hmrc.selfassessmentapi.domain.{Liability, LiabilityId, SourceType, TaxYear, _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoEmployment, MongoLiability, MongoSelfEmployment, MongoUnearnedIncome, _}
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.repositories.{SelfAssessmentMongoRepository, SelfAssessmentRepository}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LiabilityService(employmentRepo: EmploymentMongoRepository,
                       selfEmploymentRepo: SelfEmploymentMongoRepository,
                       unearnedIncomeRepo: UnearnedIncomeMongoRepository,
                       furnishedHolidayLettingsRepo: FurnishedHolidayLettingsMongoRepository,
                       liabilityRepo: LiabilityMongoRepository,
                       ukPropertiesRepo: UKPropertiesMongoRepository,
                       selfAssessmentRepository: SelfAssessmentMongoRepository,
                       liabilityCalculator: LiabilityCalculator,
                       featureSwitch: FeatureSwitch) {

  def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[Either[LiabilityCalculationError, Liability]]] = {
    liabilityRepo
      .findBy(saUtr, taxYear)
      .map(_.map {
        case calculationError: CalculationError =>
          Left(LiabilityCalculationError(calculationError.errors.map(error =>
                        LiabilityError(error.code, error.message))))
        case liability: MongoLiability => Right(liability.toLiability)
      })
  }

  def calculate(saUtr: SaUtr, taxYear: TaxYear): Future[Either[CalculationErrorId, LiabilityId]] = {
    for {
      employments <- if (isSourceEnabled(Employments)) employmentRepo.findAll(saUtr, taxYear) else Future.successful(Seq[MongoEmployment]())
      selfEmployments <- if (isSourceEnabled(SelfEmployments)) selfEmploymentRepo.findAll(saUtr, taxYear) else Future.successful(Seq[MongoSelfEmployment]())
      unearnedIncomes <- if (isSourceEnabled(UnearnedIncomes)) unearnedIncomeRepo.findAll(saUtr, taxYear) else Future.successful(Seq[MongoUnearnedIncome]())
      ukProperties <- if (isSourceEnabled(UKProperties)) ukPropertiesRepo.findAll(saUtr, taxYear) else Future.successful(Seq[MongoUKProperties]())
      taxYearProperties <- selfAssessmentRepository.findTaxYearProperties(saUtr, taxYear)
      furnishedHolidayLettings <- if (isSourceEnabled(FurnishedHolidayLettings)) furnishedHolidayLettingsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[MongoFurnishedHolidayLettings]())
      emptyLiability <- liabilityRepo.save(MongoLiability.create(saUtr, taxYear))
      liabilityResult = calculateLiability(emptyLiability, employments, selfEmployments, ukProperties, unearnedIncomes, furnishedHolidayLettings)
      liability <- liabilityRepo.save(liabilityResult)
    } yield
      liability match {
        case calculationError: CalculationError => Left(calculationError.calculationErrorId)
        case liability: MongoLiability => Right(liability.liabilityId)
      }
  }

  private[calculation] def isSourceEnabled(sourceType: SourceType) = featureSwitch.isEnabled(sourceType)

  private def calculateLiability(liability: MongoLiability,
                                 employments: Seq[MongoEmployment],
                                 selfEmployments: Seq[MongoSelfEmployment],
                                 ukProperties: Seq[MongoUKProperties],
                                 unearnedIncomes: Seq[MongoUnearnedIncome],
                                 furnishedHolidayLettings: Seq[MongoFurnishedHolidayLettings]): LiabilityResult = {
    liabilityCalculator.calculate(SelfAssessment(employments = employments,
                                                 selfEmployments = selfEmployments,
                                                 unearnedIncomes = unearnedIncomes,
                                                 ukProperties = ukProperties),
                                  liability)
  }
}

object LiabilityService {

  private lazy val service = new LiabilityService(EmploymentRepository(),
                                                  SelfEmploymentRepository(),
                                                  UnearnedIncomeRepository(),
                                                  FurnishedHolidayLettingsRepository(),
                                                  LiabilityRepository(),
                                                  UKPropertiesRepository(),
                                                  SelfAssessmentRepository(),
                                                  LiabilityCalculator(),
                                                  FeatureSwitch(AppContext.featureSwitch))

  def apply() = service
}
