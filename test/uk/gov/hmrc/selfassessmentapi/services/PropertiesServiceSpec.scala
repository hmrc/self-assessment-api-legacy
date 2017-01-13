package uk.gov.hmrc.selfassessmentapi.services

import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.Properties
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error

class PropertiesServiceSpec extends MongoEmbeddedDatabase {

  private val service = new PropertiesService(new PropertiesRepository)
  private val nino = generateNino

  "create" should {
    "return an error when a customer attempts to create more than one property business" in {
      val properties = Properties(AccountingType.CASH)

      await(service.create(nino, properties)) shouldBe Right(true)
      await(service.create(nino, properties)) shouldBe Left(Error("ALREADY_EXISTS", "A property business already exists", ""))
    }
  }
}
