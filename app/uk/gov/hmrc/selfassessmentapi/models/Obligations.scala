/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models

import com.github.nscala_time.time.OrderingImplicits
import org.joda.time.LocalDate
import play.api.libs.json.{Json, Writes}

import scala.util.{Failure, Success, Try}

case class Obligations(obligations: Seq[Obligation])

object Obligations {
  implicit val writes: Writes[Obligations] = Json.writes[Obligations]
}

case class Obligation(start: LocalDate, end: LocalDate, due: LocalDate, met: Boolean)

object Obligation {

  implicit val from =  new DesTransformValidator[des.ObligationDetail, Obligation] {
    def from(desObligation: des.ObligationDetail) = {
      Try(Obligation(
        start = LocalDate.parse(desObligation.inboundCorrespondenceFromDate),
        end = LocalDate.parse(desObligation.inboundCorrespondenceToDate),
        due = LocalDate.parse(desObligation.inboundCorrespondenceDueDate),
        met = desObligation.isFinalised)
      ) match {
        case Success(obj) => Right(obj)
        case Failure(ex) => Left(InvalidDateError(s"Unable to parse the date from des response $ex"))
      }
    }
  }

  implicit val localDateOrder: Ordering[LocalDate] = OrderingImplicits.LocalDateOrdering
  implicit val ordering: Ordering[Obligation] = Ordering.by(_.start)

  implicit val writes: Writes[Obligation] = Json.writes[Obligation]
}
