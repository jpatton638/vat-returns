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

package controllers

import base.SpecBase
import controllers.actions.AuthActionImpl
import mocks.auth.MockMicroserviceAuthorisedFunctions
import mocks.services.MockVatReturnsService
import models._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}

import scala.concurrent.Future

class VatReturnsControllerSpec extends SpecBase with MockVatReturnsService with MockMicroserviceAuthorisedFunctions {

  val success: VatReturnDetail =
    VatReturnDetail(
      "17AA",
      1.23,
      1.23,
      1.23,
      1.23,
      1.23,
      1.23,
      1.23,
      1.23,
      1.23
    )

  val singleError = Error(code = "CODE", reason = "ERROR MESSAGE")
  val multiError = MultiError(
    failures = Seq(
      Error(code = "ERROR CODE 1", reason = "ERROR MESSAGE 1"),
      Error(code = "ERROR CODE 2" +
        "", reason = "ERROR MESSAGE 2")
    )
  )

  val transformedJson: JsValue = Json.parse(
    """{
      |   "periodKey" : "17AA",
      |   "vatDueSales" : 1.23,
      |   "vatDueAcquisitions" : 1.23,
      |   "totalVatDue" : 1.23,
      |   "vatReclaimedCurrPeriod" : 1.23,
      |   "netVatDue" : 1.23,
      |   "totalValueSalesExVAT" : 1.23,
      |   "totalValuePurchasesExVAT" : 1.23,
      |   "totalValueGoodsSuppliedExVAT" : 1.23,
      |   "totalAcquisitionsExVAT" : 1.23
      |}""".stripMargin)

  val successResponse: Either[Nothing, VatReturnDetail] = Right(success)
  val badRequestSingleError: Either[ErrorResponse, Nothing] = Left(ErrorResponse(Status.BAD_REQUEST, singleError))
  val badRequestMultiError: Either[ErrorResponse, Nothing] = Left(ErrorResponse(Status.BAD_REQUEST, multiError))
  val testVrn: String = "555555555"
  val badVrn: String = "55"
  val authActionImpl = new AuthActionImpl(mockAuth, controllerComponents)

  "The GET VatReturnsController.getVatReturns method" when {

    "called by an authenticated user" which {

      object TestVatReturnsController extends VatReturnsController(authActionImpl, mockVatReturnsService, controllerComponents)

      "is requesting VAT details" should {

        "for a successful response from the VatReturnsService" should {

          lazy val result: Result = await(TestVatReturnsController.getVatReturns(testVrn, VatReturnFilters(
            periodKey = "17AA"
          ))(fakeRequest))

          "return a status of 200 (OK)" in {
            setupMockGetVatReturns(testVrn, VatReturnFilters(
              periodKey = "17AA"
            ))(successResponse)
            status(Future.successful(result)) shouldBe Status.OK
          }

          "return a json body with the transformed des return data" in {
            contentAsJson(Future.successful(result)) shouldBe transformedJson
          }
        }

        "for a bad request with single error from the VatReturnsService" should {

          lazy val result: Result = await(TestVatReturnsController.getVatReturns(testVrn, VatReturnFilters(
            periodKey = "17AA"
          ))(fakeRequest))

          "return a status of 400 (BAD_REQUEST)" in {
            setupMockGetVatReturns(testVrn, VatReturnFilters(
              periodKey = "17AA"
            ))(badRequestSingleError)

            status(Future.successful(result)) shouldBe Status.BAD_REQUEST
          }

          "return a json body with the single error message" in {

            contentAsJson(Future.successful(result)) shouldBe Json.toJson(singleError)
          }
        }

        "for an invalid vrn " should {

          lazy val result: Result = await(TestVatReturnsController.getVatReturns(badVrn, VatReturnFilters(
            periodKey = "17AA"
          ))(fakeRequest))

          "return a status of 400 (BAD_REQUEST)" in {
            status(Future.successful(result)) shouldBe Status.BAD_REQUEST
          }

          "return a json body with the invalid vrn error message" in {
            contentAsJson(Future.successful(result)) shouldBe Json.toJson(InvalidVrn)
          }
        }

        "for a bad request with multiple errors from the VatReturnsService" should {

          lazy val result: Result = await(TestVatReturnsController.getVatReturns(testVrn, VatReturnFilters(
            periodKey = "17AA"
          ))(fakeRequest))

          "return a status of 400 (BAD_REQUEST)" in {
            setupMockGetVatReturns(testVrn, VatReturnFilters(
              periodKey = "17AA"
            ))(badRequestMultiError)

            status(Future.successful(result)) shouldBe Status.BAD_REQUEST
          }

          "return a json body with the multiple error messages" in {
            contentAsJson(Future.successful(result)) shouldBe Json.toJson(multiError)
          }
        }

      }

      "for a bad request with single error from the VatReturnsService" should {

        lazy val result: Result = await(TestVatReturnsController.getVatReturns(testVrn, VatReturnFilters(
          periodKey = "17AA"
        ))(fakeRequest))

        "return a status of 400 (BAD_REQUEST)" in {
          setupMockGetVatReturns(testVrn, VatReturnFilters(
            periodKey = "17AA"
          ))(badRequestSingleError)

          status(Future.successful(result)) shouldBe Status.BAD_REQUEST
        }

        "return a json body with the single error message" in {

          contentAsJson(Future.successful(result)) shouldBe Json.toJson(singleError)
        }
      }

      "for a bad request with multiple errors from the VatReturnsService" should {

        lazy val result: Result = await(TestVatReturnsController.getVatReturns(testVrn, VatReturnFilters(
          periodKey = "17AA"
        ))(fakeRequest))

        "return a status of 400 (BAD_REQUEST)" in {
          setupMockGetVatReturns(testVrn, VatReturnFilters(
            periodKey = "17AA"
          ))(badRequestMultiError)

          status(Future.successful(result)) shouldBe Status.BAD_REQUEST
        }

        "return a json body with the multiple error messages" in {

          contentAsJson(Future.successful(result)) shouldBe Json.toJson(multiError)
        }
      }
    }

    "called by an unauthenticated user" should {

      object TestVatReturnsController extends VatReturnsController(authActionImpl, mockVatReturnsService, controllerComponents)

      "Return an UNAUTHORISED response" which {

        lazy val result: Result = await(TestVatReturnsController.getVatReturns(testVrn, VatReturnFilters(
          periodKey = "17AA"
        ))(fakeRequest))

        "has status UNAUTHORISED (401)" in {
          setupMockAuthorisationException()
          status(Future.successful(result)) shouldBe Status.UNAUTHORIZED
        }
      }
    }
  }
}