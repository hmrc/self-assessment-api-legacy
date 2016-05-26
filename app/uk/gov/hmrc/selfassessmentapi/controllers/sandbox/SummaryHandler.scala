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

object IncomesSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes.Incomes.name
}

object ExpensesSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Expense] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Expense] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Expense.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Expense] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Expense.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Expense.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes.Expenses.name
}

object BalancingChargesSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.BalancingCharge] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.BalancingCharge] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.BalancingCharge.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.BalancingCharge] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.BalancingCharge.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.BalancingCharge.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes.BalancingCharges.name
}


object GoodsAndServiceOwnUseSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.GoodsAndServicesOwnUse] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.GoodsAndServicesOwnUse] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.GoodsAndServicesOwnUse.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.selfemployment.GoodsAndServicesOwnUse] = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.GoodsAndServicesOwnUse.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.GoodsAndServicesOwnUse.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes.GoodsAndServicesOwnUse.name
}

object PrivateUseAdjustmentSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PrivateUseAdjustment] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PrivateUseAdjustment] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PrivateUseAdjustment.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PrivateUseAdjustment] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PrivateUseAdjustment.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PrivateUseAdjustment.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.SummaryTypes.PrivateUseAdjustments.name
}

object FurnishedHolidayLettingsBalancingChargesSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.BalancingCharge] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.BalancingCharge] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.BalancingCharge.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.BalancingCharge] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.BalancingCharge.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.BalancingCharge.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.SummaryTypes.BalancingCharges.name
}

object FurnishedHolidayLettingsIncomeSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Income] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Income] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Income.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Income] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Income.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Income.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.SummaryTypes.Incomes.name
}

object FurnishedHolidayLettingsExpenseSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Expense] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Expense] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Expense.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Expense] = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Expense.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.Expense.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.SummaryTypes.Expenses.name
}

object UKPropertyIncomeSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Income] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Income] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Income.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Income] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Income.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Income.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SummaryTypes.Incomes.name
}

object UKPropertyExpenseSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Expenses] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Expenses] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Expenses.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Expenses] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Expenses.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.Expenses.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SummaryTypes.Expenses.name
}

object UKPropertyTaxPaidSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SummaryTypes.TaxPaid.name
}

object UKPropertyBalancingChargesSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.BalancingCharge] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.BalancingCharge] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.BalancingCharge.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.BalancingCharge] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.BalancingCharge.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.BalancingCharge.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SummaryTypes.BalancingCharges.name
}

object UKPropertyPrivateUseAdjustmentsSummaryHandler extends SummaryHandler[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.PrivateUseAdjustment] {
  override implicit val reads: Reads[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.PrivateUseAdjustment] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.PrivateUseAdjustment.reads
  override implicit val writes: Writes[uk.gov.hmrc.selfassessmentapi.domain.ukproperty.PrivateUseAdjustment] = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.PrivateUseAdjustment.writes
  override def example(id: SummaryId) = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.PrivateUseAdjustment.example.copy(id = Some(id))
  override val listName = uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SummaryTypes.PrivateUseAdjustments.name
}

