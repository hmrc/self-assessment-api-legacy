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

package uk.gov.hmrc.selfassessmentapi.domain.studentsloan

import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain.{BaseDomain, PositiveMonetaryFieldDescription, TaxYearPropertyType}
import uk.gov.hmrc.selfassessmentapi.domain.studentsloan.StudentLoanPlanType.StudentLoanPlanType

object StudentLoanPlanType extends Enumeration {
  type StudentLoanPlanType = Value
  val Plan1, Plan2 = Value
}

case class StudentLoan(planType: StudentLoanPlanType, deductedByEmployers: BigDecimal)

object StudentLoan extends BaseDomain[StudentLoan] {

  implicit val format = EnumJson.enumFormat(StudentLoanPlanType, Some("Student Loan Plan type is invalid"))
  override implicit val writes = Json.writes[StudentLoan]
  override implicit val reads = (
      (__ \ "planType").read[StudentLoanPlanType] and
      (__ \ "deductedByEmployers").read[BigDecimal](positiveAmountValidator("deductedByEmployers"))
    ) (StudentLoan.apply _)

  override def example(id: Option[String]) = StudentLoan(StudentLoanPlanType.Plan1, 2000)
}

case object StudentLoanType extends TaxYearPropertyType {
  override val name: String = "student-loan"
  override val example: JsValue = toJson(StudentLoan.example())
  override def description(action: String): String = s"$action a student loan"
  override val title: String = "Sample student loan"
  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, "planType"),
    PositiveMonetaryFieldDescription(name, "deductedByEmployers")
  )
}
