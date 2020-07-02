package it.ldsoftware.webfleet.commons.http

trait RestMapper[T, R] {
  def map(t: T): R
}
