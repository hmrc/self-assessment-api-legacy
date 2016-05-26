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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import play.api.libs.json.Json._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class ErrorResult(message: Option[String] = None, validationErrors: Option[ValidationErrors] = None)

trait SummaryHandler[T] {

  implicit val reads: Reads[T]
  implicit val writes: Writes[T]
  def example(id: SummaryId): T
  val listName: String

  private def generateId: String = BSONObjectID.generate.stringify

  def create(jsValue: JsValue): Future[Either[ErrorResult, SummaryId]] =
    Future.successful (
      Try(jsValue.validate[T]) match {
        case Success(JsSuccess(payload, _)) => Right(generateId)
        case Success(JsError(errors)) =>
          Left(ErrorResult(validationErrors = Some(errors)))
        case Failure(e) =>
          Left(ErrorResult(message = Some(s"could not parse body due to ${e.getMessage}")))
      }
    )

  def findById(summaryId: SummaryId): Future[Option[JsValue]] = {
    Future.successful(Some(toJson(example(summaryId))))
  }

  def find: Future[Seq[SummaryId]] =
    Future.successful(
      Seq(
        generateId,
        generateId,
        generateId,
        generateId,
        generateId
      )
    )

  def delete(summaryId: SummaryId): Future[Boolean] =
    Future.successful(true)

  def update(summaryId: SummaryId, jsValue: JsValue): Future[Either[ErrorResult, SummaryId]] =
    Future.successful (
      Try(jsValue.validate[T]) match {
        case Success(JsSuccess(payload, _)) => Right(summaryId)
        case Success(JsError(errors)) =>
          Left(ErrorResult(validationErrors = Some(errors)))
        case Failure(e) =>
          Left(ErrorResult(message = Some(s"could not parse body due to ${e.getMessage}")))
      }
    )
}

object IncomesSummaryHandler extends SummaryHandler[selfemployment.Income] {
  override implicit val reads: Reads[selfemployment.Income] = selfemployment.Income.reads
  override implicit val writes: Writes[selfemployment.Income] = selfemployment.Income.writes
  override def example(id: SummaryId) = selfemployment.Income.example.copy(id = Some(id))
  override val listName = selfemployment.SummaryTypes.Incomes.name
}

object ExpensesSummaryHandler extends SummaryHandler[selfemployment.Expense] {
  override implicit val reads: Reads[selfemployment.Expense] = selfemployment.Expense.reads
  override implicit val writes: Writes[selfemployment.Expense] = selfemployment.Expense.writes
  override def example(id: SummaryId) = selfemployment.Expense.example.copy(id = Some(id))
  override val listName = selfemployment.SummaryTypes.Expenses.name
}

object BalancingChargesSummaryHandler extends SummaryHandler[selfemployment.BalancingCharge] {
  override implicit val reads: Reads[selfemployment.BalancingCharge] = selfemployment.BalancingCharge.reads
  override implicit val writes: Writes[selfemployment.BalancingCharge] = selfemployment.BalancingCharge.writes
  override def example(id: SummaryId) = selfemployment.BalancingCharge.example.copy(id = Some(id))
  override val listName = selfemployment.SummaryTypes.BalancingCharges.name
}


object GoodsAndServiceOwnUseSummaryHandler extends SummaryHandler[selfemployment.GoodsAndServicesOwnUse] {
  override implicit val reads: Reads[selfemployment.GoodsAndServicesOwnUse] = selfemployment.GoodsAndServicesOwnUse.reads
  override implicit val writes: Writes[selfemployment.GoodsAndServicesOwnUse] = selfemployment.GoodsAndServicesOwnUse.writes
  override def example(id: SummaryId) = selfemployment.GoodsAndServicesOwnUse.example.copy(id = Some(id))
  override val listName = selfemployment.SummaryTypes.GoodsAndServicesOwnUse.name
}

object PrivateUseAdjustmentSummaryHandler extends SummaryHandler[furnishedholidaylettings.PrivateUseAdjustment] {
  override implicit val reads: Reads[furnishedholidaylettings.PrivateUseAdjustment] = furnishedholidaylettings.PrivateUseAdjustment.reads
  override implicit val writes: Writes[furnishedholidaylettings.PrivateUseAdjustment] = furnishedholidaylettings.PrivateUseAdjustment.writes
  override def example(id: SummaryId) = furnishedholidaylettings.PrivateUseAdjustment.example.copy(id = Some(id))
  override val listName = furnishedholidaylettings.SummaryTypes.PrivateUseAdjustments.name
}

