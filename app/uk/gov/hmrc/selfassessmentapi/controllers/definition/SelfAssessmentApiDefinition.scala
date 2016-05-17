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

package uk.gov.hmrc.selfassessmentapi.controllers.definition

import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.definition.APIStatus.APIStatus

class SelfAssessmentApiDefinition(apiContext: String, apiStatus: APIStatus) {

  private val readScope = "read:self-assessment"
  private val writeScope = "write:self-assessment"

  val definition: Definition =
    Definition(
      scopes = Seq(
        Scope(
          key = readScope,
          name = "Self-Assessment - Read",
          description = "Allow read access to self assessment data"
        ),
        Scope(
          key = writeScope,
          name = "Self-Assessment - Write",
          description = "Allow write access to self assessment data"
        )
      ),
      api = APIDefinition(
        name = "Self Assessment",
        description = "An API for providing self assessment data and obtaining liability estimations",
        context = apiContext,
        versions = Seq(
          APIVersion(
            version = "1.0",
            status = apiStatus,
            endpoints = Seq(
              Endpoint(
                uriPattern = "/",
                endpointName = "Resolve Customer",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}",
                endpointName = "Discover Tax Years",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}",
                endpointName = "Discover Tax Year",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}",
                endpointName = "Retrieve Source",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}",
                endpointName = "Retrieve Sources",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}",
                endpointName = "Create Source",
                method = HttpMethod.POST,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}",
                endpointName = "Update Source",
                method = HttpMethod.PUT,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}",
                endpointName = "Delete Source",
                method = HttpMethod.DELETE,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}/{transaction}",
                endpointName = "Create Transaction",
                method = HttpMethod.POST,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}/{transaction}/{transactionId}",
                endpointName = "Update Transaction",
                method = HttpMethod.PUT,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}/{transaction}/{transactionId}",
                endpointName = "Delete Transaction",
                method = HttpMethod.DELETE,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}/{transaction}/{transactionId}",
                endpointName = "Retrieve Transaction",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/{source}/{sourceId}/{transaction}",
                endpointName = "Retrieve Transactions",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/liabilities",
                endpointName = "Request Liability",
                method = HttpMethod.POST,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/liabilities/{liabilityId}",
                endpointName = "Retrieve Liability",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/liabilities",
                endpointName = "Retrieve Liabilities",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/{taxYear}/liabilities/{liabilityId}",
                endpointName = "Delete Liability",
                method = HttpMethod.DELETE,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              )
            )
          )
        ),
        requiresTrust = None
      )
    )
}

object PublishedSelfAssessmentApiDefinition extends SelfAssessmentApiDefinition(AppContext.apiGatewayContext, APIStatus.PUBLISHED)

object PrototypedSelfAssessmentApiDefinition extends SelfAssessmentApiDefinition(AppContext.apiGatewayContext, APIStatus.PROTOTYPED)
