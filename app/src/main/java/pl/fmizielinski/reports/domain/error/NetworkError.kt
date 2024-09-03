package pl.fmizielinski.reports.domain.error

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import retrofit2.HttpException

@Serializable
data class NetworkError(val code: String, val message: String)

fun NetworkError.errorException(
    @StringRes uiMessage: Int,
    exception: HttpException,
    isVerificationError: Boolean = false,
): SimpleErrorException {
    return SimpleErrorException(
        uiMessage = uiMessage,
        code = code,
        message = message,
        cause = exception,
        isVerificationError = isVerificationError,
    )
}
