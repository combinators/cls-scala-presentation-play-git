package org.combinators.cls.git




import akka.routing.Router
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient




class URLCleanupTest extends PlaySpec with GuiceOneServerPerSuite {

  "Calling a URL with a leading slash after the hostname" must {
    "redirect to the same URL without the slash" in {
      val client = app.injector.instanceOf[WSClient]
      val address = s"http://localhost:$port"
      val urlWithSlash = s"$address//test/foo/"
      val response = await(client.url(urlWithSlash).get())
      val urlWithoutSlash = s"$address/test/foo/"
      response.status mustBe PERMANENT_REDIRECT
      response.header(LOCATION) mustBe urlWithoutSlash
    }
  }
}
