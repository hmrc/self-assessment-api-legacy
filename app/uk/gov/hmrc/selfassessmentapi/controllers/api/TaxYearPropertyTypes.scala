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

package uk.gov.hmrc.selfassessmentapi.controllers.api

import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs

trait TaxYearPropertyTypes {
  val featureSwitch: FeatureSwitch

  def types = Seq(PensionContributions, CharitableGivings, BlindPersons,
    TaxRefundedOrSetOffs, StudentLoans, ChildBenefits).filter(featureSwitch.isEnabled)

  def fromName(name: String): Option[TaxYearPropertyType] = types.find(_.name == name)
}

object TaxYearPropertyTypes extends TaxYearPropertyTypes {
  override val featureSwitch = FeatureSwitch(AppContext.featureSwitch)
}