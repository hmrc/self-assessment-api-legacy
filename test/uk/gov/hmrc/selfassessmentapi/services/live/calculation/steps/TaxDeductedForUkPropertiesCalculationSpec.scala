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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import org.scalatest.prop.TableDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.TaxPaid
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.UkPropertySugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.TaxPaidForUkProperty
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoTaxDeducted

class TaxDeductedForUkPropertiesCalculationSpec extends UnitSpec with TableDrivenPropertyChecks {

  "run" should {

    "calculate tax deducted amount for UK properties with amounts that do not require rounding" in {
      val liability = aLiability()

      TaxDeductedForUkPropertiesCalculation
        .run(aSelfAssessment(
                 ukProperties = Seq(aUkProperty(id = "property-1").copy(taxesPaid = Seq(aTaxPaidSummary("property-1", 500))))),
             liability)
        .getLiabilityOrFail shouldBe
        liability.copy(
            taxDeducted = Some(MongoTaxDeducted(deductionFromUkProperties = Seq(TaxPaidForUkProperty("property-1", 500)),
                                                totalDeductionFromUkProperties = 500)))
    }

    "calculate tax deducted amount for UK properties with amounts that require rounding" in {
      val liability = aLiability()

      TaxDeductedForUkPropertiesCalculation
        .run(
            aSelfAssessment(ukProperties = Seq(aUkProperty(id = "property-1").copy(taxesPaid = Seq(aTaxPaidSummary("property-1", 500.223))))),
            liability)
        .getLiabilityOrFail shouldBe
        liability.copy(
            taxDeducted = Some(MongoTaxDeducted(deductionFromUkProperties = Seq(TaxPaidForUkProperty("property-1", 500.223)),
                                                totalDeductionFromUkProperties = 500.23)))
    }
  }
}
