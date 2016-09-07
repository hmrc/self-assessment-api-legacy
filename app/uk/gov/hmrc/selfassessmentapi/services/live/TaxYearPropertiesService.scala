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

package uk.gov.hmrc.selfassessmentapi.services.live

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.domain.{TaxYear, TaxYearProperties}
import uk.gov.hmrc.selfassessmentapi.repositories.{SelfAssessmentMongoRepository, SelfAssessmentRepository}
import uk.gov.hmrc.selfassessmentapi.services.SwitchedTaxYearProperties

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxYearPropertiesService(saRepository: SelfAssessmentMongoRepository, override val featureSwitch: FeatureSwitch)
  extends SwitchedTaxYearProperties {

  def findTaxYearProperties(saUtr: SaUtr, taxYear: TaxYear): Future[Option[TaxYearProperties]] = {
    for {
      propertiesOption <- saRepository.findTaxYearProperties(saUtr, taxYear)
    } yield for {
      properties <- propertiesOption
    } yield switchedTaxYearProperties(properties)
  }

  def updateTaxYearProperties(saUtr: SaUtr, taxYear: TaxYear, taxYearProperties: TaxYearProperties): Future[Boolean] = {
    val switchedProperties = switchedTaxYearProperties(taxYearProperties)

    // If nothing has been removed (i.e. switched off), update, otherwise return an error.
    val isValidProperties = switchedProperties == taxYearProperties
    if (isValidProperties) {
      saRepository.updateTaxYearProperties(saUtr, taxYear, switchedProperties)
    }

    Future.successful(isValidProperties)
  }
}

object TaxYearPropertiesService {
  private val taxYearPropertiesService = new TaxYearPropertiesService(SelfAssessmentRepository(),
                                                                      FeatureSwitch(AppContext.featureSwitch))

  def apply() = taxYearPropertiesService
}
