package org.combinators.cls.git

import play.api.routing.Router.{Routes => RRoutes}
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/** Augments [[InhabitationController]] with routing table entries for its actions. */
trait RoutingEntries extends SimpleRouter { self: InhabitationController =>

  /** An optional prefix relative to which the routing entries will be considered. */
  val routingPrefix: Option[String] = None

  /** The address of the index page for this [[InhabitationController]] and
    * the prefix of all sub-pages related to it.
    */
  val controllerAddress: String

  override def routes: RRoutes = {
    val directRoutes: RRoutes = {
      case GET(p"/$prefix") if prefix == controllerAddress => overview()
      case GET(p"/$prefix/raw_${long(number)}") if prefix == controllerAddress => raw(number)
      case GET(p"/$prefix/prepare" ? q"number=${long(number)}") if prefix == controllerAddress => prepare(number)
      case GET(p"/$prefix/$repository.git/${file}*")
        if prefix == controllerAddress && repository == controllerAddress =>
        serveFile(file)
    }
    routingPrefix match {
      case None => directRoutes
      case Some(prefix) =>
        val prefixWithSlash = if (prefix.startsWith("/")) prefix else s"/$prefix"
        new SimpleRouter { def routes: RRoutes = directRoutes }.withPrefix(prefixWithSlash).routes
    }
  }
}