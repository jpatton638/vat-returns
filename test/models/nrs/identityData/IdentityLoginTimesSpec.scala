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

package models.nrs.identityData

import java.time.{Instant, LocalDateTime, ZoneId}

import play.api.libs.json.{JsObject, Json}
import base.SpecBase

class IdentityLoginTimesSpec extends SpecBase {

  val currentDateTimeString = "2016-11-27T09:00:00.000Z"
  val previousDateTimeString = "2016-11-24T12:00:00.000Z"

  val currentDateTimeToUse: LocalDateTime = LocalDateTime.ofInstant(Instant.parse(currentDateTimeString), ZoneId.of("UTC"))
  val previousDateTimeToUse: LocalDateTime = LocalDateTime.ofInstant(Instant.parse(previousDateTimeString), ZoneId.of("UTC"))

  val correctJson: JsObject = Json.obj(
    "currentLogin" -> currentDateTimeString,
    "previousLogin" -> previousDateTimeString
  )

  val jsonWithoutPreviousLogin: JsObject = Json.obj(
    "currentLogin" -> currentDateTimeString
  )

  val correctModel: IdentityLoginTimes = IdentityLoginTimes(
    currentDateTimeToUse,
    Some(previousDateTimeToUse)
  )

  val modelWithoutPreviousLogin: IdentityLoginTimes = IdentityLoginTimes(
    currentDateTimeToUse,
    None
  )

  "Formats" should {
    "parse correctly from json" in {
      correctJson.as[IdentityLoginTimes] shouldBe correctModel
    }
    "parse correctly to json" in {
      Json.toJson(correctModel) shouldBe correctJson
    }
    "parse correctly to json if previousLogin is None" in {
      Json.toJson(modelWithoutPreviousLogin) shouldBe jsonWithoutPreviousLogin
    }
  }
}
