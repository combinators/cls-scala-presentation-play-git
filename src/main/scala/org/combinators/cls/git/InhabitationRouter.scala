package org.combinators.cls.git

import javax.inject.Inject

import play.api.mvc._
import play.api.routing.Router.{Routes => RRoutes}
import play.api.routing.SimpleRouter
import play.api.routing.sird._

abstract class InhabitationRouter(productName: String, controller: InhabitationController) extends SimpleRouter {
  override def routes: RRoutes = {
    case GET(p"/$prefix") if prefix == productName => controller.overview()
    case GET(p"/$prefix/raw_${long(number)}") if prefix == productName => controller.raw(number)
    case GET(p"/$prefix/prepare" ? q"number=${long(number)}") if prefix == productName => controller.prepare(number)
    case GET(p"/$prefix/$repository.git/${file}*") if prefix == productName && repository == productName =>
      controller.serveFile(file)
  }
}
