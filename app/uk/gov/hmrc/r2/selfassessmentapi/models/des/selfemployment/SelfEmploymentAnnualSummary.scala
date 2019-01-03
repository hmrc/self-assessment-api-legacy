/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models.des.selfemployment

import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.r2.selfassessmentapi.models


case class SelfEmploymentAnnualSummary(annualAdjustments: Option[AnnualAdjustments],
                                       annualAllowances: Option[AnnualAllowances],
                                       annualNonFinancials: Option[AnnualNonFinancials])

object SelfEmploymentAnnualSummary {
  implicit val reads: Reads[SelfEmploymentAnnualSummary] = Json.reads[SelfEmploymentAnnualSummary]
  implicit val writes: Writes[SelfEmploymentAnnualSummary] = Json.writes[SelfEmploymentAnnualSummary]

  def from(apiSummary: models.selfemployment.SelfEmploymentAnnualSummary): SelfEmploymentAnnualSummary = {
    val adjustments = apiSummary.adjustments.map { adj =>
      AnnualAdjustments(
        includedNonTaxableProfits = adj.includedNonTaxableProfits,
        basisAdjustment = adj.basisAdjustment,
        overlapReliefUsed = adj.overlapReliefUsed,
        accountingAdjustment = adj.accountingAdjustment,
        averagingAdjustment = adj.averagingAdjustment,
        lossBroughtForward = adj.lossBroughtForward,
        outstandingBusinessIncome = adj.outstandingBusinessIncome,
        balancingChargeBpra = adj.balancingChargeBPRA,
        balancingChargeOther = adj.balancingChargeOther,
        goodsAndServicesOwnUse = adj.goodsAndServicesOwnUse
      )
    }

    val allowances = apiSummary.allowances.map { allow =>
      AnnualAllowances(
        annualInvestmentAllowance = allow.annualInvestmentAllowance,
        businessPremisesRenovationAllowance = allow.businessPremisesRenovationAllowance,
        capitalAllowanceMainPool = allow.capitalAllowanceMainPool,
        capitalAllowanceSpecialRatePool = allow.capitalAllowanceSpecialRatePool,
        zeroEmissionGoodsVehicleAllowance = allow.zeroEmissionGoodsVehicleAllowance,
        enhanceCapitalAllowance = allow.enhancedCapitalAllowance,
        allowanceOnSales = allow.allowanceOnSales,
        capitalAllowanceSingleAssetPool = allow.capitalAllowanceSingleAssetPool,
        tradingAllowance = allow.tradingAllowance
      )
    }

    val nonFinancials = apiSummary.nonFinancials.map { info =>
      AnnualNonFinancials(
        businessDetailsChangedRecently = None,
        payClass2Nics = info.payVoluntaryClass2Nic,
        exemptFromPayingClass4Nics = info.class4NicInfo.flatMap(_.isExempt),
        class4NicsExemptionReason = for {
          class4Nics <- info.class4NicInfo
          exemptionCode <- class4Nics.exemptionCode
        } yield exemptionCode.toString
      )
    }

    SelfEmploymentAnnualSummary(
      annualAdjustments = adjustments,
      annualAllowances = allowances,
      annualNonFinancials = nonFinancials)
  }

}

case class AnnualAdjustments(includedNonTaxableProfits: Option[BigDecimal],
                             basisAdjustment: Option[BigDecimal],
                             overlapReliefUsed: Option[BigDecimal],
                             accountingAdjustment: Option[BigDecimal],
                             averagingAdjustment: Option[BigDecimal],
                             lossBroughtForward: Option[BigDecimal],
                             outstandingBusinessIncome: Option[BigDecimal],
                             balancingChargeBpra: Option[BigDecimal],
                             balancingChargeOther: Option[BigDecimal],
                             goodsAndServicesOwnUse: Option[BigDecimal])

object AnnualAdjustments {
  implicit val reads: Reads[AnnualAdjustments] = Json.reads[AnnualAdjustments]
  implicit val writes: Writes[AnnualAdjustments] = Json.writes[AnnualAdjustments]
}

case class AnnualAllowances(annualInvestmentAllowance: Option[BigDecimal],
                            businessPremisesRenovationAllowance: Option[BigDecimal],
                            capitalAllowanceMainPool: Option[BigDecimal],
                            capitalAllowanceSpecialRatePool: Option[BigDecimal],
                            zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
                            enhanceCapitalAllowance: Option[BigDecimal],
                            allowanceOnSales: Option[BigDecimal],
                            capitalAllowanceSingleAssetPool: Option[BigDecimal],
                            tradingAllowance: Option[BigDecimal])

object AnnualAllowances {
  implicit val reads: Reads[AnnualAllowances] = Json.reads[AnnualAllowances]
  implicit val writes: Writes[AnnualAllowances] = Json.writes[AnnualAllowances]
}

case class AnnualNonFinancials(businessDetailsChangedRecently: Option[Boolean],
                               payClass2Nics: Option[Boolean],
                               exemptFromPayingClass4Nics: Option[Boolean],
                               class4NicsExemptionReason: Option[String])

object AnnualNonFinancials{
  implicit val reads: Reads[AnnualNonFinancials] = Json.reads[AnnualNonFinancials]
  implicit val writes: Writes[AnnualNonFinancials] = Json.writes[AnnualNonFinancials]

}
