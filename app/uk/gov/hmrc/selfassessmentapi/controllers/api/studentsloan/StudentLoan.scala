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

package uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan

import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoanPlanType.StudentLoanPlanType
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson

object StudentLoanPlanType extends Enumeration {
  type StudentLoanPlanType = Value
  val Plan1, Plan2 = Value
}

case class StudentLoan(planType: StudentLoanPlanType, deductedByEmployers: Option[BigDecimal])

object StudentLoan extends JsonMarshaller[StudentLoan] {

  implicit val format = EnumJson.enumFormat(StudentLoanPlanType, Some("Student Loan Plan type is invalid"))
  override implicit val writes = Json.writes[StudentLoan]
  override implicit val reads = (
      (__ \ "planType").read[StudentLoanPlanType] and
      (__ \ "deductedByEmployers").readNullable[BigDecimal](positiveAmountValidator("deductedByEmployers"))
    ) (StudentLoan.apply _)

  override def example(id: Option[String]) = StudentLoan(StudentLoanPlanType.Plan1, Some(2000.0))
}

case object StudentLoans extends TaxYearPropertyType {
  override val name: String = "studentLoan"
  override val documentationName = "Student Loan"
  override val example: JsValue = toJson(StudentLoan.example())
  override def description(action: String): String = s"$action a student loan"
  override val title: String = "Sample student loan"
  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, "planType", "The plan type of taxpayer's Student Loan"),
    PositiveMonetaryFieldDescription(name, "deductedByEmployers", "Amount of Student Loan repayments deducted by taxpayer's employer", optional = true)
  )
}
