package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.error.NetworkError

fun networkError(
    code: String = "code",
    message: String = "message",
) = NetworkError(
    code = code,
    message = message,
)
