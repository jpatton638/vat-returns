/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import config.MicroserviceAppConfig
import connectors.httpParsers.NrsResponseParsers._
import models.Error
import models.nrs.{AppJson, NrsReceiptRequestModel}
import play.api.http.Status.BAD_GATEWAY
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NrsConnector @Inject()(http: HttpClient, appConfig: MicroserviceAppConfig) {

  private[connectors] def urlToUse(vrn: String): String =
    appConfig.nrsSubmissionEndpoint + (if (appConfig.features.useStubFeature()) s"/$vrn" else "")

  def nrsReceiptSubmission(data: NrsReceiptRequestModel)
                          (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[SubmissionResult] =
    http.POST[NrsReceiptRequestModel, SubmissionResult](
      urlToUse(data.metadata.searchKeys.vrn),
      data,
      Seq(
        "Content-Type" -> AppJson.toString,
        "X-API-Key" -> appConfig.nrsApiKey
      )
    ).recover {
      case ex: HttpException =>
        logger.warn(s"[NrsConnector][nrsReceiptSubmission] - HTTP exception received: ${ex.message}")
        Left(Error(BAD_GATEWAY.toString, ex.message))
    }
}
