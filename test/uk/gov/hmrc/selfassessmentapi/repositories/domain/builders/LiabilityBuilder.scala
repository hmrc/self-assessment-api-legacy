package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.TestUtils._
import uk.gov.hmrc.selfassessmentapi.controllers.api.AllowancesAndReliefs
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Liability, TaxDeducted, TaxesCalculated}

case class LiabilityBuilder() {
  private var liability: Liability = Liability(BSONObjectID.generate, BSONObjectID.generate.stringify,
    generateSaUtr(), taxYear, Nil, Nil, Nil, Nil, Nil, Nil, 0, 0,
    AllowancesAndReliefs(), TaxDeducted(), Nil, Nil, Nil, Nil, TaxesCalculated(0, 0, 0, 0, 0, 0))

  def withTaxDeducted(taxDeducted: TaxDeducted): LiabilityBuilder = {
    liability = liability.copy(taxDeducted = taxDeducted)
    this
  }

  def create(): Liability = liability
}
