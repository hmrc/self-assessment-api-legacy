package uk.gov.hmrc.selfassessmentapi.services.live
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentIncome, SelfEmploymentIncomeId}

import scala.concurrent.Future

object SelfEmploymentIncomeService extends uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentIncomeService {
  override def create(selfEmploymentIncome: SelfEmploymentIncome): Future[SelfEmploymentIncomeId] = ???
}
