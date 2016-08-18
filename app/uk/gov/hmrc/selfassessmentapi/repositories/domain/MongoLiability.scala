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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand, NilTaxBand, SavingsStartingTaxBand}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._

case class MongoLiability(id: BSONObjectID,
                          liabilityId: LiabilityId,
                          override val saUtr: SaUtr,
                          override val taxYear: TaxYear,
                          createdDateTime: DateTime,
                          incomeFromEmployments: Seq[EmploymentIncome] = Nil,
                          profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
                          incomeFromFurnishedHolidayLettings: Seq[FurnishedHolidayLettingIncome] = Nil,
                          interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties] = Nil,
                          dividendsFromUKSources: Seq[DividendsFromUKSources] = Nil,
                          totalIncomeReceived: Option[BigDecimal] = None,
                          nonSavingsIncomeReceived: Option[BigDecimal] = None,
                          totalAllowancesAndReliefs: Option[BigDecimal] = None,
                          deductionsRemaining: Option[BigDecimal] = None,
                          totalIncomeOnWhichTaxIsDue: Option[BigDecimal] = None,
                          nonSavingsIncome: Seq[TaxBandAllocation] = Nil,
                          savingsIncome: Seq[TaxBandAllocation] = Nil,
                          dividendsIncome: Seq[TaxBandAllocation] = Nil,
                          allowancesAndReliefs: AllowancesAndReliefs = AllowancesAndReliefs(),
                          taxDeducted: Option[MongoTaxDeducted] = None,
                          profitFromUkProperties: Seq[UkPropertyIncome] = Nil)
    extends LiabilityResult {

  private lazy val dividendsTaxes = dividendsIncome.map { bandAllocation =>
    bandAllocation.taxBand match {
      case NilTaxBand => bandAllocation.toTaxBandSummary(0)
      case BasicTaxBand => bandAllocation.toTaxBandSummary(7.5)
      case HigherTaxBand => bandAllocation.toTaxBandSummary(32.5)
      case AdditionalHigherTaxBand => bandAllocation.toTaxBandSummary(38.1)
      case unsupported => throw new IllegalArgumentException(s"Unsupported dividends tax band: $unsupported")
    }
  }

  private lazy val savingsTaxes = savingsIncome.map { bandAllocation =>
    bandAllocation.taxBand match {
      case NilTaxBand => bandAllocation.toTaxBandSummary(0)
      case SavingsStartingTaxBand => bandAllocation.toTaxBandSummary(0)
      case BasicTaxBand => bandAllocation.toTaxBandSummary(20)
      case HigherTaxBand => bandAllocation.toTaxBandSummary(40)
      case AdditionalHigherTaxBand => bandAllocation.toTaxBandSummary(45)
      case unsupported => throw new IllegalArgumentException(s"Unsupported savings tax band: $unsupported")
    }
  }

  private lazy val nonSavingsTaxes = nonSavingsIncome.map { bandAllocation =>
    bandAllocation.taxBand match {
      case BasicTaxBand => bandAllocation.toTaxBandSummary(20)
      case HigherTaxBand => bandAllocation.toTaxBandSummary(40)
      case AdditionalHigherTaxBand => bandAllocation.toTaxBandSummary(45)
      case unsupported => throw new IllegalArgumentException(s"Unsupported non savings tax band: $unsupported")
    }
  }

  private lazy val totalIncomeTax = (nonSavingsTaxes ++ savingsTaxes ++ dividendsTaxes).map(_.tax).sum

  private lazy val totalTaxDeducted = taxDeducted.map(_.totalTaxDeducted).getOrElse(BigDecimal(0))

  private lazy val totalTaxDue = totalIncomeTax - totalTaxDeducted

  def toLiability =
    Liability(
        income = IncomeSummary(
            incomes = IncomeFromSources(
                nonSavings = NonSavingsIncomes(
                    employment = incomeFromEmployments,
                    selfEmployment = profitFromSelfEmployments,
                    ukProperties = profitFromUkProperties,
                    furnishedHolidayLettings = incomeFromFurnishedHolidayLettings
                ),
                savings = SavingsIncomes(
                    fromUKBanksAndBuildingSocieties = interestFromUKBanksAndBuildingSocieties
                ),
                dividends = DividendsIncomes(
                    fromUKSources = dividendsFromUKSources
                ),
                total = totalIncomeReceived.getOrElse(0)
            ),
            deductions = Some(
                Deductions(
                    incomeTaxRelief = allowancesAndReliefs.incomeTaxRelief.getOrElse(0),
                    personalAllowance = allowancesAndReliefs.personalAllowance.getOrElse(0),
                    retirementAnnuityContract = allowancesAndReliefs.retirementAnnuityContract.getOrElse(0),
                    total = sum(allowancesAndReliefs.incomeTaxRelief,
                                allowancesAndReliefs.personalAllowance,
                                allowancesAndReliefs.retirementAnnuityContract)
                )),
            totalIncomeOnWhichTaxIsDue = totalIncomeOnWhichTaxIsDue.getOrElse(0)
        ),
        incomeTaxCalculations = IncomeTaxCalculations(
            nonSavings = nonSavingsTaxes,
            savings = savingsTaxes,
            dividends = dividendsTaxes,
            total = totalIncomeTax
        ),
        taxDeducted = taxDeducted
          .map(taxDeducted =>
                TaxDeducted(interestFromUk = taxDeducted.interestFromUk,
                            deductionFromUkProperties = taxDeducted.deductionFromUkProperties,
                            fromEmployments = taxDeducted.ukTaxesPaidForEmployments.map(ukTaxPaidForEmployment =>
                                  UkTaxPaidForEmployment(ukTaxPaidForEmployment.sourceId,
                                                         ukTaxPaidForEmployment.ukTaxPaid)),
                            total = taxDeducted.totalTaxDeducted))
          .getOrElse(TaxDeducted(0, 0, Nil, 0)),
        totalTaxDue = if (totalTaxDue > 0) totalTaxDue else 0,
        totalTaxOverpaid = if (totalTaxDue < 0) totalTaxDue.abs else 0
    )

  def totalSavingsIncome = interestFromUKBanksAndBuildingSocieties.map(_.totalInterest).sum
}

