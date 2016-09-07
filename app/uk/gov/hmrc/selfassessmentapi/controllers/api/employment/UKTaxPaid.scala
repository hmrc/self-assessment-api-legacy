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

package uk.gov.hmrc.selfassessmentapi.controllers.api.employment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SummaryId, JsonMarshaller}
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case class UkTaxPaid(id: Option[SummaryId] = None, amount: BigDecimal)

object UkTaxPaid extends JsonMarshaller[UkTaxPaid]{

  implicit val writes = Json.writes[UkTaxPaid]
  implicit val reads: Reads[UkTaxPaid] = (
    Reads.pure(None) and
      (__ \ "amount").read[BigDecimal](amountValidator("amount"))
    ) (UkTaxPaid.apply _)

  override def example(id: Option[SummaryId]) = UkTaxPaid(id, BigDecimal(10000.00))

}
