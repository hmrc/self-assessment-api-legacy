/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, AccountingType, TaxYear}

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

    private def errorWithMessage(code: String, message: String) =
      s"""
         |{
         |  "code": "$code",
         |  "message": "$message"
         |}
       """.stripMargin

    val invalidNino: String =
      errorWithMessage("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    val ninoInvalid: String = errorWithMessage("NINO_INVALID", "The provided Nino is invalid")
    val invalidPayload: String =
      errorWithMessage("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
    val invalidRequest: String = errorWithMessage("INVALID_REQUEST", "Invalid request")
    val ninoNotFound: String =
      errorWithMessage("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val desNotFound: String =
      errorWithMessage("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
    val duplicateTradingName: String = errorWithMessage("CONFLICT", "Duplicated trading name.")
    val notFound: String = errorWithMessage("NOT_FOUND", "Resource was not found")
    val invalidPeriod: String = businessErrorWithMessage(
      "INVALID_PERIOD" -> "Periods should be contiguous and have no gaps between one another")
    val overlappingPeriod: String = businessErrorWithMessage(
      "OVERLAPPING_PERIOD" -> "Period overlaps with existing periods.")
    val nonContiguousPeriod: String = businessErrorWithMessage(
      "NOT_CONTIGUOUS_PERIOD" -> "Periods should be contiguous.")
    val misalignedPeriod: String = businessErrorWithMessage(
      "MISALIGNED_PERIOD" -> "Period is not within the accounting period.")
    val misalignedAndOverlappingPeriod: String = businessErrorWithMessage(
      "MISALIGNED_PERIOD" -> "Period is not within the accounting period.",
      "OVERLAPPING_PERIOD" -> "Period overlaps with existing periods.")
    val invalidOriginatorId: String =
      errorWithMessage("INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")
    val internalServerError: String = errorWithMessage("INTERNAL_SERVER_ERROR", "An internal server error occurred")
    val invalidCalcId: String = errorWithMessage("INVALID_CALCID", "Submission has not passed validation")
    val unauthorised: String = errorWithMessage("UNAUTHORIZED", "Bearer token is missing or not authorized")
    val clientNotSubscribed: String = errorWithMessage("CLIENT_NOT_SUBSCRIBED", "The client is not subscribed to MTD")
    val agentNotAuthorised: String = errorWithMessage("AGENT_NOT_AUTHORIZED", "The agent is not authorized")
    val agentNotSubscribed: String =
      errorWithMessage("AGENT_NOT_SUBSCRIBED", "The agent is not subscribed to agent services")
    val notAllowedConsolidatedExpenses = businessErrorWithMessage(
      "NOT_ALLOWED_CONSOLIDATED_EXPENSES" -> "The submission contains consolidated expenses but the accumulative turnover amount exceeds the threshold")
    val invalidTaxYear: String = businessErrorWithMessage(
      "TAX_YEAR_INVALID" -> "The provided tax year is not valid.")

    val resourceGone: String = errorWithMessage("RESOURCE_GONE", "This resource is no longer available")

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

    def businessErrorWithMessage(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "BUSINESS_ERROR",
         |  "message": "Business validation error",
         |  "errors": [
         |    ${errors
           .map {
             case (code, msg) => errorWithMessage(code, msg)
           }
           .mkString(",")}
         |  ]
         |}
         """.stripMargin
    }
  }

  object Banks {
    def apply(accountName: String = "Savings Account"): JsValue = {
      Json.obj("accountName" -> accountName)
    }

    def annualSummary(taxedUkInterest: Option[BigDecimal], untaxedUkInterest: Option[BigDecimal]): JsValue = {
      taxedUkInterest
        .fold[JsObject](Json.obj())(v => Json.obj("taxedUkInterest" -> v)) ++
      untaxedUkInterest
        .fold(Json.obj())(v => Json.obj("untaxedUkInterest" -> v))
    }
  }

  object Properties {
    def apply(): JsValue = Json.obj()

    def consolidationPeriod(fromDate: Option[String] = None,
                            toDate: Option[String] = None,
                            rentIncome: BigDecimal = 0,
                            rentIncomeTaxDeducted: BigDecimal = 0,
                            premiumsOfLeaseGrant: Option[BigDecimal] = None,
                            reversePremiums: BigDecimal = 0,
                            otherPropertyIncome: Option[BigDecimal] = None,
                            premisesRunningCosts: Option[BigDecimal] = None,
                            repairsAndMaintenance: Option[BigDecimal] = None,
                            financialCosts: Option[BigDecimal] = None,
                            professionalFees: Option[BigDecimal] = None,
                            costOfServices: Option[BigDecimal] = None,
                            otherCost: Option[BigDecimal] = None,
                            residentialFinancialCost: Option[BigDecimal] = None,
                            consolidatedExpenses: Option[BigDecimal] = None): JsValue = {

      val from =
        fromDate
          .map { date =>
            s"""
               | "from": "$date",
         """.stripMargin
          }
          .getOrElse("")

      val to =
        toDate
          .map { date =>
            s"""
               | "to": "$date",
         """.stripMargin
          }
          .getOrElse("")

      val ce =
        consolidatedExpenses
          .map { ce =>
            s"""
               | "consolidatedExpenses": { "amount": $ce }
         """.stripMargin
          }
          .getOrElse("")

      Json.parse(s"""
                    |{
                    |  $from
                    |  $to
                    |  "incomes": {
                    |    "rentIncome": { "amount": $rentIncome, "taxDeducted": $rentIncomeTaxDeducted },
                    |    ${premiumsOfLeaseGrant
                      .map(income => s""" "premiumsOfLeaseGrant": { "amount": $income },""")
                      .getOrElse("")}
                    |    "reversePremiums": { "amount": $reversePremiums }
                    |  },
                    |  "expenses": {
                    |    $ce
                    |  }
                    |}
       """.stripMargin)
    }

    def residentialPeriod(fromDate: Option[String] = None,
                          toDate: Option[String] = None,
                          rentIncome: BigDecimal = 0,
                          rentIncomeTaxDeducted: BigDecimal = 0,
                          premiumsOfLeaseGrant: Option[BigDecimal] = None,
                          reversePremiums: BigDecimal = 0,
                          otherPropertyIncome: Option[BigDecimal] = None,
                          premisesRunningCosts: Option[BigDecimal] = None,
                          repairsAndMaintenance: Option[BigDecimal] = None,
                          financialCosts: Option[BigDecimal] = None,
                          professionalFees: Option[BigDecimal] = None,
                          costOfServices: Option[BigDecimal] = None,
                          otherCost: Option[BigDecimal] = None,
                          residentialFinancialCost: Option[BigDecimal] = None,
                          consolidatedExpenses: Option[BigDecimal] = None): JsValue = {

      val from =
        fromDate
          .map { date =>
            s"""
               | "from": "$date",
         """.stripMargin
          }
          .getOrElse("")

      val to =
        toDate
          .map { date =>
            s"""
               | "to": "$date",
         """.stripMargin
          }
          .getOrElse("")

      val rfc =
        residentialFinancialCost
          .map { rfc =>
            s"""
               | "residentialFinancialCost": { "amount": $rfc }
         """.stripMargin
          }
          .getOrElse("")

      Json.parse(s"""
                    |{
                    |  $from
                    |  $to
                    |  "incomes": {
                    |    "rentIncome": { "amount": $rentIncome, "taxDeducted": $rentIncomeTaxDeducted },
                    |    ${premiumsOfLeaseGrant
                      .map(income => s""" "premiumsOfLeaseGrant": { "amount": $income },""")
                      .getOrElse("")}
                    |    "reversePremiums": { "amount": $reversePremiums }
                    |  },
                    |  "expenses": {
                    |    $rfc
                    |  }
                    |}
       """.stripMargin)
    }

    def periodSummary(dates: (String, String)*): JsValue = {
      val nestedDates = dates
        .map { date =>
          s"""
             |{
             |  "id": "${date._1}_${date._2}",
             |  "from": "${date._1}",
             |  "to": "${date._2}"
             |}
           """.stripMargin
        }
        .mkString(",")

      Json.parse(
        s"""
           |[
           |  $nestedDates
           |]
         """.stripMargin
      )
    }

    def fhlAnnualSummary(annualInvestmentAllowance: BigDecimal = 0.0,
                         otherCapitalAllowance: BigDecimal = 0.0,
                         lossBroughtForward: BigDecimal = 0.0,
                         privateUseAdjustment: BigDecimal = 0.0,
                         balancingCharge: BigDecimal = 0.0,
                         periodOfGraceAdjustment: Boolean = false): JsValue = {
      Json.parse(s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "otherCapitalAllowance": $otherCapitalAllowance
           |  },
           |  "adjustments": {
           |   "lossBroughtForward": $lossBroughtForward,
           |   "privateUseAdjustment": $privateUseAdjustment,
           |   "balancingCharge": $balancingCharge,
           |   "periodOfGraceAdjustment": $periodOfGraceAdjustment
           |  }
           |}
    """.stripMargin)
    }

    val invalidFhlAnnualSummary: JsValue = {
      Json.parse(
        """
          |{
          | "adjustments" : {
          |   "periodOfGraceAdjustment" : "not a boolean"
          | }
          |}
        """.stripMargin
      )
    }

    def otherAnnualSummary(annualInvestmentAllowance: BigDecimal = 0.0,
                           otherCapitalAllowance: BigDecimal = 0.0,
                           zeroEmissionsGoodsVehicleAllowance: BigDecimal = 0.0,
                           costOfReplacingDomesticItems: BigDecimal = 0.0,
                           lossBroughtForward: BigDecimal = 0.0,
                           privateUseAdjustment: BigDecimal = 0.0,
                           balancingCharge: BigDecimal = 0.0): JsValue = {
      Json.parse(s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "otherCapitalAllowance": $otherCapitalAllowance,
           |    "costOfReplacingDomesticItems": $costOfReplacingDomesticItems,
           |    "zeroEmissionsGoodsVehicleAllowance": $zeroEmissionsGoodsVehicleAllowance
           |  },
           |  "adjustments": {
           |   "lossBroughtForward": $lossBroughtForward,
           |   "privateUseAdjustment": $privateUseAdjustment,
           |   "balancingCharge": $balancingCharge
           |  }
           |}
    """.stripMargin)
    }

  }

  object SelfEmployment {
    def apply(accPeriodStart: String = "2017-04-06",
              accPeriodEnd: String = "2018-04-05",
              accountingType: String = "CASH",
              commencementDate: Option[String] = Some("2017-01-01"),
              cessationDate: Option[String] = Some("2017-01-02"),
              tradingName: String = "Acme Ltd",
              businessDescription: Option[String] = Some("Accountancy services"),
              businessAddressLineOne: Option[String] = Some("1 Acme Rd."),
              businessAddressLineTwo: Option[String] = Some("London"),
              businessAddressLineThree: Option[String] = Some("Greater London"),
              businessAddressLineFour: Option[String] = Some("United Kingdom"),
              businessPostcode: Option[String] = Some("A9 9AA")): JsValue = {

      val cessation = cessationDate.map(date => s"""
                                                   |  "cessationDate": "$date",
         """.stripMargin).getOrElse("")

      val commencement = commencementDate.map(date => s"""
                                                         |  "commencementDate": "$date",
       """.stripMargin).getOrElse("")

      val businessDesc = businessDescription.map(desc => s"""
                                                            |  "businessDescription": "$desc",
       """.stripMargin).getOrElse("")

      val addrOne = businessAddressLineOne.map(line => s"""
                                                          |  "businessAddressLineOne": "$line",
       """.stripMargin).getOrElse("")

      val addrTwo = businessAddressLineTwo.map(line => s"""
                                                          |  "businessAddressLineTwo": "$line",
       """.stripMargin).getOrElse("")

      val addrThree = businessAddressLineThree.map(line => s"""
                                                              |  "businessAddressLineThree": "$line",
       """.stripMargin).getOrElse("")

      val addrFour = businessAddressLineFour.map(line => s"""
                                                            |  "businessAddressLineFour": "$line",
       """.stripMargin).getOrElse("")

      val addrPostcode = businessPostcode.map(code => s"""
                                                         |  "businessPostcode": "$code",
       """.stripMargin).getOrElse("")

      Json.parse(s"""
                    |{
                    |  "accountingPeriod": {
                    |    "start": "$accPeriodStart",
                    |    "end": "$accPeriodEnd"
                    |  },
                    |  $cessation
                    |  $commencement
                    |  $businessDesc
                    |  $addrOne
                    |  $addrTwo
                    |  $addrThree
                    |  $addrFour
                    |  $addrPostcode
                    |  "accountingType": "$accountingType",
                    |  "tradingName": "$tradingName"
                    |}
         """.stripMargin)

    }

    def selfEmploymentJson(accPeriodStart: String = "2017-04-06",
                           accPeriodEnd: String = "2018-04-05",
                           accountingType: String = "CASH",
                           commencementDate: Option[String] = Some("2017-01-01"),
                           cessationDate: Option[String] = Some("2017-01-02"),
                           tradingName: String = "Acme Ltd",
                           businessDescription: Option[String] = None,
                           businessAddressLineOne: Option[String] = Some("1 Acme Rd."),
                           businessAddressLineTwo: Option[String] = Some("London"),
                           businessAddressLineThree: Option[String] = Some("Greater London"),
                           businessAddressLineFour: Option[String] = Some("United Kingdom"),
                           businessPostcode: Option[String] = Some("A9 9AA")) = {

      val selfEmployment = uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmployment(
        None,
        AccountingPeriod(LocalDate.parse(accPeriodStart), LocalDate.parse(accPeriodEnd)),
        AccountingType.fromDes(accountingType).getOrElse(AccountingType.CASH),
        LocalDate.parse(commencementDate.get),
        Some(LocalDate.parse(cessationDate.get)),
        tradingName,
        businessDescription,
        businessAddressLineOne.get,
        businessAddressLineTwo,
        businessAddressLineThree,
        businessAddressLineFour,
        businessPostcode.get
      )

      selfEmployment
    }

    def selfEmploymentUpdateJson(tradingName: String = "Acme Ltd",
                           businessDescription: String = "Accountancy services",
                           businessAddressLineOne: String = "1 Acme Rd.",
                           businessAddressLineTwo: String = "London",
                           businessAddressLineThree: String = "Greater London",
                           businessAddressLineFour: String = "United Kingdom",
                           businessPostcode: String = "A9 9AA") = {

      val selfEmploymentUpdate = uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentUpdate(
        tradingName,
        businessDescription,
        businessAddressLineOne,
        Some(businessAddressLineTwo),
        Some(businessAddressLineThree),
        Some(businessAddressLineFour),
        businessPostcode
      )

      selfEmploymentUpdate
    }

    def update(tradingName: String = "Acme Ltd",
               businessDescription: String = "Accountancy services",
               businessAddressLineOne: String = "1 Acme Rd.",
               businessAddressLineTwo: String = "London",
               businessAddressLineThree: String = "Greater London",
               businessAddressLineFour: String = "United Kingdom",
               businessPostcode: String = "A9 9AA"): JsValue = {

      Json.parse(s"""
                    |{
                    |  "tradingName": "$tradingName",
                    |  "businessDescription": "$businessDescription",
                    |  "businessAddressLineOne": "$businessAddressLineOne",
                    |  "businessAddressLineTwo": "$businessAddressLineTwo",
                    |  "businessAddressLineThree": "$businessAddressLineThree",
                    |  "businessAddressLineFour": "$businessAddressLineFour",
                    |  "businessPostcode": "$businessPostcode"
                    |}
         """.stripMargin)
    }

    def annualSummary(annualInvestmentAllowance: BigDecimal = 500.25,
                      businessPremisesRenovationAllowance: BigDecimal = 500.25,
                      capitalAllowanceMainPool: BigDecimal = 500.25,
                      capitalAllowanceSpecialRatePool: BigDecimal = 500.25,
                      enhancedCapitalAllowance: BigDecimal = 500.25,
                      allowanceOnSales: BigDecimal = 500.25,
                      zeroEmissionGoodsVehicleAllowance: BigDecimal = 500.25,
                      includedNonTaxableProfits: BigDecimal = 500.25,
                      basisAdjustment: BigDecimal = 500.25,
                      overlapReliefUsed: BigDecimal = 500.25,
                      accountingAdjustment: BigDecimal = 500.25,
                      averagingAdjustment: BigDecimal = 500.25,
                      lossBroughtForward: BigDecimal = 500.25,
                      outstandingBusinessIncome: BigDecimal = 500.25,
                      balancingChargeBPRA: BigDecimal = 500.25,
                      balancingChargeOther: BigDecimal = 500.25,
                      goodsAndServicesOwnUse: BigDecimal = 500.25,
                      capitalAllowanceSingleAssetPool: BigDecimal = 500.25,
                      businessDetailsChangedRecently: Boolean = true,
                      payVoluntaryClass2Nic: Boolean = false,
                      isExempt: Boolean = true,
                      exemptionCode: String = "003"): JsValue = {
      Json.parse(s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "businessPremisesRenovationAllowance": $businessPremisesRenovationAllowance,
           |    "capitalAllowanceMainPool": $capitalAllowanceMainPool,
           |    "capitalAllowanceSpecialRatePool": $capitalAllowanceSpecialRatePool,
           |    "enhancedCapitalAllowance": $enhancedCapitalAllowance,
           |    "allowanceOnSales": $allowanceOnSales,
           |    "zeroEmissionGoodsVehicleAllowance": $zeroEmissionGoodsVehicleAllowance,
           |    "capitalAllowanceSingleAssetPool": $capitalAllowanceSingleAssetPool
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
           |  },
           |  "nonFinancials": {
           |    "class4NicInfo": {
           |      "isExempt": $isExempt,
           |      "exemptionCode": "$exemptionCode"
           |    },
           |    "payVoluntaryClass2Nic": $payVoluntaryClass2Nic
           |  }
           |}
       """.stripMargin)
    }

    def periodWithSimplifiedExpenses(fromDate: Option[String] = None,
                                     toDate: Option[String] = None,
                                     turnover: BigDecimal = 0,
                                     otherIncome: BigDecimal = 0,
                                     consolidatedExpenses: Option[BigDecimal]) = {

      val (from, to) = fromToDates(fromDate, toDate)

      Json.parse(s"""
                    |{
                    |  $from
                    |  $to
                    |  "incomes": {
                    |    "turnover": { "amount": $turnover },
                    |    "other": { "amount": $otherIncome }
                    |  }
                    |
                    |  ${consolidatedExpenses.fold("")(se => s""","consolidatedExpenses": $se""")}
                    |
                    |}
                  """.stripMargin)
    }

    private def fromToDates(fromDate: Option[String], toDate: Option[String]) = {
      (fromDate
         .map { date =>
           s"""
               | "from": "$date",
         """.stripMargin
         }
         .getOrElse(""),
       toDate
         .map { date =>
           s"""
               | "to": "$date",
         """.stripMargin
         }
         .getOrElse(""))
    }

    def period(fromDate: Option[String] = None,
               toDate: Option[String] = None,
               turnover: BigDecimal = 10.10,
               otherIncome: BigDecimal = 10.10,
               costOfGoodsBought: (BigDecimal, BigDecimal) = (10.10, 10.10),
               cisPaymentsToSubcontractors: (BigDecimal, BigDecimal) = (10.10, 10.10),
               staffCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               travelCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               premisesRunningCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               maintenanceCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               adminCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               advertisingCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               interest: (BigDecimal, BigDecimal) = (10.10, 10.10),
               financialCharges: (BigDecimal, BigDecimal) = (10.10, 10.10),
               badDebt: (BigDecimal, BigDecimal) = (10.10, 10.10),
               professionalFees: (BigDecimal, BigDecimal) = (10.10, 10.10),
               businessEntertainmentCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               depreciation: (BigDecimal, BigDecimal) = (10.10, 10.10),
               otherExpenses: (BigDecimal, BigDecimal) = (10.10, 10.10),
               consolidatedExpenses: Option[BigDecimal] = None): JsValue = {

      val (from, to) = fromToDates(fromDate, toDate)

      Json.parse(s"""
           |{
           |  $from
           |  $to
           |  "incomes": {
           |    "turnover": { "amount": $turnover },
           |    "other": { "amount": $otherIncome }
           |  },
           |  "expenses": {
           |    "cisPaymentsToSubcontractors": {
           |      "amount": ${cisPaymentsToSubcontractors._1},
           |      "disallowableAmount": ${cisPaymentsToSubcontractors._2}
           |    },
           |    "depreciation": {
           |      "amount": ${depreciation._1},
           |      "disallowableAmount": ${depreciation._2}
           |    },
           |    "costOfGoodsBought": {
           |      "amount": ${costOfGoodsBought._1},
           |      "disallowableAmount": ${costOfGoodsBought._2}
           |    },
           |    "professionalFees": {
           |      "amount": ${professionalFees._1},
           |      "disallowableAmount": ${professionalFees._2}
           |    },
           |    "businessEntertainmentCosts": {
           |      "amount": ${businessEntertainmentCosts._1},
           |      "disallowableAmount": ${businessEntertainmentCosts._2}
           |    },
           |    "staffCosts": { "amount": ${staffCosts._1}, "disallowableAmount": ${staffCosts._2} },
           |    "travelCosts": { "amount": ${travelCosts._1}, "disallowableAmount": ${travelCosts._2} },
           |    "premisesRunningCosts": { "amount": ${premisesRunningCosts._1}, "disallowableAmount": ${premisesRunningCosts._2} },
           |    "maintenanceCosts": { "amount": ${maintenanceCosts._1}, "disallowableAmount": ${maintenanceCosts._2} },
           |    "adminCosts": { "amount": ${adminCosts._1}, "disallowableAmount": ${adminCosts._2} },
           |    "advertisingCosts": { "amount": ${advertisingCosts._1}, "disallowableAmount": ${advertisingCosts._2} },
           |    "interest": { "amount": ${interest._1}, "disallowableAmount": ${interest._2} },
           |    "financialCharges": { "amount": ${financialCharges._1}, "disallowableAmount": ${financialCharges._2} },
           |    "badDebt": { "amount": ${badDebt._1}, "disallowableAmount": ${badDebt._2} },
           |    "other": { "amount": ${otherExpenses._1}, "disallowableAmount": ${otherExpenses._2} }
           |  }
           |
           |  ${consolidatedExpenses.fold("")(se => s""","consolidatedExpenses": $se""")}
           |
           |}
       """.stripMargin)
    }

    def periodWithNegativeBadDebtsExpenses(fromDate: Option[String] = None,
               toDate: Option[String] = None,
               turnover: BigDecimal = 10.10,
               otherIncome: BigDecimal = 10.10,
               costOfGoodsBought: (BigDecimal, BigDecimal) = (10.10, 10.10),
               cisPaymentsToSubcontractors: (BigDecimal, BigDecimal) = (10.10, 10.10),
               staffCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               travelCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               premisesRunningCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               maintenanceCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               adminCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               advertisingCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               interest: (BigDecimal, BigDecimal) = (10.10, 10.10),
               financialCharges: (BigDecimal, BigDecimal) = (10.10, 10.10),
               badDebt: (BigDecimal, BigDecimal) = (10.10, 10.10),
               professionalFees: (BigDecimal, BigDecimal) = (10.10, 10.10),
               businessEntertainmentCosts: (BigDecimal, BigDecimal) = (10.10, 10.10),
               depreciation: (BigDecimal, BigDecimal) = (10.10, 10.10),
               otherExpenses: (BigDecimal, BigDecimal) = (10.10, 10.10),
               consolidatedExpenses: Option[BigDecimal] = None): JsValue = {

      val (from, to) = fromToDates(fromDate, toDate)

      Json.parse(s"""
                    |{
                    |  $from
                    |  $to
                    |  "incomes": {
                    |    "turnover": { "amount": $turnover },
                    |    "other": { "amount": $otherIncome }
                    |  },
                    |  "expenses": {
                    |    "cisPaymentsToSubcontractors": {
                    |      "amount": ${cisPaymentsToSubcontractors._1},
                    |      "disallowableAmount": ${cisPaymentsToSubcontractors._2}
                    |    },
                    |    "depreciation": {
                    |      "amount": ${depreciation._1},
                    |      "disallowableAmount": ${depreciation._2}
                    |    },
                    |    "costOfGoodsBought": {
                    |      "amount": ${costOfGoodsBought._1},
                    |      "disallowableAmount": ${costOfGoodsBought._2}
                    |    },
                    |    "professionalFees": {
                    |      "amount": ${professionalFees._1},
                    |      "disallowableAmount": ${professionalFees._2}
                    |    },
                    |    "businessEntertainmentCosts": {
                    |      "amount": ${businessEntertainmentCosts._1},
                    |      "disallowableAmount": ${businessEntertainmentCosts._2}
                    |    },
                    |    "staffCosts": { "amount": ${staffCosts._1}, "disallowableAmount": ${staffCosts._2} },
                    |    "travelCosts": { "amount": ${travelCosts._1}, "disallowableAmount": ${travelCosts._2} },
                    |    "premisesRunningCosts": { "amount": ${premisesRunningCosts._1}, "disallowableAmount": ${premisesRunningCosts._2} },
                    |    "maintenanceCosts": { "amount": ${maintenanceCosts._1}, "disallowableAmount": ${maintenanceCosts._2} },
                    |    "adminCosts": { "amount": ${adminCosts._1}, "disallowableAmount": ${adminCosts._2} },
                    |    "advertisingCosts": { "amount": ${advertisingCosts._1}, "disallowableAmount": ${advertisingCosts._2} },
                    |    "interest": { "amount": ${interest._1}, "disallowableAmount": ${interest._2} },
                    |    "financialCharges": { "amount": ${financialCharges._1}, "disallowableAmount": ${financialCharges._2} },
                    |    "badDebt": { "amount": ${badDebt._1}, "disallowableAmount": ${badDebt._2} },
                    |    "other": { "amount": ${otherExpenses._1}, "disallowableAmount": ${otherExpenses._2} }
                    |  }
                    |
           |  ${consolidatedExpenses.fold("")(se => s""","consolidatedExpenses": $se""")}
                    |
           |}
       """.stripMargin)
    }

  }

  object Dividends {
    def apply(amount: BigDecimal = 500.45): JsValue = {
      Json.obj("ukDividends" -> amount)
    }
  }

  object Crystallisation {
    def intentToCrystallise(): JsValue = Json.obj()

    def crystallisationRequest(calcId: String = "77427777"): JsValue = {
      Json.obj("calculationId" -> calcId)
    }
  }

  object Obligations {

    def crystallisationObligations(id: String, taxYear: TaxYear): String =
      s"""
         |{
         |    "obligations": [
         |        {
         |            "identification": {
         |                "incomeSourceType": "ITSA",
         |                "referenceNumber": "$id",
         |                "referenceType": "NINO"
         |            },
         |            "obligationDetails": [
         |                {
         |                    "status": "F",
         |                    "inboundCorrespondenceFromDate": "2016-04-06",
         |                    "inboundCorrespondenceToDate": "2017-04-05",
         |                    "inboundCorrespondenceDueDate": "2018-01-31",
         |                    "periodKey": "16P0",
         |                    "inboundCorrespondenceDateReceived": "2018-01-01"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "${taxYear.taxYearFromDate}",
         |                    "inboundCorrespondenceToDate": "${taxYear.taxYearToDate}",
         |                    "inboundCorrespondenceDueDate": "2019-01-31",
         |                    "periodKey": "17P0"
         |                }
         |            ]
         |        },
         |        {
         |            "identification": {
         |                "incomeSourceType": "ITSB",
         |                "referenceNumber": "XDIS00000000166",
         |                "referenceType": "MTDBIS"
         |            },
         |            "obligationDetails": [
         |                {
         |                    "status": "F",
         |                    "inboundCorrespondenceFromDate": "2017-04-06",
         |                    "inboundCorrespondenceToDate": "2017-07-05",
         |                    "inboundCorrespondenceDueDate": "2017-08-05",
         |                    "periodKey": "#001",
         |                    "inboundCorrespondenceDateReceived": "2017-06-30"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-07-06",
         |                    "inboundCorrespondenceToDate": "2017-10-05",
         |                    "inboundCorrespondenceDueDate": "2017-11-05",
         |                    "periodKey": "#002"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-10-06",
         |                    "inboundCorrespondenceToDate": "2018-01-05",
         |                    "inboundCorrespondenceDueDate": "2018-02-05",
         |                    "periodKey": "#003"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2018-01-06",
         |                    "inboundCorrespondenceToDate": "2018-04-05",
         |                    "inboundCorrespondenceDueDate": "2018-05-05",
         |                    "periodKey": "#004"
         |                }
         |            ]
         |        },
         |        {
         |            "identification": {
         |                "incomeSourceType": "ITSP",
         |                "referenceNumber": "XGIS00000000169",
         |                "referenceType": "MTDBIS"
         |            },
         |            "obligationDetails": [
         |                {
         |                    "status": "F",
         |                    "inboundCorrespondenceFromDate": "2017-04-06",
         |                    "inboundCorrespondenceToDate": "2017-07-05",
         |                    "inboundCorrespondenceDueDate": "2017-08-05",
         |                    "periodKey": "#001",
         |                    "inboundCorrespondenceDateReceived": "2017-06-30"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-07-06",
         |                    "inboundCorrespondenceToDate": "2017-10-05",
         |                    "inboundCorrespondenceDueDate": "2017-11-05",
         |                    "periodKey": "#002"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-10-06",
         |                    "inboundCorrespondenceToDate": "2018-01-05",
         |                    "inboundCorrespondenceDueDate": "2018-02-05",
         |                    "periodKey": "#003"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2018-01-06",
         |                    "inboundCorrespondenceToDate": "2018-04-05",
         |                    "inboundCorrespondenceDueDate": "2018-05-05",
         |                    "periodKey": "#004"
         |                }
         |            ]
         |        }
         |    ]
         |}
       """.stripMargin

    def apply(firstMet: Boolean = false,
              secondMet: Boolean = false,
              thirdMet: Boolean = false,
              fourthMet: Boolean = false): JsValue = {
      Json.parse(s"""
           |{
           |  "obligations": [
           |    {
           |      "start": "2017-04-06",
           |      "end": "2017-07-05",
           |      "due": "2017-08-05",
           |      "met": $firstMet
           |    },
           |    {
           |      "start": "2017-07-06",
           |      "end": "2017-10-05",
           |      "due": "2017-11-05",
           |      "met": $secondMet
           |    },
           |    {
           |      "start": "2017-10-06",
           |      "end": "2018-01-05",
           |      "due": "2018-02-05",
           |      "met": $thirdMet
           |    },
           |    {
           |      "start": "2018-01-06",
           |      "end": "2018-04-05",
           |      "due": "2018-05-06",
           |      "met": $fourthMet
           |    }
           |  ]
           |}
         """.stripMargin)
    }

    val eops: JsValue = {
      Json.parse(s"""
           |{
           |  "obligations": [
           |    {
           |      "start": "2017-04-06",
           |      "end": "2018-04-05",
           |      "due": "2018-05-06",
           |      "status": "Open"
           |    }
           |  ]
           |}
         """.stripMargin)
    }
  }

  object CharitableGivings {
    def apply(currentYear: BigDecimal = 10000.32,
              oneOffCurrentYear: BigDecimal = 1000.23,
              currentYearTreatedAsPreviousYear: BigDecimal = 300.27,
              nextYearTreatedAsCurrentYear: BigDecimal = 400.13,
              nonUKCharities: BigDecimal = 2000.19,
              landAndBuildings: BigDecimal = 700.11,
              sharesOrSecurities: BigDecimal = 600.31,
              investmentsNonUKCharities: BigDecimal =  300.22): JsValue = {
      Json.parse(s"""
                    |  {
                    |    "giftAidPayments": {
                    |        "currentYear": $currentYear,
                    |        "oneOffCurrentYear": $oneOffCurrentYear,
                    |        "currentYearTreatedAsPreviousYear": $currentYearTreatedAsPreviousYear,
                    |        "nextYearTreatedAsCurrentYear": $nextYearTreatedAsCurrentYear,
                    |        "nonUKCharities": $nonUKCharities
                    |    },
                    |    "gifts": {
                    |        "landAndBuildings": $landAndBuildings,
                    |        "sharesOrSecurities": $sharesOrSecurities,
                    |        "investmentsNonUKCharities": $investmentsNonUKCharities
                    |    }
                    |}
         """.stripMargin)
    }
  }
}
