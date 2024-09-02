package pl.fmizielinski.reports.ui.base

import pl.fmizielinski.reports.domain.error.CompositeErrorException
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.ui.base.ErrorHandler.VerificationError

interface ErrorHandler {

    suspend fun handleError(error: ErrorException) {
        if (error is SimpleErrorException) {
            if (error.isVerificationError) {
                val verificationError = parseVerificationError(error)
                handleVerificationError(listOf(verificationError))
            } else {
                handleNonVerificationError(error)
            }
        } else if (error is CompositeErrorException) {
            val verificationErrors = error.exceptions
                .filter { it.isVerificationError }
                .map(::parseVerificationError)
            handleVerificationError(verificationErrors)
            error.exceptions
                .filter { !it.isVerificationError }
                .forEach { handleNonVerificationError(it) }
        }
    }

    fun parseVerificationError(error: SimpleErrorException): VerificationError

    suspend fun handleVerificationError(verificationErrors: List<VerificationError>)

    suspend fun handleNonVerificationError(error: SimpleErrorException)

    interface VerificationError {
        val messageResId: Int
    }
}

inline fun <reified T : VerificationError> List<VerificationError>.filterIsNotInstance() =
    filter { it !is T }

inline fun <reified T : VerificationError> List<VerificationError>.findVerificationError() =
    firstOrNull { it is T }
        ?.messageResId


