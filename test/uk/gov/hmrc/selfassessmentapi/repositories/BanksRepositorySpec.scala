package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.Bank

class BanksRepositorySpec extends MongoEmbeddedDatabase {
  private val repo = new BanksRepository
  private val nino = generateNino

  private def createBank(nino: Nino, lastModifiedDateTime: DateTime, id: BSONObjectID = BSONObjectID.generate): Bank = {
    Bank(id, id.stringify, nino, lastModifiedDateTime, "myBank", foreign = false)
  }

  "create" should {
    "persist a bank" in {
      val id = BSONObjectID.generate
      val bank = createBank(nino, DateTime.now(DateTimeZone.UTC), id)

      await(repo.create(bank)) shouldBe true
      await(repo.retrieve(id.stringify, nino)) shouldBe Some(bank)
    }
  }

  "update" should {
    "fail if a bank with the provided ID does not exist" in {
      val id = BSONObjectID.generate
      await(repo.update(id.stringify, nino, createBank(nino, DateTime.now(DateTimeZone.UTC), id))) shouldBe false
    }

    "overwrite existing fields in bank with new data provided by the user" in {
      val id = BSONObjectID.generate
      val bank = createBank(nino, DateTime.now(DateTimeZone.UTC), id)

      await(repo.create(bank)) shouldBe true
      await(repo.retrieve(id.stringify, nino)) shouldBe Some(bank)

      val newBank = bank.copy(accountName = "superBank")
      await(repo.update(id.stringify, nino, newBank)) shouldBe true

      val result = await(repo.retrieve(id.stringify, nino)).get
      result.accountName shouldBe newBank.accountName
      result.foreign shouldBe newBank.foreign
    }
  }

  "retrieve" should {
    "return None if a bank with the provided ID does not exist" in {
      await(repo.retrieve(BSONObjectID.generate.stringify, nino)) shouldBe None
    }
  }

  "retrieveAll" should {
    "return all bank sources associated with a nino" in {
      val id1 = BSONObjectID.generate
      val id2 = BSONObjectID.generate
      val bank1 = createBank(nino, DateTime.now(DateTimeZone.UTC), id1)
      val bank2 = createBank(nino, DateTime.now(DateTimeZone.UTC), id2)

      await(repo.create(bank1)) shouldBe true
      await(repo.create(bank2)) shouldBe true
      await(repo.retrieveAll(nino)) should contain theSameElementsAs Seq(bank1, bank2)
    }

    "return an empty sequence for a nino containing no bank sources" in {
      await(repo.retrieveAll(nino)) shouldBe Seq.empty
    }
  }

  "deleteAllBeforeDate" should {
    "delete all records older than the provided DateTime object" in {
      val bankToKeep = createBank(nino, DateTime.now(DateTimeZone.UTC).plusDays(1))
      val bankToRemoveOne = createBank(nino, DateTime.now(DateTimeZone.UTC).minusDays(1))
      val bankToRemoveTwo = createBank(nino, DateTime.now(DateTimeZone.UTC))

      await(repo.create(bankToKeep))
      await(repo.create(bankToRemoveOne))
      await(repo.create(bankToRemoveTwo))
      await(repo.deleteAllBeforeDate(DateTime.now(DateTimeZone.UTC))) shouldBe 2
      await(repo.retrieveAll(nino)) should contain theSameElementsAs Seq(bankToKeep)
    }
  }
}
