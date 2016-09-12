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

package uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case class Income(id: Option[SummaryId] = None,
                  amount: BigDecimal)

object Income extends JsonMarshaller[Income] {

  implicit val writes = Json.writes[Income]
  implicit val reads: Reads[Income] = (
    Reads.pure(None) and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (Income.apply _)

  override def example(id: Option[SummaryId]) = Income(id, BigDecimal(1000))
}
