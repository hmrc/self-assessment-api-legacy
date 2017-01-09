/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.controllers.api.benefit

import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._


case class Benefit(id: Option[SourceId] = None)

object Benefit extends JsonMarshaller[Benefit] {

  implicit val writes = Json.writes[Benefit]

  implicit val reads = Reads.pure(None).map(Benefit(_))

  override def example(id : Option[SourceId]) = Benefit(id)
}
