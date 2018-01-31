package org.combinators.cls.git

import play.api.routing.Router.{Routes => RRoutes}
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/** Augments [[InhabitationController]] with routing table entries for its actions. */
trait RoutingEntries extends SimpleRouter { self: InhabitationController =>

  /** The address of the index page for this [[InhabitationController]] and
    * the prefix of all sub-pages related to it.
    */
  val routingPrefix: String

  override def routes: RRoutes = {
    case GET(p"/$prefix") if prefix == routingPrefix => overview()
    case GET(p"/$prefix/raw_${long(number)}") if prefix == routingPrefix => raw(number)
    case GET(p"/$prefix/prepare" ? q"number=${long(number)}") if prefix == routingPrefix => prepare(number)
    case GET(p"/$prefix/$repository.git/${file}*")
      if prefix == routingPrefix && repository == routingPrefix =>
      serveFile(file)
  }
}