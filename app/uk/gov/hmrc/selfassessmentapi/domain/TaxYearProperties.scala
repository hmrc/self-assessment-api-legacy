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
import play.api.libs.json.Json._
import play.api.libs.json._

case class PensionContribution(ukRegisteredPension: Option[BigDecimal] = None,
                               retirementAnnuity: Option[BigDecimal] = None,
                               employerScheme: Option[BigDecimal] = None,
                               overseasPensions: Option[BigDecimal] = None)

object PensionContribution extends BaseDomain[PensionContribution] {

  override implicit val writes = Json.writes[PensionContribution]

  val ukRegisteredPension = "ukRegisteredPension"
  val retirementAnnuity = "retirementAnnuity"
  val employerScheme = "employerScheme"
  val overseasPensions = "overseasPensions"

  override implicit val reads = (
    (__ \ ukRegisteredPension).readNullable[BigDecimal](positiveAmountValidator("ukRegisteredPension")) and
      (__ \ retirementAnnuity).readNullable[BigDecimal](positiveAmountValidator("retirementAnnuity")) and
      (__ \ employerScheme).readNullable[BigDecimal](positiveAmountValidator("employerScheme")) and
      (__ \ overseasPensions).readNullable[BigDecimal](positiveAmountValidator("overseasPensions"))
    ) (PensionContribution.apply _)

  override def example(id: Option[String] = None) =
    PensionContribution(
      ukRegisteredPension = Some(1000.45),
      retirementAnnuity = Some(1000.00),
      employerScheme = Some(12000.05),
      overseasPensions = Some(1234.43))
}

case object PensionContributions extends TaxYearPropertyType {
  override val name: String = "pension-contributions"
  override val example: JsValue = toJson(PensionContribution.example())

  override def description(action: String): String = s"$action a pension-contribution"

  override val title: String = "Sample pension contributions"

  import PensionContribution._

  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, ukRegisteredPension, optional = true),
    PositiveMonetaryFieldDescription(name, retirementAnnuity, optional = true),
    PositiveMonetaryFieldDescription(name, employerScheme, optional = true),
    PositiveMonetaryFieldDescription(name, overseasPensions, optional = true)
  )
}

case class TaxYearProperties(id: Option[String] = None, pensionContributions: Option[PensionContribution] = None)

object TaxYearProperties extends BaseDomain[TaxYearProperties] {

  override implicit val writes = Json.writes[TaxYearProperties]

  override implicit val reads = (
    Reads.pure(None) and
      (__ \ "pensionContributions").readNullable[PensionContribution]
    ) (TaxYearProperties.apply _)

  override def example(id: Option[String] = None) = TaxYearProperties(pensionContributions = Some(PensionContribution.example()))

}
