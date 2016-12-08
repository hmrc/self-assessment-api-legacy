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
}
