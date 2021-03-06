package it.ldsoftware.webfleet.commons.service.model

sealed trait ServiceFailure

case class NotFound(searched: String) extends ServiceFailure
case class Invalid(errors: List[ValidationError]) extends ServiceFailure
case object ForbiddenError extends ServiceFailure
case class UnexpectedError(th: Throwable, message: String) extends ServiceFailure
case class ServiceUnavailable(status: ApplicationHealth) extends ServiceFailure
