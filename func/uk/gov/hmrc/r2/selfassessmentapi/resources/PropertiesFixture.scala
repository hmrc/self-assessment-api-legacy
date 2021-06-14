package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.utils.Nino

object PropertiesFixture {

  def misalignedAndOverlappingPeriod(propertyType: PropertyType): JsValue =
    period(propertyType, from = Some("2017-08-04"), to = Some("2017-09-04"))

  def misalignedPeriod(propertyType: PropertyType): JsValue =
    period(propertyType, from = Some("2017-08-04"), to = Some("2017-09-04"))

  def bothExpenses(propertyType: PropertyType): JsValue = period(propertyType, consolidatedExpenses = Some(100.50))

  def bothExpensesUpdate(propertyType: PropertyType): JsValue =
    period(propertyType, from = None, to = None, consolidatedExpenses = Some(100.50))

  def period(propertyType: PropertyType,
             from: Option[String] = Some("2017-04-06"),
             to: Option[String] = Some("2018-04-05"),
             consolidatedExpenses: Option[BigDecimal] = None,
             costOfServices: Option[BigDecimal] = None,
             onlyConsolidated: Boolean = false,
             onlyResidential: Boolean = false,
             noExpenses: Boolean = false,
             overConsolidatedExpenses: Boolean = false): JsValue = propertyType match {
    case PropertyType.FHL if noExpenses =>
      Jsons.Properties.fhlPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500
      )
    case PropertyType.FHL if overConsolidatedExpenses =>
      Jsons.Properties.fhlPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        consolidatedExpenses = Some(99999999999999.50)
      )
    case PropertyType.FHL =>
      Jsons.Properties.fhlPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        premisesRunningCosts = Some(100.50),
        repairsAndMaintenance = Some(100.50),
        financialCosts = Some(100),
        professionalFees = Some(100.50),
        otherCost = Some(100.50),
        costOfServices = costOfServices,
        consolidatedExpenses = consolidatedExpenses
      )
    case PropertyType.OTHER if noExpenses =>
      Jsons.Properties.consolidationPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        otherPropertyIncome = Some(13.10)
      )
    case PropertyType.OTHER if onlyConsolidated =>
      Jsons.Properties.consolidationPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        otherPropertyIncome = Some(13.10),
        consolidatedExpenses = Some(100.55),
        broughtFwdResidentialFinancialCost = Some(13.10)
      )
    case PropertyType.OTHER if onlyResidential =>
      Jsons.Properties.residentialPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        otherPropertyIncome = Some(13.10),
        residentialFinancialCost = Some(100.55)
      )
    case PropertyType.OTHER if overConsolidatedExpenses =>
      Jsons.Properties.otherPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        otherPropertyIncome = Some(13.10),
        consolidatedExpenses = Some(99999999999999.50)
      )
    case PropertyType.OTHER =>
      Jsons.Properties.otherPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        otherPropertyIncome = Some(13.10),
        premisesRunningCosts = Some(100.50),
        repairsAndMaintenance = Some(100.50),
        financialCosts = Some(100),
        professionalFees = Some(100.50),
        costOfServices = costOfServices,
        otherCost = Some(100.50),
        residentialFinancialCost = Some(100.50),
        consolidatedExpenses = consolidatedExpenses
      )
  }

  def invalidPeriod(propertyType: PropertyType): JsValue = propertyType match {
    case PropertyType.FHL =>
      Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-01"),
        toDate = Some("02-04-2017"),
        rentIncome = -500,
        financialCosts = Some(400.456))
    case PropertyType.OTHER =>
      Jsons.Properties.otherPeriod(
        fromDate = Some("2017-04-01"),
        toDate = Some("02-04-2017"),
        rentIncome = -500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(-200.22),
        reversePremiums = 22.35
      )
  }

  def expectedJson(propertyType: PropertyType): JsValue = propertyType match {
    case PropertyType.OTHER =>
      Json.parse(
        """
          |{"code":"INVALID_REQUEST","message":"Invalid request","errors":[{"code":"INVALID_MONETARY_AMOUNT","message":"Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places","path":"/incomes/premiumsOfLeaseGrant/amount"},{"code":"INVALID_MONETARY_AMOUNT","message":"Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places","path":"/incomes/rentIncome/amount"},{"code":"INVALID_DATE","message":"please provide a date in ISO format (i.e. YYYY-MM-DD)","path":"/to"}]}
          |""".stripMargin)
    case PropertyType.FHL => Json.parse(
      """
        |{"code":"INVALID_REQUEST","message":"Invalid request","errors":[{"code":"INVALID_MONETARY_AMOUNT","message":"Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places","path":"/expenses/financialCosts/amount"},{"code":"INVALID_MONETARY_AMOUNT","message":"Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places","path":"/incomes/rentIncome/amount"},{"code":"INVALID_DATE","message":"please provide a date in ISO format (i.e. YYYY-MM-DD)","path":"/to"}]}
        |""".stripMargin)
  }

  def expectedJsonforUpdatePeriod(propertyType: PropertyType): JsValue = propertyType match {
    case PropertyType.OTHER =>
      Json.parse(
        """
          |{
          |	"code": "INVALID_REQUEST",
          |	"message": "Invalid request",
          |	"errors": [{
          |		"code": "INVALID_MONETARY_AMOUNT",
          |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
          |		"path": "/incomes/rentIncome/amount"
          |	}, {
          |		"code": "INVALID_MONETARY_AMOUNT",
          |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
          |		"path": "/incomes/premiumsOfLeaseGrant/amount"
          |	}]
          |}
          |""".stripMargin)
    case PropertyType.FHL => Json.parse(
      """
        |{
        |	"code": "INVALID_REQUEST",
        |	"message": "Invalid request",
        |	"errors": [{
        |		"code": "INVALID_MONETARY_AMOUNT",
        |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
        |		"path": "/expenses/financialCosts/amount"
        |	}, {
        |		"code": "INVALID_MONETARY_AMOUNT",
        |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
        |		"path": "/incomes/rentIncome/amount"
        |	}]
        |}
        |""".stripMargin)
  }

  def expectedBody(propertyType: PropertyType): String = propertyType match {
    case PropertyType.FHL =>
      """
        |{"from":"2017-04-05","to":"2018-04-04","incomes":{"rentIncome":{"amount":200,"taxDeducted":0},"rarRentReceived":{"amount":100}},"expenses":{"premisesRunningCosts":{"amount":200},"repairsAndMaintenance":{"amount":200},"financialCosts":{"amount":200},"professionalFees":{"amount":200},"costOfServices":{"amount":200},"consolidatedExpenses":{"amount":0},"other":{"amount":200},"rarReliefClaimed":{"amount":100}}}
        |""".stripMargin

    case PropertyType.OTHER =>
      """
        |{"from":"2017-04-05","to":"2018-04-04","incomes":{"rentIncome":{"amount":200,"taxDeducted":0},"premiumsOfLeaseGrant":{"amount":200},"reversePremiums":{"amount":200},"otherPropertyIncome":{"amount":200},"rarRentReceived":{"amount":100}},"expenses":{"premisesRunningCosts":{"amount":200},"repairsAndMaintenance":{"amount":200},"financialCosts":{"amount":200},"professionalFees":{"amount":200},"costOfServices":{"amount":200},"consolidatedExpenses":{"amount":0},"residentialFinancialCost":{"amount":200},"other":{"amount":200},"rarReliefClaimed":{"amount":100}}}
        |""".stripMargin
  }

  def periodWillBeReturnedFor(nino: Nino, propertyType: PropertyType, periodId: String = "2017-04-06_2018-04-05"): String =
    propertyType match {
      case PropertyType.FHL =>
        DesJsons.Properties.Period.fhl(
          transactionReference = periodId,
          from = "2017-04-05",
          to = "2018-04-04",
          rentIncome = 200.00,
          amountClaimed = Some(100.00),
          rentsReceived = Some(100.00),
          premisesRunningCosts = 200.00,
          repairsAndMaintenance = 200.00,
          financialCosts = 200.00,
          professionalFees = 200.00,
          costOfServices = 200.00,
          other = 200.00)
          .toString()
      case PropertyType.OTHER =>
        DesJsons.Properties.Period.other(
          transactionReference = periodId,
          from = "2017-04-05",
          to = "2018-04-04",
          rentIncome = 200.00,
          amountClaimed = Some(100.00),
          rentsReceived = Some(100.00),
          premiumsOfLeaseGrant = Some(200.00),
          reversePremiums = Some(200.00),
          otherPropertyIncome = Some(200.00),
          premisesRunningCosts = Some(200.00),
          repairsAndMaintenance = Some(200.00),
          financialCosts = Some(200.00),
          professionalFees = Some(200.00),
          costOfServices = Some(200.00),
          residentialFinancialCost = Some(200.00),
          other = Some(200.00))
          .toString()
    }

  def annualSummary(propertyType: PropertyType.Value): JsValue = propertyType match {
    case PropertyType.OTHER => Jsons.Properties.otherAnnualSummary(rarJointLet = false)
    case PropertyType.FHL => Jsons.Properties.fhlAnnualSummary()
  }

  def invalidAnnualSummary(propertyType: PropertyType.Value): JsValue = propertyType match {
    case PropertyType.OTHER => Jsons.Properties.otherAnnualSummary(
      annualInvestmentAllowance = -10000.50,
      otherCapitalAllowance = 1000.20,
      zeroEmissionsGoodsVehicleAllowance = 50.50,
      costOfReplacingDomesticItems = 150.55,
      lossBroughtForward = 20.22,
      privateUseAdjustment = -22.23,
      balancingCharge = 350.34,
      bpraBalancingCharge = 0.0,
      nonResidentLandlord = true,
      rarJointLet = false
    )
    case PropertyType.FHL => Jsons.Properties.fhlAnnualSummary(
      annualInvestmentAllowance = -10000.50,
      otherCapitalAllowance = 1000.20,
      lossBroughtForward = 20.22,
      privateUseAdjustment = -22.23,
      balancingCharge = 350.34,
      periodOfGraceAdjustment = true)
  }

  def desAnnualSummary(propertyType: PropertyType.Value): String = propertyType match {
    case PropertyType.OTHER => DesJsons.Properties.AnnualSummary.other
    case PropertyType.FHL => DesJsons.Properties.AnnualSummary.fhl
  }
}
