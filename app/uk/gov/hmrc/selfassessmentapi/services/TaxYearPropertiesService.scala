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

package uk.gov.hmrc.selfassessmentapi.services

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.domain.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.domain.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.domain.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.domain.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.domain.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.domain.{TaxYear, TaxYearProperties, TaxYearPropertyType}
import uk.gov.hmrc.selfassessmentapi.repositories.{SelfAssessmentMongoRepository, SelfAssessmentRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxYearPropertiesService(saRepository: SelfAssessmentMongoRepository, featureSwitch: FeatureSwitch) {

  def taxYearPropertyIsEnabled(propertyType: TaxYearPropertyType): Boolean = {
    featureSwitch.isEnabled(propertyType)
  }

  def findTaxYearProperties(saUtr: SaUtr, taxYear: TaxYear): Future[Option[TaxYearProperties]] = {
    for {
      propertiesOption <- saRepository.findTaxYearProperties(saUtr, taxYear)
    } yield for {
      properties <- propertiesOption
    } yield switchedTaxYearProperties(properties)
  }

  def updateTaxYearProperties(saUtr: SaUtr, taxYear: TaxYear, taxYearProperties: TaxYearProperties): Future[Unit] = {
    val switchedProperties = switchedTaxYearProperties(taxYearProperties)

    saRepository.updateTaxYearProperties(saUtr, taxYear, switchedProperties)
  }

  private def switchedTaxYearProperties(properties: TaxYearProperties): TaxYearProperties = {
    val pensionContributions = if (taxYearPropertyIsEnabled(PensionContributions)) properties.pensionContributions else None
    val charitableGivings = if (taxYearPropertyIsEnabled(CharitableGivings)) properties.charitableGivings else None
    val blindPersons = if (taxYearPropertyIsEnabled(BlindPersons)) properties.blindPerson else None
    val studentLoans = if (taxYearPropertyIsEnabled(StudentLoans)) properties.studentLoan else None
    val taxRefundedOrSetOffs = if (taxYearPropertyIsEnabled(TaxRefundedOrSetOffs)) properties.taxRefundedOrSetOff else None
    val childBenefits = if (taxYearPropertyIsEnabled(ChildBenefits)) properties.childBenefit else None

    TaxYearProperties(pensionContributions = pensionContributions, charitableGivings = charitableGivings, blindPerson = blindPersons,
      studentLoan = studentLoans, taxRefundedOrSetOff = taxRefundedOrSetOffs, childBenefit = childBenefits)
  }
}

object TaxYearPropertiesService {
  private val taxYearPropertiesService = new TaxYearPropertiesService(SelfAssessmentRepository(),
                                                                      FeatureSwitch(AppContext.featureSwitch))

  def apply() = taxYearPropertiesService
}
