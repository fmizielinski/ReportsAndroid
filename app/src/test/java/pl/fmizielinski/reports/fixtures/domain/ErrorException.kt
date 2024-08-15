package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.error.ErrorException

fun errorException(
    uiMessage: Int = 0,
    message: String = "message",
    cause: Throwable? = null,
) = ErrorException(
    uiMessage = uiMessage,
    message = message,
    cause = cause,
)
