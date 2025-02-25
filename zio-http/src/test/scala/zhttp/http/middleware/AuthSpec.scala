package zhttp.http.middleware

import zhttp.http._
import zhttp.internal.HttpAppTestExtensions
import zio.UIO
import zio.test.Assertion._
import zio.test._

object AuthSpec extends DefaultRunnableSpec with HttpAppTestExtensions {
  private val successBasicHeader: Headers  = Headers.basicAuthorizationHeader("user", "resu")
  private val failureBasicHeader: Headers  = Headers.basicAuthorizationHeader("user", "user")
  private val bearerToken: String          = "dummyBearerToken"
  private val successBearerHeader: Headers = Headers.bearerAuthorizationHeader(bearerToken)
  private val failureBearerHeader: Headers = Headers.bearerAuthorizationHeader(bearerToken + "SomethingElse")

  private val basicAuthM: HttpMiddleware[Any, Nothing]     = Middleware.basicAuth { c =>
    c.uname.reverse == c.upassword
  }
  private val basicAuthZIOM: HttpMiddleware[Any, Nothing]  = Middleware.basicAuthZIO { c =>
    UIO(c.uname.reverse == c.upassword)
  }
  private val bearerAuthM: HttpMiddleware[Any, Nothing]    = Middleware.bearerAuth { c =>
    c == bearerToken
  }
  private val bearerAuthZIOM: HttpMiddleware[Any, Nothing] = Middleware.bearerAuthZIO { c =>
    UIO(c == bearerToken)
  }

  def spec = suite("AuthSpec") {
    suite("basicAuth") {
      testM("HttpApp is accepted if the basic authentication succeeds") {
        val app = (Http.ok @@ basicAuthM).status
        assertM(app(Request().addHeaders(successBasicHeader)))(equalTo(Status.Ok))
      } +
        testM("Uses forbidden app if the basic authentication fails") {
          val app = (Http.ok @@ basicAuthM).status
          assertM(app(Request().addHeaders(failureBasicHeader)))(equalTo(Status.Unauthorized))
        } +
        testM("Responses should have WWW-Authentication header if Basic Auth failed") {
          val app = Http.ok @@ basicAuthM header "WWW-AUTHENTICATE"
          assertM(app(Request().addHeaders(failureBasicHeader)))(isSome)
        }
    } +
      suite("basicAuthZIO") {
        testM("HttpApp is accepted if the basic authentication succeeds") {
          val app = (Http.ok @@ basicAuthZIOM).status
          assertM(app(Request().addHeaders(successBasicHeader)))(equalTo(Status.Ok))
        } +
          testM("Uses forbidden app if the basic authentication fails") {
            val app = (Http.ok @@ basicAuthZIOM).status
            assertM(app(Request().addHeaders(failureBasicHeader)))(equalTo(Status.Unauthorized))
          } +
          testM("Responses should have WWW-Authentication header if Basic Auth failed") {
            val app = Http.ok @@ basicAuthZIOM header "WWW-AUTHENTICATE"
            assertM(app(Request().addHeaders(failureBasicHeader)))(isSome)
          }
      } +
      suite("bearerAuth") {
        testM("HttpApp is accepted if the bearer authentication succeeds") {
          val app = (Http.ok @@ bearerAuthM).status
          assertM(app(Request().addHeaders(successBearerHeader)))(equalTo(Status.Ok))
        } +
          testM("Uses forbidden app if the bearer authentication fails") {
            val app = (Http.ok @@ bearerAuthM).status
            assertM(app(Request().addHeaders(failureBearerHeader)))(equalTo(Status.Unauthorized))
          } +
          testM("Responses should have WWW-Authentication header if bearer Auth failed") {
            val app = Http.ok @@ bearerAuthM header "WWW-AUTHENTICATE"
            assertM(app(Request().addHeaders(failureBearerHeader)))(isSome)
          }
      } +
      suite("bearerAuthZIO") {
        testM("HttpApp is accepted if the bearer authentication succeeds") {
          val app = (Http.ok @@ bearerAuthZIOM).status
          assertM(app(Request().addHeaders(successBearerHeader)))(equalTo(Status.Ok))
        } +
          testM("Uses forbidden app if the bearer authentication fails") {
            val app = (Http.ok @@ bearerAuthZIOM).status
            assertM(app(Request().addHeaders(failureBearerHeader)))(equalTo(Status.Unauthorized))
          } +
          testM("Responses should have WWW-Authentication header if bearer Auth failed") {
            val app = Http.ok @@ bearerAuthZIOM header "WWW-AUTHENTICATE"
            assertM(app(Request().addHeaders(failureBearerHeader)))(isSome)
          }
      }
  }
}
