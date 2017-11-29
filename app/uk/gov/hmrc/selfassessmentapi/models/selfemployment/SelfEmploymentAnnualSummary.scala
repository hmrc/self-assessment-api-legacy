/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, des}

case class SelfEmploymentAnnualSummary(allowances: Option[Allowances],
                                       adjustments: Option[Adjustments],
                                       disallowableExpenses: Option[Expenses],
                                       nonFinancials: Option[NonFinancials])

object SelfEmploymentAnnualSummary {
  implicit val writes: Writes[SelfEmploymentAnnualSummary] = Json.writes[SelfEmploymentAnnualSummary]

  implicit val reads: Reads[SelfEmploymentAnnualSummary] = (
      (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments] and
      (__ \ "disallowableExpenses").readNullable[Expenses] and
      (__ \ "nonFinancials").readNullable[NonFinancials]
    ) (SelfEmploymentAnnualSummary.apply _).filter(
    ValidationError(
      "Balancing charge on BPRA (Business Premises Renovation Allowance) can only be claimed when there is a value for BPRA.",
      ErrorCode.INVALID_BALANCING_CHARGE_BPRA)) { annualSummary => validateBalancingChargeBPRA(annualSummary) }

  private def validateBalancingChargeBPRA(annualSummary: SelfEmploymentAnnualSummary): Boolean = {
    annualSummary.adjustments.forall { adjustments =>
      adjustments.balancingChargeBPRA.forall { _ =>
        annualSummary.allowances.exists(_.businessPremisesRenovationAllowance.exists(_ > 0))
      }
    }
  }

  def from(desSummary: des.selfemployment.SelfEmploymentAnnualSummary): SelfEmploymentAnnualSummary = {
    val adjustments = desSummary.annualAdjustments.map { adj =>
      Adjustments(
        includedNonTaxableProfits = adj.includedNonTaxableProfits,
        basisAdjustment = adj.basisAdjustment,
        overlapReliefUsed = adj.overlapReliefUsed,
        accountingAdjustment = adj.accountingAdjustment,
        averagingAdjustment = adj.averagingAdjustment,
        lossBroughtForward = adj.lossBroughtForward,
        outstandingBusinessIncome = adj.outstandingBusinessIncome,
        balancingChargeBPRA = adj.balancingChargeBpra,
        balancingChargeOther = adj.balancingChargeOther,
        goodsAndServicesOwnUse = adj.goodsAndServicesOwnUse,
        overlapProfitCarriedForward = adj.overlapProfitCarriedForward,
        adjustedProfit = adj.adjustedProfit,
        adjustedLoss = adj.adjustedLoss,
        lossOffsetAgainstOtherIncome = adj.lossOffsetAgainstOtherIncome,
        lossCarriedBackOffsetAgainstIncomeOrCGT = adj.lossCarriedBackOffsetAgainstIncomeOrCGT,
        lossCarriedForwardTotal = adj.lossCarriedForwardTotal,
        cisDeductionsTotal = adj.cisDeductionsTotal,
        taxDeductionsFromTradingIncome = adj.taxDeductionsFromTradingIncome,
        class4NicProfitAdjustment = adj.class4NicProfitAdjustment
      )
    }

    val allowances = desSummary.annualAllowances.map { allow =>
      Allowances(
        annualInvestmentAllowance = allow.annualInvestmentAllowance,
        capitalAllowanceMainPool = allow.capitalAllowanceMainPool,
        capitalAllowanceSpecialRatePool = allow.capitalAllowanceSpecialRatePool,
        businessPremisesRenovationAllowance = allow.businessPremisesRenovationAllowance,
        enhancedCapitalAllowance = allow.enhanceCapitalAllowance,
        allowanceOnSales = allow.allowanceOnSales,
        zeroEmissionGoodsVehicleAllowance = allow.zeroEmissionGoodsVehicleAllowance,
        capitalAllowanceSingleAssetPool = allow.capitalAllowanceSingleAssetPool
      )
    }

    val nonFinancials = NonFinancials.from(desSummary.annualNonFinancials)

    val disallowableExpenses = desSummary.annualDisallowables.map { deductions =>
      Expenses(
        costOfGoodsBought = deductions.costOfGoods.map(_.amount),
        cisPaymentsToSubcontractors = deductions.constructionIndustryScheme.map(_.amount),
        staffCosts = deductions.staffCosts.map(_.amount),
        travelCosts = deductions.travelCosts.map(_.amount),
        premisesRunningCosts = deductions.premisesRunningCosts.map(_.amount),
        maintenanceCosts = deductions.maintenanceCosts.map(_.amount),
        adminCosts = deductions.adminCosts.map(_.amount),
        advertisingCosts = deductions.advertisingCosts.map(_.amount),
        businessEntertainmentCosts = deductions.businessEntertainmentCosts.map(_.amount),
        interest = deductions.interest.map(_.amount),
        financialCharges = deductions.financialCharges.map(_.amount),
        badDebt = deductions.badDebt.map(_.amount),
        professionalFees = deductions.professionalFees.map(_.amount),
        depreciation = deductions.depreciation.map(_.amount),
        other = deductions.other.map(_.amount)
      )
    }

    SelfEmploymentAnnualSummary(allowances, adjustments, disallowableExpenses, nonFinancials)
  }
}
