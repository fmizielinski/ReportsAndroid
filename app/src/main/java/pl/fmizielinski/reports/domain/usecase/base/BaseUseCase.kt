package pl.fmizielinski.reports.domain.usecase.base

import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.domain.error.CompositeErrorException
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import retrofit2.HttpException

abstract class BaseUseCase {

    @Suppress("TooGenericExceptionCaught")
    @Throws(HttpException::class)
    protected inline fun <reified T> catchHttpExceptions(
        body: () -> T,
        noinline handler: ((HttpException) -> ErrorException)? = null,
        noinline fallback: ((HttpException) -> T)? = null,
    ): T = try {
        body()
    } catch (e: HttpException) {
        if (handler != null) {
            throw handler(e)
        } else {
            requireNotNull(fallback) {
                "Fallback must be provided if handler is omitted to handle HttpException"
            }.invoke(e)
        }
    } catch (e: Exception) {
        throw genericErrorException(e)
    }

    protected open fun genericErrorException(cause: Throwable): SimpleErrorException {
        return SimpleErrorException(
            uiMessage = R.string.common_error_unknown,
            message = "Unknown error",
            cause = cause,
        )
    }

    protected fun List<SimpleErrorException>.asErrorException(): ErrorException {
        return when {
            size == 1 -> first()
            else -> CompositeErrorException(this)
        }
    }
}
