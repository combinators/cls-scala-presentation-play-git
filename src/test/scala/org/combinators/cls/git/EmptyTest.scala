package org.combinators.cls.git

import javax.inject.Inject

import org.combinators.cls.interpreter.CombinatorInfo
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.webjars.play.WebJarsUtil
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import play.api.test.Helpers._


class EmptyTest extends PlaySpec with GuiceOneServerPerSuite {
  "Calling the emptytest overview" must {
    "result in a valid response" in {
      val client = app.injector.instanceOf[WSClient]
      val request = s"/emptytest"
      val url = s"http://localhost:$port$request"
      val response = await(client.url(url).get())
      response.status mustBe OK
      response.body.toLowerCase.indexOf("repository") must be > 0
    }
  }
}

class EmptyTestController @Inject()(webJars: WebJarsUtil, applicationLifecycle: ApplicationLifecycle)
  extends InhabitationController(webJars, applicationLifecycle) with RoutingEntries {
  val controllerAddress: String = "emptytest"
  val combinatorComponents: Map[String, CombinatorInfo] = Map.empty
  val results: Results = EmptyResults()
}
