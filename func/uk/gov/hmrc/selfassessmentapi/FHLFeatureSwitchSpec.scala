package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

class FHLFeatureSwitchUKOnSpec extends BaseFunctionalSpec {

  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = config)

  private val config = Map("Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true)

  "The FHL feature switch" should {
    "include UK FHLs when the switch is enabled" in {
      val payload = Json.parse(
        """
          |{
          |  "propertyLocation": "UK",
          |  "allowances": {
          |    "capitalGainsAllowance": 1000.00
          |  },
          |  "adjustments": {
          |    "lossBroughtForward": 500.00
          |  }
          |}
        """.stripMargin)

        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
        .thenAssertThat()
          .statusIs(201)

      when()
        .post(s"/sandbox/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
      .thenAssertThat()
        .statusIs(201)
    }
  }
}

class FHLFeatureSwitchUKOffSpec extends BaseFunctionalSpec {

  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = config)

  private val config = Map("Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> false)

  "The FHL feature switch" should {
    "exclude UK FHLs when the switch is disabled" in {
      val payload = Json.parse(
        """
          |{
          |  "propertyLocation": "UK",
          |  "allowances": {
          |    "capitalGainsAllowance": 1000.00
          |  },
          |  "adjustments": {
          |    "lossBroughtForward": 500.00
          |  }
          |}
        """.stripMargin)

        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
        .thenAssertThat()
          .statusIs(400)

        .when()
          .post(s"/sandbox/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
        .thenAssertThat()
          .statusIs(400)
    }
  }
}

class FHLFeatureSwitchEEAOnSpec extends BaseFunctionalSpec {

  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = config)

  private val config = Map("Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true)

  "The FHL feature switch" should {
    "exclude UK FHLs when the switch is disabled" in {
      val payload = Json.parse(
        """
          |{
          |  "propertyLocation": "EEA",
          |  "allowances": {
          |    "capitalGainsAllowance": 1000.00
          |  },
          |  "adjustments": {
          |    "lossBroughtForward": 500.00
          |  }
          |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
      .thenAssertThat()
        .statusIs(201)

      .when()
        .post(s"/sandbox/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
      .thenAssertThat()
        .statusIs(201)
    }
  }
}

class FHLFeatureSwitchEEAOffSpec extends BaseFunctionalSpec {

  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = config)

  private val config = Map("Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> false)

  "The FHL feature switch" should {
    "exclude UK FHLs when the switch is disabled" in {
      val payload = Json.parse(
        """
          |{
          |  "propertyLocation": "EEA",
          |  "allowances": {
          |    "capitalGainsAllowance": 1000.00
          |  },
          |  "adjustments": {
          |    "lossBroughtForward": 500.00
          |  }
          |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
      .thenAssertThat()
        .statusIs(400)

      when()
        .post(s"/sandbox/$saUtr/$taxYear/furnished-holiday-lettings", Some(payload))
      .thenAssertThat()
        .statusIs(400)
    }
  }
}
