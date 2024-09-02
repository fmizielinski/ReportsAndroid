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
        handler: (HttpException) -> ErrorException,
    ): T = try {
        body()
    } catch (e: HttpException) {
        throw handler(e)
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
