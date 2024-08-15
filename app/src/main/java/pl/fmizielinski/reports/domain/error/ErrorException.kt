package pl.fmizielinski.reports.domain.error

import androidx.annotation.StringRes
import pl.fmizielinski.reports.domain.model.SnackBarData

open class ErrorException(
    @StringRes val uiMessage: Int,
    override val message: String,
    override val cause: Throwable? = null,
) : Exception()

fun ErrorException.toSnackBarData() = SnackBarData(uiMessage)
