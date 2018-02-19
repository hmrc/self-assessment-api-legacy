/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.des

import play.api.libs.json.{Json, Reads}

case class Obligations(obligations: Seq[Obligation])

object Obligations {
  implicit val reads: Reads[Obligations] = Json.reads[Obligations]
}

case class Obligation(id: Option[String], `type`: String, details: Seq[ObligationDetail])

object Obligation {
  implicit val reads: Reads[Obligation] = Json.reads[Obligation]
}

case class ObligationDetail(status: String,
                            inboundCorrespondenceFromDate: String,
                            inboundCorrespondenceToDate: String,
                            inboundCorrespondenceDateReceived: Option[String],
                            inboundCorrespondenceDueDate: String,
                            periodKey: Option[String]) {
  def isFinalised: Boolean = status == "F"
}

object ObligationDetail {
  implicit val reads: Reads[ObligationDetail] = Json.reads[ObligationDetail]
}

case class ObligationsNew(obligations: Seq[ObligationNew])

object ObligationsNew {
  implicit val reads: Reads[ObligationsNew] = Json.reads[ObligationsNew]
}

case class ObligationNew(identification: ObligationIdentification, obligationDetails: Seq[ObligationDetail])

object ObligationNew {
  implicit val reads: Reads[ObligationNew] = Json.reads[ObligationNew]
}

case class ObligationIdentification(incomeSourceType: String, referenceNumber: String, referenceType: String)

object ObligationIdentification {
  implicit val reads: Reads[ObligationIdentification] = Json.reads[ObligationIdentification]
}