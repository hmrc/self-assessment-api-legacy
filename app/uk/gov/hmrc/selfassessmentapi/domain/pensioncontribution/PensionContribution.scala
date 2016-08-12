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

package uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain.{JsonMarshaller, _}

case class PensionContribution(ukRegisteredPension: Option[BigDecimal] = None,
                               retirementAnnuity: Option[BigDecimal] = None,
                               employerScheme: Option[BigDecimal] = None,
                               overseasPension: Option[BigDecimal] = None) {

  def retirementAnnuityContract: BigDecimal = {
    Sum(retirementAnnuity, employerScheme, overseasPension)
  }
}

object PensionContribution extends JsonMarshaller[PensionContribution] {

  override implicit val writes = Json.writes[PensionContribution]

  override implicit val reads = (
    (__ \ "ukRegisteredPension").readNullable[BigDecimal](positiveAmountValidator("ukRegisteredPension")) and
      (__ \ "retirementAnnuity").readNullable[BigDecimal](positiveAmountValidator("retirementAnnuity")) and
      (__ \ "employerScheme").readNullable[BigDecimal](positiveAmountValidator("employerScheme")) and
      (__ \ "overseasPension").readNullable[BigDecimal](positiveAmountValidator("overseasPension"))
    ) (PensionContribution.apply _)

  override def example(id: Option[String] = None) =
    PensionContribution(
      ukRegisteredPension = Some(1000.45),
      retirementAnnuity = Some(1000.00),
      employerScheme = Some(12000.05),
      overseasPension = Some(1234.43))
}
