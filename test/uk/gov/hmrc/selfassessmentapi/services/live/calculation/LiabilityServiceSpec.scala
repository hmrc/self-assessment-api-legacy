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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes
import SourceTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain.LiabilityResult
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.services.live.TaxYearPropertiesService

import scala.concurrent.Future

class LiabilityServiceSpec extends UnitSpec with MockitoSugar {

  private val nino = NinoGenerator().nextNino()
  private val liabilityRepo = mock[LiabilityMongoRepository]
  private val selfEmploymentRepo = mock[SelfEmploymentMongoRepository]
  private val benefitRepo = mock[BenefitsMongoRepository]
  private val ukPropertyRepo = mock[UKPropertiesMongoRepository]
  private val furnishedHolidayLettingsRepo = mock[FurnishedHolidayLettingsMongoRepository]
  private val dividendsRepo = mock[DividendMongoRepository]
  private val banksRepo = mock[BanksMongoRepository]
  private val taxYearPropertiesService = mock[TaxYearPropertiesService]
  private val featureSwitch = mock[FeatureSwitch]
  private val service = new LiabilityService(selfEmploymentRepo,
                                             benefitRepo,
                                             furnishedHolidayLettingsRepo,
                                             liabilityRepo,
                                             ukPropertyRepo,
                                             banksRepo,
                                             taxYearPropertiesService,
                                             dividendsRepo,
                                             featureSwitch)

  "calculate" should {

    when(taxYearPropertiesService.findTaxYearProperties(any[Nino], any[TaxYear])).thenReturn(Future.successful(None))

    // Stub save and calculate methods to return the same item they are given.
    when(liabilityRepo.save(any[LiabilityResult])).thenAnswer(new Answer[Future[LiabilityResult]] {
      override def answer(invocation: InvocationOnMock): Future[LiabilityResult] = {
        val arg = invocation.getArguments.head.asInstanceOf[LiabilityResult]
        Future.successful(arg)
      }
    })

    "get self employment sources from repository when Self Employment source is switched on" in {
      when(featureSwitch.isEnabled(SelfEmployments)).thenReturn(true)
      when(selfEmploymentRepo.findAll(nino, taxYear)).thenReturn(Seq())

      await(service.calculate(nino, taxYear))

      verify(selfEmploymentRepo).findAll(nino, taxYear)
    }

    "not get self employment sources from repository when Self Employment source is switched off" in {
      when(featureSwitch.isEnabled(SelfEmployments)).thenReturn(false)

      await(service.calculate(nino, taxYear))

      verifyNoMoreInteractions(selfEmploymentRepo)
    }

    "get benefits sources from repository when benefits source is switched on" in {
      when(featureSwitch.isEnabled(Benefits)).thenReturn(true)
      when(benefitRepo.findAll(nino, taxYear)).thenReturn(Seq())

      await(service.calculate(nino, taxYear))

      verify(benefitRepo).findAll(nino, taxYear)
    }

    "not get benefit source from repository when benefit source is switched off" in {
      when(featureSwitch.isEnabled(Benefits)).thenReturn(false)

      await(service.calculate(nino, taxYear))

      verifyNoMoreInteractions(benefitRepo)
    }

    "get UK property sources from repository when the UK property source is switched on" in {
      when(featureSwitch.isEnabled(UKProperties)).thenReturn(true)
      when(ukPropertyRepo.findAll(nino, taxYear)).thenReturn(Seq())

      await(service.calculate(nino, taxYear))

      verify(ukPropertyRepo).findAll(nino, taxYear)
    }

    "not get UK property sources from repository when the UK property source is switched off" in {
      when(featureSwitch.isEnabled(UKProperties)).thenReturn(false)

      await(service.calculate(nino, taxYear))

      verifyNoMoreInteractions(ukPropertyRepo)
    }

    "get savings from repository when the Savings source is switched on" in {
      when(featureSwitch.isEnabled(Banks)).thenReturn(true)
      when(banksRepo.findAll(nino, taxYear)).thenReturn(Seq())

      await(service.calculate(nino, taxYear))

      verify(banksRepo).findAll(nino, taxYear)
    }

    "get savings from repository when the Savings source is switched off" in {
      when(featureSwitch.isEnabled(Banks)).thenReturn(false)

      await(service.calculate(nino, taxYear))

      verifyNoMoreInteractions(banksRepo)
    }
  }
}
