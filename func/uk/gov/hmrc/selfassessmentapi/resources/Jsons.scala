package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}

object Jsons {

  object Errors {

    private def error(error: (String, String)) = {
      s"""
         |    {
         |      "code": "${error._1}",
         |      "path": "${error._2}"
         |    }
         """.stripMargin
    }

    def invalidRequest(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "INVALID_REQUEST",
         |  "message": "Invalid request",
         |  "errors": [
         |    ${errors.map { error }.mkString(",")}
         |  ]
         |}
         """.stripMargin
    }

    def businessError(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "BUSINESS_ERROR",
         |  "message": "Business validation error",
         |  "errors": [
         |    ${errors.map { error }.mkString(",")}
         |  ]
         |}
         """.stripMargin
    }
  }

  def selfEmployment(accPeriodStart: String = "2017-04-06", accPeriodEnd: String = "2018-04-05", accountingType: String = "CASH",
                     commencementDate: String = s"${LocalDate.now.minusDays(1)}"): JsValue = {
    Json.parse(
      s"""
         |{
         |  "accountingPeriod": {
         |    "start": "$accPeriodStart",
         |    "end": "$accPeriodEnd"
         |  },
         |  "accountingType": "$accountingType",
         |  "commencementDate": "$commencementDate"
         |}
         """.stripMargin)
  }

  def selfEmploymentAnnualSummary(annualInvestmentAllowance: BigDecimal = 500.25, capitalAllowanceMainPool: BigDecimal = 500.25,
                                  capitalAllowanceSpecialRatePool: BigDecimal = 500.25, businessPremisesRenovationAllowance: BigDecimal = 500.25,
                                  enhancedCapitalAllowance: BigDecimal = 500.25, allowanceOnSales: BigDecimal = 500.25,
                                  zeroEmissionGoodsVehicleAllowance: BigDecimal = 500.25,
                                  includedNonTaxableProfits: BigDecimal = 500.25, basisAdjustment: BigDecimal = -500.25,
                                  overlapReliefUsed: BigDecimal = 500.25, accountingAdjustment: BigDecimal = 500.25,
                                  averagingAdjustment: BigDecimal = -500.25, lossBroughtForward: BigDecimal = 500.25,
                                  outstandingBusinessIncome: BigDecimal = 500.25, balancingChargeBPRA: BigDecimal = 500.25,
                                  balancingChargeOther: BigDecimal = 500.25, goodsAndServicesOwnUse: BigDecimal = 500.25): JsValue = {
    Json.parse(
      s"""
         |{
         |  "allowances": {
         |    "annualInvestmentAllowance": $annualInvestmentAllowance,
         |    "capitalAllowanceMainPool": $capitalAllowanceMainPool,
         |    "capitalAllowanceSpecialRatePool": $capitalAllowanceSpecialRatePool,
         |    "businessPremisesRenovationAllowance": $businessPremisesRenovationAllowance,
         |    "enhancedCapitalAllowance": $enhancedCapitalAllowance,
         |    "allowanceOnSales": $allowanceOnSales,
         |    "zeroEmissionGoodsVehicleAllowance": $zeroEmissionGoodsVehicleAllowance
         |  },
         |  "adjustments": {
         |    "includedNonTaxableProfits": $includedNonTaxableProfits,
         |    "basisAdjustment": $basisAdjustment,
         |    "overlapReliefUsed": $overlapReliefUsed,
         |    "accountingAdjustment": $accountingAdjustment,
         |    "averagingAdjustment": $averagingAdjustment,
         |    "lossBroughtForward": $lossBroughtForward,
         |    "outstandingBusinessIncome": $outstandingBusinessIncome,
         |    "balancingChargeBPRA": $balancingChargeBPRA,
         |    "balancingChargeOther": $balancingChargeOther,
         |    "goodsAndServicesOwnUse": $goodsAndServicesOwnUse
         |  }
         |}
       """.stripMargin)
  }

  def selfEmploymentPeriod(fromDate: Option[String] = None, toDate: Option[String] = None,
                           turnover: BigDecimal = 0, otherIncome: BigDecimal = 0,
                           costOfGoodsBought: (BigDecimal, BigDecimal) = (0, 0), cisPaymentsToSubcontractors: (BigDecimal, BigDecimal) = (0, 0),
                           staffCosts: (BigDecimal, BigDecimal) = (0, 0), travelCosts: (BigDecimal, BigDecimal) = (0, 0),
                           premisesRunningCosts: (BigDecimal, BigDecimal) = (0, 0), maintenanceCosts: (BigDecimal, BigDecimal) = (0, 0),
                           adminCosts: (BigDecimal, BigDecimal) = (0, 0), advertisingCosts: (BigDecimal, BigDecimal) = (0, 0),
                           interest: (BigDecimal, BigDecimal) = (0, 0), financialCharges: (BigDecimal, BigDecimal) = (0, 0),
                           badDebt: (BigDecimal, BigDecimal) = (0, 0), professionalFees: (BigDecimal, BigDecimal) = (0, 0),
                           depreciation: (BigDecimal, BigDecimal) = (0, 0), otherExpenses: (BigDecimal, BigDecimal) = (0, 0)): JsValue = {

    val from =
      fromDate.map { date =>
        s"""
           | "from": "$date",
         """.stripMargin
      }.getOrElse("")

    val to =
      toDate.map { date =>
        s"""
           | "to": "$date",
         """.stripMargin
      }.getOrElse("")

    Json.parse(
      s"""
         |{
         |  $from
         |  $to
         |  "incomes": {
         |    "turnover": { "amount": $turnover },
         |    "other": { "amount": $otherIncome }
         |  },
         |  "expenses": {
         |    "costOfGoodsBought": { "amount": ${costOfGoodsBought._1}, "disallowableAmount": ${costOfGoodsBought._2} },
         |    "cisPaymentsToSubcontractors": { "amount": ${cisPaymentsToSubcontractors._1}, "disallowableAmount": ${cisPaymentsToSubcontractors._2} },
         |    "staffCosts": { "amount": ${staffCosts._1}, "disallowableAmount": ${staffCosts._2} },
         |    "travelCosts": { "amount": ${travelCosts._1}, "disallowableAmount": ${travelCosts._2} },
         |    "premisesRunningCosts": { "amount": ${premisesRunningCosts._1}, "disallowableAmount": ${premisesRunningCosts._2} },
         |    "maintenanceCosts": { "amount": ${maintenanceCosts._1}, "disallowableAmount": ${maintenanceCosts._2} },
         |    "adminCosts": { "amount": ${adminCosts._1}, "disallowableAmount": ${adminCosts._2} },
         |    "advertisingCosts": { "amount": ${advertisingCosts._1}, "disallowableAmount": ${advertisingCosts._2} },
         |    "interest": { "amount": ${interest._1}, "disallowableAmount": ${interest._2} },
         |    "financialCharges": { "amount": ${financialCharges._1}, "disallowableAmount": ${financialCharges._2} },
         |    "badDebt": { "amount": ${badDebt._1}, "disallowableAmount": ${badDebt._2} },
         |    "professionalFees": { "amount": ${professionalFees._1}, "disallowableAmount": ${professionalFees._2} },
         |    "depreciation": { "amount": ${depreciation._1}, "disallowableAmount": ${depreciation._2} },
         |    "other": { "amount": ${otherExpenses._1}, "disallowableAmount": ${otherExpenses._2} }
         |  }
         |}
       """.stripMargin)
  }
}
