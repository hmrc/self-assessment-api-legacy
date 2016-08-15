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

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonIgnoreProperties}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand, NilTaxBand, SavingsStartingTaxBand}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math

case class MongoLiability(id: BSONObjectID,
                          liabilityId: LiabilityId,
                          saUtr: SaUtr,
                          taxYear: TaxYear,
                          createdDateTime: DateTime,
                          incomeFromEmployments: Seq[EmploymentIncome] = Nil,
                          profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
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
                          profitFromUkProperties: Seq[UkPropertyIncome] = Nil) extends Math {

  private lazy val dividendsTaxes = dividendsIncome.map {
    bandAllocation => bandAllocation.taxBand match {
      case NilTaxBand => bandAllocation.toTaxBandSummary(0)
      case BasicTaxBand => bandAllocation.toTaxBandSummary(7.5)
      case HigherTaxBand => bandAllocation.toTaxBandSummary(32.5)
      case AdditionalHigherTaxBand => bandAllocation.toTaxBandSummary(38.1)
      case unsupported => throw new IllegalArgumentException(s"Unsupported dividends tax band: $unsupported")
    }
  }

  private lazy val savingsTaxes = savingsIncome.map {
    bandAllocation => bandAllocation.taxBand match {
      case NilTaxBand => bandAllocation.toTaxBandSummary(0)
      case SavingsStartingTaxBand => bandAllocation.toTaxBandSummary(0)
      case BasicTaxBand => bandAllocation.toTaxBandSummary(20)
      case HigherTaxBand => bandAllocation.toTaxBandSummary(40)
      case AdditionalHigherTaxBand => bandAllocation.toTaxBandSummary(45)
      case unsupported => throw new IllegalArgumentException(s"Unsupported savings tax band: $unsupported")
    }
  }

  private lazy val nonSavingsTaxes = nonSavingsIncome.map {
    bandAllocation => bandAllocation.taxBand match {
      case BasicTaxBand => bandAllocation.toTaxBandSummary(20)
      case HigherTaxBand => bandAllocation.toTaxBandSummary(40)
      case AdditionalHigherTaxBand => bandAllocation.toTaxBandSummary(45)
      case unsupported => throw new IllegalArgumentException(s"Unsupported non savings tax band: $unsupported")
    }
  }

  private lazy val totalIncomeTax = (nonSavingsTaxes ++ savingsTaxes ++ dividendsTaxes).map(_.tax).sum

  private lazy val totalTaxDeducted = taxDeducted.map(_.interestFromUk).getOrElse(BigDecimal(0))

  private lazy val totalTaxDue = totalIncomeTax - totalTaxDeducted

  def toLiability =
    Liability(
      income = IncomeSummary(
        incomes = IncomeFromSources(
          nonSavings = NonSavingsIncomes(
            employment = incomeFromEmployments,
            selfEmployment = profitFromSelfEmployments,
            ukProperties = profitFromUkProperties
          ),
          savings = SavingsIncomes(
            fromUKBanksAndBuildingSocieties = interestFromUKBanksAndBuildingSocieties
          ),
          dividends = DividendsIncomes(
            fromUKSources = dividendsFromUKSources
          ),
          total = totalIncomeReceived.getOrElse(0)
        ),
        deductions = Some(Deductions(
          incomeTaxRelief = allowancesAndReliefs.incomeTaxRelief.getOrElse(0),
          personalAllowance = allowancesAndReliefs.personalAllowance.getOrElse(0),
          total = sum(allowancesAndReliefs.incomeTaxRelief, allowancesAndReliefs.personalAllowance)
        )),
        totalIncomeOnWhichTaxIsDue = totalIncomeOnWhichTaxIsDue.getOrElse(0)
      ),
      incomeTaxCalculations = IncomeTaxCalculations(
        nonSavings = nonSavingsTaxes,
        savings = savingsTaxes,
        dividends = dividendsTaxes,
        total = totalIncomeTax
      ),
      taxDeducted = taxDeducted.map(taxDeducted =>
        TaxDeducted(
          interestFromUk = taxDeducted.interestFromUk,
          total = totalTaxDeducted)
      ).getOrElse(TaxDeducted(0, 0)),
      totalTaxDue = if (totalTaxDue > 0) totalTaxDue else 0,
      totalTaxOverpaid = if (totalTaxDue < 0) totalTaxDue.abs else 0
    )

  def totalSavingsIncome = interestFromUKBanksAndBuildingSocieties.map(_.totalInterest).sum
}


case class EmploymentIncome(sourceId: SourceId, pay: BigDecimal, benefitsAndExpenses: BigDecimal, allowableExpenses : BigDecimal, total: BigDecimal)

case class SelfEmploymentIncome(sourceId: SourceId, taxableProfit: BigDecimal, profit: BigDecimal)

case class UkPropertyIncome(sourceId: SourceId, profit: BigDecimal)

case class TaxBandAllocation(amount: BigDecimal, taxBand: TaxBand) extends Math {

  def toTaxBandSummary(chargedAt: BigDecimal) = uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary(taxBand.name, amount, s"$chargedAt%", tax(chargedAt))

  def tax(chargedAt: BigDecimal): BigDecimal = roundDown(amount * chargedAt / 100)

  def available: BigDecimal = positiveOrZero(taxBand.width - amount)

  def + (other: TaxBandAllocation) = {
    require(taxBand == other.taxBand)
    TaxBandAllocation(amount + other.amount, taxBand)
  }
}

case class AllowancesAndReliefs(personalAllowance: Option[BigDecimal] = None, personalSavingsAllowance: Option[BigDecimal] = None, incomeTaxRelief: Option[BigDecimal] = None, savingsStartingRate: Option[BigDecimal] = None)

case class MongoTaxDeducted(interestFromUk: BigDecimal)

object MongoLiability {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val employmentIncomeFormats = Json.format[EmploymentIncome]
  implicit val selfEmploymentIncomeFormats = Json.format[SelfEmploymentIncome]
  implicit val ukPropertyIncomeFormats = Json.format[UkPropertyIncome]
  implicit val taxBandAllocationFormats = Json.format[TaxBandAllocation]
  implicit val allowancesAndReliefsFormats = Json.format[AllowancesAndReliefs]
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

