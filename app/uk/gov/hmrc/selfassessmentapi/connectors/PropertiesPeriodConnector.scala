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

package uk.gov.hmrc.selfassessmentapi.connectors

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties._
import uk.gov.hmrc.selfassessmentapi.models.{Financials, Period, PeriodId, des}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesPeriodResponse

import scala.concurrent.Future

trait PropertiesPeriodConnector[P <: Period, F <: Financials] {
  def create(nino: Nino, properties: P)(implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse]
  def update(nino: Nino, propertyType: PropertyType, periodId: PeriodId, financials: F)(
      implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse]
}

object PropertiesPeriodConnector {
  def apply[P <: Period, F <: Financials](
      implicit p: PropertiesPeriodConnector[P, F]): PropertiesPeriodConnector[P, F] = implicitly

  private lazy val baseUrl: String = AppContext.desUrl

  implicit object OtherPropertiesPeriodConnector
      extends PropertiesPeriodConnector[Other.Properties, Other.Financials] {
    override def create(nino: Nino, properties: Other.Properties)(
        implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
      httpPost[des.properties.Other.Properties, PropertiesPeriodResponse](
        baseUrl + s"/income-store/nino/$nino/uk-properties/other/periodic-summaries",
        des.properties.Other.Properties.from(properties),
        PropertiesPeriodResponse)

    override def update(nino: Nino, propertyType: PropertyType, periodId: PeriodId, financials: Other.Financials)(
        implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
      httpPut[Option[des.properties.Other.Financials], PropertiesPeriodResponse](
        baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries/$periodId",
        des.properties.Other.Financials.from(Some(financials)),
        PropertiesPeriodResponse)
  }

  implicit object FHLPropertiesPeriodConnector extends PropertiesPeriodConnector[FHL.Properties, FHL.Financials] {
    override def create(nino: Nino, properties: FHL.Properties)(
        implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
      httpPost[des.properties.FHL.Properties, PropertiesPeriodResponse](
        baseUrl + s"/income-store/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries",
        des.properties.FHL.Properties.from(properties),
        PropertiesPeriodResponse)

    override def update(nino: Nino, propertyType: PropertyType, periodId: PeriodId, financials: FHL.Financials)(
        implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
      httpPut[Option[des.properties.FHL.Financials], PropertiesPeriodResponse](
        baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries/$periodId",
        des.properties.FHL.Financials.from(Some(financials)),
        PropertiesPeriodResponse)
  }

  def retrieve(nino: Nino, from: LocalDate, to: LocalDate, propertyType: PropertyType)(
      implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
    httpGet[PropertiesPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to",
      PropertiesPeriodResponse)

  def retrieveAll(nino: Nino, propertyType: PropertyType)(
      implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
    httpGet[PropertiesPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/periodicsummaries",
      PropertiesPeriodResponse)
}
