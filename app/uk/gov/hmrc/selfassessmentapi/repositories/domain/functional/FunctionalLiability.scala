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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain._

object TaxDeducted {
  def apply(selfAssessment: SelfAssessment) =
    MongoTaxDeducted(interestFromUk = Savings.TotalTaxPaid(selfAssessment), deductionFromUkProperties = UkProperty.TaxesPaid(selfAssessment),
      ukTaxesPaidForEmployments = Employment.TaxesPaid(selfAssessment))
}

case class FunctionalLiability(id: BSONObjectID,
                          liabilityId: LiabilityId,
                          saUtr: SaUtr,
                          taxYear: TaxYear,
                          employmentIncome: Seq[EmploymentIncome],
                          selfEmploymentIncome: Seq[SelfEmploymentIncome],
                          ukPropertyIncome: Seq[UkPropertyIncome],
                          furnishedHolidayLettingsIncome: Seq[FurnishedHolidayLettingIncome],
                          savingsIncome: Seq[InterestFromUKBanksAndBuildingSocieties],
                          ukDividendsIncome: Seq[DividendsFromUKSources],
                          totalIncomeReceived: BigDecimal,
                          totalTaxableIncome: BigDecimal,
                          allowancesAndReliefs: AllowancesAndReliefs,
                          taxDeducted: MongoTaxDeducted,
                          dividendTaxBandSummary: Seq[TaxBandSummary],
                          savingsTaxBandSummary: Seq[TaxBandSummary],
                          nonSavingsTaxBandSummary: Seq[TaxBandSummary],
                          totalIncomeTax: BigDecimal,
                          totalTaxDeducted: BigDecimal,
                          totalTaxDue: BigDecimal,
                          totalTaxOverPaid: BigDecimal) extends FLiabilityResult {
  def toLiability =
    Liability(
      income = IncomeSummary(
        incomes = IncomeFromSources(
          nonSavings = NonSavingsIncomes(
            employment = employmentIncome,
            selfEmployment = selfEmploymentIncome,
            ukProperties = ukPropertyIncome,
            furnishedHolidayLettings = furnishedHolidayLettingsIncome
          ),
          savings = SavingsIncomes(
            fromUKBanksAndBuildingSocieties = savingsIncome
          ),
          dividends = DividendsIncomes(
            fromUKSources = ukDividendsIncome
          ),
          total = totalIncomeReceived
        ),
        deductions = Some(
          domain.Deductions(
            incomeTaxRelief = allowancesAndReliefs.incomeTaxRelief.getOrElse(0),
            personalAllowance = allowancesAndReliefs.personalAllowance.getOrElse(0),
            retirementAnnuityContract = allowancesAndReliefs.retirementAnnuityContract.getOrElse(0),
            total = Sum(allowancesAndReliefs.incomeTaxRelief,
              allowancesAndReliefs.personalAllowance,
              allowancesAndReliefs.retirementAnnuityContract)
          )),
        totalIncomeOnWhichTaxIsDue = totalTaxableIncome
      ),
      incomeTaxCalculations = IncomeTaxCalculations(
        nonSavings = nonSavingsTaxBandSummary,
        savings = savingsTaxBandSummary,
        dividends = dividendTaxBandSummary,
        total = totalIncomeTax
      ),
      taxDeducted =
          domain.TaxDeducted(interestFromUk = taxDeducted.interestFromUk,
            fromUkProperties = taxDeducted.deductionFromUkProperties,
            fromEmployments = taxDeducted.ukTaxesPaidForEmployments,
            total = totalTaxDeducted),
      totalTaxDue = totalTaxDue,
      totalTaxOverpaid = totalTaxOverPaid)
}

object FunctionalLiability {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats

  implicit val taxDeductedFormats = Json.format[MongoTaxDeducted]
  implicit val liabilityFormats = Json.format[FunctionalLiability]

