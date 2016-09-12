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

import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYearProperties, TaxYearPropertyType}

trait SwitchedTaxYearProperties {
  val featureSwitch: FeatureSwitch

  def taxYearPropertyIsEnabled(propertyType: TaxYearPropertyType): Boolean = {
    featureSwitch.isEnabled(propertyType)
  }

  def switchedTaxYearProperties(properties: TaxYearProperties): TaxYearProperties = {
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
