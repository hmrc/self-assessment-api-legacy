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

package uk.gov.hmrc.selfassessmentapi.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.domain.charitablegiving.CharitableGiving
import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.domain.taxrefundedorsetoff.TaxRefundedOrSetOff


case class TaxYearProperties(id: Option[String] = None, pensionContributions: Option[PensionContribution] = None,
                             charitableGivings: Option[CharitableGiving] = None,
                             blindPerson: Option[BlindPerson] = None,
                             taxRefundedOrSetOff: Option[TaxRefundedOrSetOff] = None)

object TaxYearProperties extends BaseDomain[TaxYearProperties] {

  override implicit val writes = Json.writes[TaxYearProperties]

  override implicit val reads = (
    Reads.pure(None) and
      (__ \ "pensionContributions").readNullable[PensionContribution] and
      (__ \ "charitableGivings").readNullable[CharitableGiving] and
      (__ \ "blindPerson").readNullable[BlindPerson] and
      (__ \ "taxRefundedOrSetOff").readNullable[TaxRefundedOrSetOff]
    ) (TaxYearProperties.apply _)

  override def example(id: Option[String] = None) = TaxYearProperties(pensionContributions = Some(PensionContribution.example()),
    charitableGivings = Some(CharitableGiving.example()),
    blindPerson = Some(BlindPerson.example()),
    taxRefundedOrSetOff = Some(TaxRefundedOrSetOff.example()))

}