  def create(saUtr: SaUtr, taxYear: TaxYear, selfAssessment: SelfAssessment, createdDateTime: DateTime = DateTime.now()) = {
    val id = BSONObjectID.generate
    new FunctionalLiability(id = id,
      liabilityId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      employmentIncome = Employment.Incomes(selfAssessment),
      selfEmploymentIncome = SelfEmployment.Incomes(selfAssessment),
      furnishedHolidayLettingsIncome = FurnishedHolidayLetting.Incomes(selfAssessment),
      ukPropertyIncome = UkProperty.Incomes(selfAssessment),
      savingsIncome = Savings.Incomes(selfAssessment),
      ukDividendsIncome = Dividends.FromUK(selfAssessment),
      allowancesAndReliefs = Deductions(selfAssessment),
      taxDeducted = TaxDeducted(selfAssessment),
      dividendTaxBandSummary = Dividends.IncomeTaxBandSummary(selfAssessment),
      savingsTaxBandSummary = Savings.IncomeTaxBandSummary(selfAssessment),
      nonSavingsTaxBandSummary = NonSavings.IncomeTaxBandSummary(selfAssessment),
      totalTaxableIncome = Totals.TaxableIncome(selfAssessment),
      totalIncomeReceived = Totals.IncomeReceived(selfAssessment),
      totalIncomeTax = Totals.IncomeTax(selfAssessment),
      totalTaxDeducted = Totals.TaxDeducted(selfAssessment),
      totalTaxDue = Totals.TaxDue(selfAssessment),
      totalTaxOverPaid = Totals.TaxOverpaid(selfAssessment)
    )
  }

}

case class MongoUkTaxPaidForEmployment(sourceId: SourceId, ukTaxPaid: BigDecimal)

object MongoUkTaxPaidForEmployment {
  implicit val ukTaxPaidForEmploymentFormats = Json.format[MongoUkTaxPaidForEmployment]
}

case class MongoTaxDeducted(interestFromUk: BigDecimal = 0,
                            totalDeductionFromUkProperties: BigDecimal = 0,
                            deductionFromUkProperties: Seq[TaxPaidForUkProperty] = Nil,
                            ukTaxPaid: BigDecimal = 0,
                            ukTaxesPaidForEmployments: Seq[UkTaxPaidForEmployment] = Nil) {

  def totalTaxDeducted = interestFromUk + totalDeductionFromUkProperties + ukTaxPaid
}

object MongoTaxDeducted {
  implicit val taxDeductedFormats = Json.format[MongoTaxDeducted]
}


case class FLiabilityCalculationError(code: ErrorCode, message: String)

object FLiabilityCalculationError {
  implicit val calculationErrorFormats = Json.format[FLiabilityCalculationError]
}

case class FLiabilityCalculationErrors(id: BSONObjectID,
                                           liabilityCalculationErrorId: LiabilityCalculationErrorId,
                                           override val saUtr: SaUtr,
                                           override val taxYear: TaxYear,
                                           errors: Seq[FLiabilityCalculationError])
  extends FLiabilityResult

object FLiabilityCalculationErrors {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val calculationErrorsFormats = Json.format[FLiabilityCalculationErrors]

  def create(saUtr: SaUtr, taxYear: TaxYear, errors: Seq[FLiabilityCalculationError]): FLiabilityCalculationErrors = {
    val id = BSONObjectID.generate
    FLiabilityCalculationErrors(id = id,
      liabilityCalculationErrorId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      errors = errors)
  }
}

trait FLiabilityResult {

  def saUtr: SaUtr
  def taxYear: TaxYear

  def fold[X](fError: FLiabilityCalculationErrors => X, fLiability: FunctionalLiability => X) = this match {
    case c: FLiabilityCalculationErrors => fError(c)
    case l: FunctionalLiability => fLiability(l)
  }
}

object FLiabilityResult {

  import FLiabilityCalculationErrors.calculationErrorsFormats
  import FunctionalLiability.liabilityFormats

  implicit val liabilityResultFormat = Json.format[FLiabilityResult]

  def unapply(liabilityResult: FLiabilityResult): Option[(String, JsValue)] = {
    val (prod: Product, sub) = liabilityResult match {
      case o: FunctionalLiability => (o, Json.toJson(o)(liabilityFormats))
      case o: FLiabilityCalculationErrors => (o, Json.toJson(o)(calculationErrorsFormats))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(className: String, data: JsValue): FLiabilityResult = {
    (className match {
      case "FunctionalLiability" => Json.fromJson[FunctionalLiability](data)(liabilityFormats)
      case "FLiabilityCalculationErrors" =>
        Json.fromJson[FLiabilityCalculationErrors](data)(calculationErrorsFormats)
    }).get
  }
}






