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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.{BlindPerson, BlindPersons}
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.{CharitableGiving, CharitableGivings}
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.{ChildBenefit, ChildBenefits}
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionContributions}
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.{StudentLoan, StudentLoans}
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.{TaxRefundedOrSetOff, TaxRefundedOrSetOffs}


case class TaxYearProperties(id: Option[String] = None, pensionContributions: Option[PensionContribution] = None,
                             charitableGivings: Option[CharitableGiving] = None,
                             blindPerson: Option[BlindPerson] = None,
                             studentLoan: Option[StudentLoan] = None,
                             taxRefundedOrSetOff: Option[TaxRefundedOrSetOff] = None,
                             childBenefit: Option[ChildBenefit] = None) {

  def retirementAnnuityContract = pensionContributions.map(_.retirementAnnuityContract)
}

object TaxYearProperties extends JsonMarshaller[TaxYearProperties] {

  /**
    This is prevent newing up in a TaxYearProperties object in the test!!!
    We need to separate documentation generation code from domain; the documentation
    generation code must be a function of domain and embedded in the domain
   */
  private val featureSwitch = FeatureSwitch(AppContext.featureSwitch)
  override implicit val writes = Json.writes[TaxYearProperties]

  override implicit val reads = (
    Reads.pure(None) and
      (__ \ "pensionContributions").readNullable[PensionContribution] and
      (__ \ "charitableGivings").readNullable[CharitableGiving] and
      (__ \ "blindPerson").readNullable[BlindPerson] and
      (__ \ "studentLoan").readNullable[StudentLoan] and
      (__ \ "taxRefundedOrSetOff").readNullable[TaxRefundedOrSetOff] and
      (__ \ "childBenefit").readNullable[ChildBenefit]
    ) (TaxYearProperties.apply _)

  def atLeastOnePropertyIsEnabled: Boolean =
    featureSwitch.isEnabled(PensionContributions) ||
    featureSwitch.isEnabled(CharitableGivings) ||
    featureSwitch.isEnabled(BlindPersons) ||
    featureSwitch.isEnabled(StudentLoans) ||
    featureSwitch.isEnabled(TaxRefundedOrSetOffs) ||
    featureSwitch.isEnabled(ChildBenefits)

  override def example(id: Option[String]) =
    TaxYearProperties(
      id = id,
      pensionContributions = if(featureSwitch.isEnabled(PensionContributions)) Some(PensionContribution.example()) else None,
      charitableGivings = if(featureSwitch.isEnabled(CharitableGivings)) Some(CharitableGiving.example()) else None,
      blindPerson = if(featureSwitch.isEnabled(BlindPersons)) Some(BlindPerson.example()) else None,
      studentLoan = if(featureSwitch.isEnabled(StudentLoans)) Some(StudentLoan.example()) else None,
      taxRefundedOrSetOff = if(featureSwitch.isEnabled(TaxRefundedOrSetOffs)) Some(TaxRefundedOrSetOff.example()) else None,
      childBenefit = if(featureSwitch.isEnabled(ChildBenefits)) Some(ChildBenefit.example()) else None
    )
}
