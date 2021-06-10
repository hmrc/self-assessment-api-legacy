package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.libs.json.JsValue
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType.PropertyType

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

  def expectedJson(propertyType: PropertyType): String = propertyType match {
    case PropertyType.FHL =>
      Jsons.Errors.invalidRequest("INVALID_DATE" -> "/to",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/expenses/financialCosts/amount")
    case PropertyType.OTHER =>
      Jsons.Errors.invalidRequest("INVALID_DATE" -> "/to",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount")
  }

  def expectedBody(propertyType: PropertyType): String = propertyType match {
    case PropertyType.FHL =>
      Jsons.Properties
        .fhlPeriod(
          fromDate = Some("2017-04-05"),
          toDate = Some("2018-04-04"),
          rentIncome = 200.00,
          rarRentReceived = 100.00,
          premisesRunningCosts = Some(200),
          repairsAndMaintenance = Some(200),
          financialCosts = Some(200),
          professionalFees = Some(200),
          costOfServices = Some(200.00),
          otherCost = Some(200)
        )
        .toString()

    case PropertyType.OTHER =>
      Jsons.Properties
        .otherPeriod(
          fromDate = Some("2017-04-05"),
          toDate = Some("2018-04-04"),
          rentIncome = 200.00,
          premiumsOfLeaseGrant = Some(200),
          reversePremiums = 200,
          otherPropertyIncome = Some(200),
          premisesRunningCosts = Some(200),
          repairsAndMaintenance = Some(200),
          financialCosts = Some(200),
          professionalFees = Some(200),
          costOfServices = Some(200),
          residentialFinancialCost = Some(200),
          otherCost = Some(200)
        )
        .toString()
  }
}
