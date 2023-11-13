package com.github.simulatan.todo.util

import jakarta.transaction.RollbackException
import jakarta.validation.ConstraintViolationException
import org.jboss.resteasy.spi.BadRequestException

fun <T> mapValidatorException(block: () -> T): T {
	fun exception(e: ConstraintViolationException)
		= BadRequestException("Constraint violations found! ${e.constraintViolations}")

	return try {
		block()
	} catch (e: ConstraintViolationException) {
		throw exception(e)
	} catch (e: RollbackException) {
		val cause = e.cause
		if (cause !is ConstraintViolationException) {
			throw e
		}
		throw exception(cause)
	}
}