object FurnishedHolidayLettingsBalancingChargesSummaryHandler extends SummaryHandler[furnishedholidaylettings.BalancingCharge] {
  override implicit val reads: Reads[furnishedholidaylettings.BalancingCharge] = furnishedholidaylettings.BalancingCharge.reads
  override implicit val writes: Writes[furnishedholidaylettings.BalancingCharge] = furnishedholidaylettings.BalancingCharge.writes
  override def example(id: SummaryId) = furnishedholidaylettings.BalancingCharge.example.copy(id = Some(id))
  override val listName = furnishedholidaylettings.SummaryTypes.BalancingCharges.name
}

object FurnishedHolidayLettingsIncomeSummaryHandler extends SummaryHandler[furnishedholidaylettings.Income] {
  override implicit val reads: Reads[furnishedholidaylettings.Income] = furnishedholidaylettings.Income.reads
  override implicit val writes: Writes[furnishedholidaylettings.Income] = furnishedholidaylettings.Income.writes
  override def example(id: SummaryId) = furnishedholidaylettings.Income.example.copy(id = Some(id))
  override val listName = furnishedholidaylettings.SummaryTypes.Incomes.name
}

object FurnishedHolidayLettingsExpenseSummaryHandler extends SummaryHandler[furnishedholidaylettings.Expense] {
  override implicit val reads: Reads[furnishedholidaylettings.Expense] = furnishedholidaylettings.Expense.reads
  override implicit val writes: Writes[furnishedholidaylettings.Expense] = furnishedholidaylettings.Expense.writes
  override def example(id: SummaryId) = furnishedholidaylettings.Expense.example.copy(id = Some(id))
  override val listName = furnishedholidaylettings.SummaryTypes.Expenses.name
}

object UKPropertyIncomeSummaryHandler extends SummaryHandler[ukproperty.Income] {
  override implicit val reads: Reads[ukproperty.Income] = ukproperty.Income.reads
  override implicit val writes: Writes[ukproperty.Income] = ukproperty.Income.writes
  override def example(id: SummaryId) = ukproperty.Income.example.copy(id = Some(id))
  override val listName = ukproperty.SummaryTypes.Incomes.name
}

object UKPropertyExpenseSummaryHandler extends SummaryHandler[ukproperty.Expenses] {
  override implicit val reads: Reads[ukproperty.Expenses] = ukproperty.Expenses.reads
  override implicit val writes: Writes[ukproperty.Expenses] = ukproperty.Expenses.writes
  override def example(id: SummaryId) = ukproperty.Expenses.example.copy(id = Some(id))
  override val listName = ukproperty.SummaryTypes.Expenses.name
}

object UKPropertyTaxPaidSummaryHandler extends SummaryHandler[ukproperty.TaxPaid] {
  override implicit val reads: Reads[ukproperty.TaxPaid] = ukproperty.TaxPaid.reads
  override implicit val writes: Writes[ukproperty.TaxPaid] = ukproperty.TaxPaid.writes
  override def example(id: SummaryId) = ukproperty.TaxPaid.example.copy(id = Some(id))
  override val listName = ukproperty.SummaryTypes.TaxPaid.name
}

object UKPropertyBalancingChargesSummaryHandler extends SummaryHandler[ukproperty.BalancingCharge] {
  override implicit val reads: Reads[ukproperty.BalancingCharge] = ukproperty.BalancingCharge.reads
  override implicit val writes: Writes[ukproperty.BalancingCharge] = ukproperty.BalancingCharge.writes
  override def example(id: SummaryId) = ukproperty.BalancingCharge.example.copy(id = Some(id))
  override val listName = ukproperty.SummaryTypes.BalancingCharges.name
}

object UKPropertyPrivateUseAdjustmentsSummaryHandler extends SummaryHandler[ukproperty.PrivateUseAdjustment] {
  override implicit val reads: Reads[ukproperty.PrivateUseAdjustment] = ukproperty.PrivateUseAdjustment.reads
  override implicit val writes: Writes[ukproperty.PrivateUseAdjustment] = ukproperty.PrivateUseAdjustment.writes
  override def example(id: SummaryId) = ukproperty.PrivateUseAdjustment.example.copy(id = Some(id))
  override val listName = ukproperty.SummaryTypes.PrivateUseAdjustments.name
}

