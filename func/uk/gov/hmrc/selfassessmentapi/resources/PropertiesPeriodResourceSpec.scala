/*
 * Copyright 2020 HM Revenue & Customs
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

import uk.gov.hmrc.selfassessmentapi.models.Period
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesPeriodResourceSpec extends BaseFunctionalSpec {

  "retrieving all periods" should {

    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {

      s"return code 200 with a JSON array of all $propertyType periods belonging to the property business" in {
        val expectedJson = Jsons.Properties.periodSummary(
          ("2017-07-05", "2017-08-04"),
          ("2017-04-06", "2017-07-04")
        )
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodsWillBeReturnedFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsJson()
          .bodyIsLike(expectedJson.toString)
          .selectFields(_ \\ "id")
          .isLength(2)
          .matches(Period.periodPattern)
      }

      s"return a 200 response with an empty array when an empty $propertyType periods list is returned" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .emptyPeriodsWillBeReturnedFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsJson()
          .jsonBodyIsEmptyArray
      }

      s"return code 404 for an $propertyType property business containing no periods" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .noPeriodsFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 404 for an $propertyType property business that does not exist" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .doesNotExistPeriodFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 500 when we receive an unexpected JSON from DES for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .invalidPeriodsJsonFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(500)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .isATeapotFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(500)
      }

    }
  }

//  def misalignedAndOverlappingPeriod(propertyType: PropertyType) =
//    period(propertyType, from = Some("2017-08-04"), to = Some("2017-09-04"))
//
//  def misalignedPeriod(propertyType: PropertyType) =
//    period(propertyType, from = Some("2017-08-04"), to = Some("2017-09-04"))
//
//  def bothExpenses(propertyType: PropertyType) = period(propertyType, consolidatedExpenses = Some(100.50))
//
//  def bothExpensesUpdate(propertyType: PropertyType) =
//    period(propertyType, from = None, to = None, consolidatedExpenses = Some(100.50))

//  def period(propertyType: PropertyType,
//             from: Option[String] = Some("2017-04-06"),
//             to: Option[String] = Some("2018-04-05"),
//             consolidatedExpenses: Option[BigDecimal] = None,
//             costOfServices: Option[BigDecimal] = None,
//             onlyConsolidated: Boolean = false,
//             onlyResidential: Boolean = false,
//             noExpenses: Boolean = false,
//             overConsolidatedExpenses: Boolean = false): JsValue = propertyType match {
//    case PropertyType.FHL if noExpenses =>
//      Jsons.Properties.fhlPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500
//      )
//    case PropertyType.FHL if overConsolidatedExpenses =>
//      Jsons.Properties.fhlPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500,
//        consolidatedExpenses = Some(99999999999999.50)
//      )
//    case PropertyType.FHL =>
//      Jsons.Properties.fhlPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500,
//        premisesRunningCosts = Some(100.50),
//        repairsAndMaintenance = Some(100.50),
//        financialCosts = Some(100),
//        professionalFees = Some(100.50),
//        otherCost = Some(100.50),
//        costOfServices = costOfServices,
//        consolidatedExpenses = consolidatedExpenses
//      )
//    case PropertyType.OTHER if noExpenses =>
//      Jsons.Properties.consolidationPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500,
//        rentIncomeTaxDeducted = 250.55,
//        premiumsOfLeaseGrant = Some(200.22),
//        reversePremiums = 22.35,
//        otherPropertyIncome = Some(13.10)
//      )
//    case PropertyType.OTHER if onlyConsolidated =>
//      Jsons.Properties.consolidationPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500,
//        rentIncomeTaxDeducted = 250.55,
//        premiumsOfLeaseGrant = Some(200.22),
//        reversePremiums = 22.35,
//        otherPropertyIncome = Some(13.10),
//        consolidatedExpenses = Some(100.55)
//      )
//    case PropertyType.OTHER if onlyResidential =>
//      Jsons.Properties.residentialPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500,
//        rentIncomeTaxDeducted = 250.55,
//        premiumsOfLeaseGrant = Some(200.22),
//        reversePremiums = 22.35,
//        otherPropertyIncome = Some(13.10),
//        residentialFinancialCost = Some(100.55)
//      )
//    case PropertyType.OTHER if overConsolidatedExpenses =>
//      Jsons.Properties.otherPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500,
//        rentIncomeTaxDeducted = 250.55,
//        premiumsOfLeaseGrant = Some(200.22),
//        reversePremiums = 22.35,
//        otherPropertyIncome = Some(13.10),
//        consolidatedExpenses = Some(99999999999999.50)
//      )
//    case PropertyType.OTHER =>
//      Jsons.Properties.otherPeriod(
//        fromDate = from,
//        toDate = to,
//        rentIncome = 500,
//        rentIncomeTaxDeducted = 250.55,
//        premiumsOfLeaseGrant = Some(200.22),
//        reversePremiums = 22.35,
//        otherPropertyIncome = Some(13.10),
//        premisesRunningCosts = Some(100.50),
//        repairsAndMaintenance = Some(100.50),
//        financialCosts = Some(100),
//        professionalFees = Some(100.50),
//        costOfServices = costOfServices,
//        otherCost = Some(100.50),
//        residentialFinancialCost = Some(100.50),
//        consolidatedExpenses = consolidatedExpenses
//      )
//  }
//
//  def invalidPeriod(propertyType: PropertyType): JsValue = propertyType match {
//    case PropertyType.FHL =>
//      Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-01"),
//                                 toDate = Some("02-04-2017"),
//                                 rentIncome = -500,
//                                 financialCosts = Some(400.456))
//    case PropertyType.OTHER =>
//      Jsons.Properties.otherPeriod(
//        fromDate = Some("2017-04-01"),
//        toDate = Some("02-04-2017"),
//        rentIncome = -500,
//        rentIncomeTaxDeducted = 250.55,
//        premiumsOfLeaseGrant = Some(-200.22),
//        reversePremiums = 22.35
//      )
//  }

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

//  def expectedBody(propertyType: PropertyType): String = propertyType match {
//    case PropertyType.FHL =>
//      Jsons.Properties
//        .fhlPeriod(
//          fromDate = Some("2017-04-05"),
//          toDate = Some("2018-04-04"),
//          rentIncome = 200.00,
//          premisesRunningCosts = Some(200),
//          repairsAndMaintenance = Some(200),
//          financialCosts = Some(200),
//          professionalFees = Some(200),
//          costOfServices = Some(200.00),
//          otherCost = Some(200)
//        )
//        .toString()
//
//    case PropertyType.OTHER =>
//      Jsons.Properties
//        .otherPeriod(
//          fromDate = Some("2017-04-05"),
//          toDate = Some("2018-04-04"),
//          rentIncome = 200.00,
//          premiumsOfLeaseGrant = Some(200),
//          reversePremiums = 200,
//          otherPropertyIncome = Some(200),
//          premisesRunningCosts = Some(200),
//          repairsAndMaintenance = Some(200),
//          financialCosts = Some(200),
//          professionalFees = Some(200),
//          costOfServices = Some(200),
//          residentialFinancialCost = Some(200),
//          otherCost = Some(200)
//        )
//        .toString()
//  }
}
