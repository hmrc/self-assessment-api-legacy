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
import scala.math.BigDecimal.RoundingMode

trait Math {

  protected def sum(values: Option[BigDecimal]*): BigDecimal = values.flatten.sum

  protected def valueOrZero(maybeValue: Option[BigDecimal]): BigDecimal = maybeValue.getOrElse(0)

  protected def positiveOrZero(n: BigDecimal): BigDecimal = n match {
    case x if x > 0 => x
    case _ => 0
  }

  protected def capAt(n: Option[BigDecimal], cap: BigDecimal): Option[BigDecimal] = n map {
    case x if x > cap => cap
    case x => x
  }

  protected def roundDown(n: BigDecimal): BigDecimal = n.setScale(0, RoundingMode.DOWN)

  protected def roundDownToNearest(n: BigDecimal, v: Int): BigDecimal = roundDown(n / v) * v

  protected def roundUp(n: BigDecimal): BigDecimal = n.setScale(0, RoundingMode.UP)
}
