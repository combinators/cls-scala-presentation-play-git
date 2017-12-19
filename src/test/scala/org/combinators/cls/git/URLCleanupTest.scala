package org.combinators.cls.git

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class URLCleanupTest extends PlaySpec with GuiceOneServerPerSuite {
  "Calling a URL with a trailing slash after the hostname" must {
    "redirect to the same URL without the slash" in {
      val client = app.injector.instanceOf[WSClient]
      val requestWithoutSlash = s"/test/foo"
      val urlWithSlash = s"http://localhost:$port$requestWithoutSlash/"
      val response = await(client.url(urlWithSlash).withFollowRedirects(false).get())
      response.status mustBe MOVED_PERMANENTLY
      response.header(LOCATION) mustBe Some(requestWithoutSlash)
    }
  }
}
