package pl.fmizielinski.reports.utils

import kotlinx.coroutines.flow.flow

inline fun <reified T> exceptionFlow(exception: Throwable) = flow<T> {
    throw exception
}
