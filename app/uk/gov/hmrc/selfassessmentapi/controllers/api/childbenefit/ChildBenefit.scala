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

package uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{JsonMarshaller, ErrorCode}
import ErrorCode.VALUE_BELOW_MINIMUM
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case class ChildBenefit(amount: BigDecimal, numberOfChildren: Int, dateBenefitStopped: Option[LocalDate] = None)


object ChildBenefit extends JsonMarshaller[ChildBenefit] {
  override implicit val writes = Json.writes[ChildBenefit]
  override implicit val reads = (
    (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount")) and
    (__ \ "numberOfChildren").read[Int].filter(ValidationError("numberOfChildren must be greater than 0", VALUE_BELOW_MINIMUM))(_ >= 0) and
    (__ \ "dateBenefitStopped").readNullable[LocalDate]
  )(ChildBenefit.apply _).filter(ValidationError("If the amount is greater than 0, the numberOfChildren must also be greater than 0", VALUE_BELOW_MINIMUM))
   {benefit => if (benefit.amount > 0) benefit.numberOfChildren > 0 else true}

  override def example(id: Option[String]): ChildBenefit = ChildBenefit(1234.34, 3, Some(new LocalDate(2016, 4, 5)))
}
