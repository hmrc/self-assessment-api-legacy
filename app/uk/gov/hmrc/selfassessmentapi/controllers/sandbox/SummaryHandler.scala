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

object IncomesSummaryHandler extends SummaryHandler[SelfEmploymentIncome] {
  override implicit val reads: Reads[SelfEmploymentIncome] = SelfEmploymentIncome.reads
  override implicit val writes: Writes[SelfEmploymentIncome] = SelfEmploymentIncome.writes
  override def example(id: SummaryId) = SelfEmploymentIncome.example.copy(id = Some(id))
  override val listName = SummaryTypes.SelfEmploymentIncomes.name
}

object ExpensesSummaryHandler extends SummaryHandler[SelfEmploymentExpense] {
  override implicit val reads: Reads[SelfEmploymentExpense] = SelfEmploymentExpense.reads
  override implicit val writes: Writes[SelfEmploymentExpense] = SelfEmploymentExpense.writes
  override def example(id: SummaryId) = SelfEmploymentExpense.example.copy(id = Some(id))
  override val listName = SummaryTypes.Expenses.name
}

object BalancingChargesSummaryHandler extends SummaryHandler[BalancingCharge] {
  override implicit val reads: Reads[BalancingCharge] = BalancingCharge.reads
  override implicit val writes: Writes[BalancingCharge] = BalancingCharge.writes
  override def example(id: SummaryId) = BalancingCharge.example.copy(id = Some(id))
  override val listName = SummaryTypes.BalancingCharges.name
}


object GoodsAndServiceOwnUseSummaryHandler extends SummaryHandler[GoodsAndServicesOwnUse] {
  override implicit val reads: Reads[GoodsAndServicesOwnUse] = GoodsAndServicesOwnUse.reads
  override implicit val writes: Writes[GoodsAndServicesOwnUse] = GoodsAndServicesOwnUse.writes
  override def example(id: SummaryId) = GoodsAndServicesOwnUse.example.copy(id = Some(id))
  override val listName = SummaryTypes.GoodsAndServicesOwnUse.name
}

object PrivateUseAdjustmentSummaryHandler extends SummaryHandler[PrivateUseAdjustment] {
  override implicit val reads: Reads[PrivateUseAdjustment] = PrivateUseAdjustment.reads
  override implicit val writes: Writes[PrivateUseAdjustment] = PrivateUseAdjustment.writes
  override def example(id: SummaryId) = PrivateUseAdjustment.example.copy(id = Some(id))
  override val listName = SummaryTypes.PrivateUseAdjustment.name
}

object FurnishedHolidayLettingsIncomeSummaryHandler extends SummaryHandler[FurnishedHolidayLettingsIncome] {
  override implicit val reads: Reads[FurnishedHolidayLettingsIncome] = FurnishedHolidayLettingsIncome.reads
  override implicit val writes: Writes[FurnishedHolidayLettingsIncome] = FurnishedHolidayLettingsIncome.writes
  override def example(id: SummaryId) = FurnishedHolidayLettingsIncome.example.copy(id = Some(id))
  override val listName = SummaryTypes.FurnishedHolidayLettingsIncome.name
}

object UKPropertyIncomeSummaryHandler extends SummaryHandler[UKPropertyIncome] {
  override implicit val reads: Reads[UKPropertyIncome] = UKPropertyIncome.reads
  override implicit val writes: Writes[UKPropertyIncome] = UKPropertyIncome.writes
  override def example(id: SummaryId) = UKPropertyIncome.example.copy(id = Some(id))
  override val listName = SummaryTypes.UKPropertyIncomes.name
}