case class EmploymentIncome(sourceId: SourceId,
                            pay: BigDecimal,
                            benefitsAndExpenses: BigDecimal,
                            allowableExpenses: BigDecimal,
                            total: BigDecimal)

case class FurnishedHolidayLettingIncome(sourceId: String, profit: BigDecimal)

case class SelfEmploymentIncome(sourceId: SourceId, taxableProfit: BigDecimal, profit: BigDecimal)

case class UkPropertyIncome(sourceId: SourceId, profit: BigDecimal)

case class TaxBandAllocation(amount: BigDecimal, taxBand: TaxBand) {

  def toTaxBandSummary(chargedAt: BigDecimal) =
    uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary(taxBand.name, amount, s"$chargedAt%", tax(chargedAt))

  def tax(chargedAt: BigDecimal): BigDecimal = roundDownToPennies(amount * chargedAt / 100)

  def available: BigDecimal = positiveOrZero(taxBand.width - amount)

  def +(other: TaxBandAllocation) = {
    require(taxBand == other.taxBand)
    TaxBandAllocation(amount + other.amount, taxBand)
  }
}

case class AllowancesAndReliefs(personalAllowance: Option[BigDecimal] = None,
                                personalSavingsAllowance: Option[BigDecimal] = None,
                                incomeTaxRelief: Option[BigDecimal] = None,
                                savingsStartingRate: Option[BigDecimal] = None,
                                retirementAnnuityContract: Option[BigDecimal] = None)

case class MongoUkTaxPaidForEmployment(sourceId: SourceId, ukTaxPaid: BigDecimal)

