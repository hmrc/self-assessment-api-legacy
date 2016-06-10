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

import uk.gov.hmrc.selfassessmentapi.domain.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.domain.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.domain.taxrefundedorsetoff.TaxRefundedOrSetOffs

object TaxYearPropertyTypes {
  val types = Seq(PensionContributions, CharitableGivings, BlindPersons, TaxRefundedOrSetOffs)
  private val typesByName = types.map(x => x.name -> x).toMap
  def fromName(name: String): Option[TaxYearPropertyType] = typesByName.get(name)
}
