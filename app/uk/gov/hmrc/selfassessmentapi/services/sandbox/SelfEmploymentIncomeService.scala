package uk.gov.hmrc.selfassessmentapi.services.sandbox
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentIncome, SelfEmploymentIncomeId}

import scala.concurrent.Future

object SelfEmploymentIncomeService extends uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentIncomeService {
  override def create(selfEmploymentIncome: SelfEmploymentIncome): Future[SelfEmploymentIncomeId] = Future.successful(BSONObjectID.generate.stringify)
}
