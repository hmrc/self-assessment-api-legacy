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

trait FieldDescription {
  val source: String
  val name: String
  val `type`: String
  def description: String
  val example: String
  val optional: Boolean
}

case class PositiveMonetaryFieldDescription(source: String, name: String, optional: Boolean = false) extends FieldDescription {
  val `type` = "Monetary"
  val example = "100.00"
  val description = "Monetary amount as a positive number with up to 2 decimal places."
}

case class MonetaryFieldDescription(source: String, name: String, optional: Boolean = false) extends FieldDescription {
  val `type` = "Monetary"
  val example = "-100.00"
  val description = "Monetary amount as a number with up to 2 decimal places."
}

case class FullFieldDescription(source: String, name: String, `type`: String, example: String, description: String, optional: Boolean = false) extends FieldDescription

trait Documentable {
  val title: String
  def description(action: String): String
  val fieldDescriptions: Seq[FieldDescription] = Nil
}