case class MongoTaxDeducted(interestFromUk: BigDecimal = 0,
                            deductionFromUkProperties: BigDecimal = 0,
                            ukTaxPaid: BigDecimal = 0,
                            ukTaxesPaidForEmployments: Seq[MongoUkTaxPaidForEmployment] = Nil) {
  def totalTaxDeducted = interestFromUk + deductionFromUkProperties + ukTaxPaid
}

case class MongoLiabilityCalculationError(code: ErrorCode, message: String)

object MongoLiabilityCalculationError {
  implicit val calculationErrorFormats = Json.format[MongoLiabilityCalculationError]
}

case class MongoLiabilityCalculationErrors(id: BSONObjectID,
                                           liabilityCalculationErrorId: LiabilityCalculationErrorId,
                                           override val saUtr: SaUtr,
                                           override val taxYear: TaxYear,
                                           errors: Seq[MongoLiabilityCalculationError])
    extends LiabilityResult

object MongoLiabilityCalculationErrors {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val calculationErrorsFormats = Json.format[MongoLiabilityCalculationErrors]

  def create(saUtr: SaUtr, taxYear: TaxYear, errors: Seq[MongoLiabilityCalculationError]): MongoLiabilityCalculationErrors = {
    val id = BSONObjectID.generate
    MongoLiabilityCalculationErrors(id = id,
                                   liabilityCalculationErrorId = id.stringify,
                                   saUtr = saUtr,
                                   taxYear = taxYear,
                                   errors = errors)
  }
}

object MongoLiability {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val employmentIncomeFormats = Json.format[EmploymentIncome]
  implicit val selfEmploymentIncomeFormats = Json.format[SelfEmploymentIncome]
  implicit val ukPropertyIncomeFormats = Json.format[UkPropertyIncome]
  implicit val furnishedHolidayLettingIncomeFormats = Json.format[FurnishedHolidayLettingIncome]
  implicit val taxBandAllocationFormats = Json.format[TaxBandAllocation]
  implicit val allowancesAndReliefsFormats = Json.format[AllowancesAndReliefs]
  implicit val ukTaxPaidForEmploymentFormats = Json.format[MongoUkTaxPaidForEmployment]
  implicit val taxDeductedFormats = Json.format[MongoTaxDeducted]
  implicit val liabilityFormats = Json.format[MongoLiability]

  def create(saUtr: SaUtr, taxYear: TaxYear): MongoLiability = {
    val id = BSONObjectID.generate
    MongoLiability(
        id = id,
        liabilityId = id.stringify,
        saUtr = saUtr,
        taxYear = taxYear,
        createdDateTime = DateTime.now(DateTimeZone.UTC)
    )
  }
}

sealed trait LiabilityResult {

  def saUtr: SaUtr
  def taxYear: TaxYear

  def fold[X](fError: MongoLiabilityCalculationErrors => X, fLiability: MongoLiability => X) = this match {
    case c: MongoLiabilityCalculationErrors => fError(c)
    case l: MongoLiability => fLiability(l)
  }
}

object LiabilityResult {

  import MongoLiabilityCalculationErrors.calculationErrorsFormats
  import MongoLiability.liabilityFormats

  implicit val liabilityResultFormat = Json.format[LiabilityResult]

  def unapply(liabilityResult: LiabilityResult): Option[(String, JsValue)] = {
    val (prod: Product, sub) = liabilityResult match {
      case o: MongoLiability => (o, Json.toJson(o)(liabilityFormats))
      case o: MongoLiabilityCalculationErrors => (o, Json.toJson(o)(calculationErrorsFormats))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(className: String, data: JsValue): LiabilityResult = {
    (className match {
      case "MongoLiability" => Json.fromJson[MongoLiability](data)(liabilityFormats)
      case "MongoLiabilityCalculationErrors" =>
        Json.fromJson[MongoLiabilityCalculationErrors](data)(calculationErrorsFormats)
    }).get
  }
}
