package pl.fmizielinski.reports.domain.error

import androidx.annotation.StringRes
import pl.fmizielinski.reports.domain.common.model.SnackBarData

abstract class ErrorException : Exception()

class SimpleErrorException(
    @StringRes val uiMessage: Int,
    val code: String = "",
    override val message: String,
    override val cause: Throwable? = null,
    val isVerificationError: Boolean = false,
) : ErrorException()

class CompositeErrorException(
    val exceptions: List<SimpleErrorException>,
) : ErrorException()

class UnauthorizedErrorException(
    override val cause: Throwable? = null,
) : ErrorException()

fun SimpleErrorException.toSnackBarData() = SnackBarData(uiMessage)
