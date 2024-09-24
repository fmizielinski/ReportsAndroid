package pl.fmizielinski.reports.utils

import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.isA

inline fun <reified T : Throwable> expectThrowable(
    subject: Throwable,
    noinline block: Builder<T>.() -> Unit,
) {
    expectThat(subject).isA<T>()
        .and(block)
}
