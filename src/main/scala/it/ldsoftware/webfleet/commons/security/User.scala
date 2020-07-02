package it.ldsoftware.webfleet.commons.security

case class User(name: String, permissions: Set[String] = Set(), jwt: Option[String] = None)
