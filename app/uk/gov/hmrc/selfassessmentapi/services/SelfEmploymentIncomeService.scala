package uk.gov.hmrc.selfassessmentapi.services

import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentIncome, SelfEmploymentIncomeId}

import scala.concurrent.Future

trait SelfEmploymentIncomeService {

  def create(selfEmploymentIncome: SelfEmploymentIncome): Future[SelfEmploymentIncomeId]

}
