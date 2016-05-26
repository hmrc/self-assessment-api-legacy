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

package uk.gov.hmrc.selfassessmentapi.domain.selfemployment

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.domain._

case class SelfEmployment(id: Option[SelfEmploymentId] = None,
                          name: String,
                          commencementDate: LocalDate,
                          allowances: Option[Allowances] = None,
                          adjustments: Option[Adjustments] = None)

object SelfEmployment {

  implicit val selfEmploymentWrites = Json.writes[SelfEmployment]

  def commencementDateValidator = Reads.of[LocalDate].filter(ValidationError("commencement date should be in the past", COMMENCEMENT_DATE_NOT_IN_THE_PAST))(_.isBefore(LocalDate.now()))

  implicit val selfEmploymentReads: Reads[SelfEmployment] = (
    Reads.pure(None) and
      (__ \ "name").read[String](lengthValidator) and
      (__ \ "commencementDate").read[LocalDate](commencementDateValidator) and
      (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments]
    ) (SelfEmployment.apply _)


  lazy val example: SelfEmployment = SelfEmployment(
    id = None,
    name = "name",
    commencementDate = LocalDate.parse("2016-01-01"),
    allowances = Some(Allowances.example),
    adjustments = Some(Adjustments.example))
}
