package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.error.CompositeErrorException
import pl.fmizielinski.reports.domain.error.SimpleErrorException

fun simpleErrorException(
    uiMessage: Int = 0,
    code: String = "code",
    message: String = "message",
    cause: Throwable? = null,
    isVerificationError: Boolean = false,
) = SimpleErrorException(
    uiMessage = uiMessage,
    code = code,
    message = message,
    cause = cause,
    isVerificationError = isVerificationError,
)

fun compositeErrorException(
    exceptions: List<SimpleErrorException> = emptyList(),
) = CompositeErrorException(
    exceptions = exceptions,
)
