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
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math

case class MongoLiability(id: BSONObjectID,
                          liabilityId: LiabilityId,
                          saUtr: SaUtr,
                          taxYear: TaxYear,
                          createdDateTime: DateTime,
                          profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
                          interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties] = Nil,
                          dividendsFromUKSources: Seq[DividendsFromUKSources] = Nil,
                          totalIncomeReceived: Option[BigDecimal] = None,
                          payPensionProfitsReceived: Option[BigDecimal] = None,
                          totalTaxableIncome: Option[BigDecimal] = None,
                          personalAllowance: Option[BigDecimal] = None,
                          deductions: Option[Deductions] = None,
                          deductionsRemaining: Option[Deductions] = None,
                          totalIncomeOnWhichTaxIsDue: Option[BigDecimal] = None,
                          payPensionsProfits: Seq[TaxBandSummary] = Nil,
                          savingsIncome: Seq[TaxBandSummary] = Nil,
                          dividends: Seq[TaxBandSummary] = Nil,
                          personalSavingsAllowance: Option[BigDecimal] = None) {

  require(if (deductionsRemaining.isDefined) deductions.isDefined else true, "deductions must be defined if deductionsRemaining are")
  require((for {
    ded <- deductions
    remDed <- deductionsRemaining
  } yield ded.incomeTaxRelief >= remDed.incomeTaxRelief && ded.totalDeductions >= remDed.totalDeductions).getOrElse(true),
    "Values on deductions must be greater than or equal to the corresponding values on deductionsRemaining")

  def toLiability =
    Liability(
      id = Some(liabilityId),
      income = IncomeSummary(
        incomes = IncomeFromSources(
          selfEmployment = profitFromSelfEmployments.map(_.toIncome),
          interestFromUKBanksAndBuildingSocieties = interestFromUKBanksAndBuildingSocieties,
          dividendsFromUKSources = dividendsFromUKSources,
          employment = Nil
        ),
        deductions = deductions,
        totalIncomeReceived = totalIncomeReceived.getOrElse(0),
        personalAllowance = personalAllowance.getOrElse(0),
        totalTaxableIncome = totalTaxableIncome.getOrElse(0),
        totalIncomeOnWhichTaxIsDue = totalIncomeOnWhichTaxIsDue.getOrElse(0)
      ),
      incomeTaxCalculations = IncomeTaxCalculations(
        payPensionsProfits = payPensionsProfits.map(_.toTaxBandSummary),
        savingsIncome = savingsIncome.map(_.toTaxBandSummary),
        dividends = dividends.map(_.toTaxBandSummary),
        incomeTaxCharged = (payPensionsProfits ++ savingsIncome ++ dividends).map(_.tax).sum
      ),
      credits = Nil,
      class4Nic = CalculatedAmount(calculations = Nil, total = 0),
      totalTaxDue = 0
    )
}

case class SelfEmploymentIncome(sourceId: SourceId, taxableProfit: BigDecimal, profit: BigDecimal, lossBroughtForward: BigDecimal) {

  def toIncome = Income(sourceId, taxableProfit, profit)
}

case class TaxBandSummary(taxableAmount: BigDecimal, taxBand: TaxBand) extends Math {

  def toTaxBandSummary = uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary(taxBand.name, taxableAmount, s"${taxBand.chargedAt}%", tax)

  def tax: BigDecimal = roundDown(taxableAmount * taxBand.chargedAt / 100)
}

object MongoLiability {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val incomeFormats = Json.format[SelfEmploymentIncome]
  implicit val taxBandSummaryFormats = Json.format[TaxBandSummary]
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

