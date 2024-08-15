package pl.fmizielinski.reports.domain.usecase.auth

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.model.RegistrationData
import pl.fmizielinski.reports.domain.model.toRegisterRequestModel
import retrofit2.HttpException

@Factory
class RegisterUseCase(
    private val authService: AuthService,
) {

    @Suppress("TooGenericExceptionCaught")
    @Throws(ErrorException::class)
    suspend operator fun invoke(data: RegistrationData) {
        try {
            authService.register(data.toRegisterRequestModel())
        } catch (e: HttpException) {
            throw e.toErrorException()
        } catch (e: Exception) {
            throw genericErrorException(e)
        }
    }

    private fun HttpException.toErrorException(): ErrorException {
        return when (code()) {
            400 -> ErrorException(
                uiMessage = R.string.registerScreen_error_register,
                message = "Invalid credentials",
                cause = this,
            )

            else -> genericErrorException(this)
        }
    }

    private fun genericErrorException(cause: Throwable): ErrorException {
        return ErrorException(
            uiMessage = R.string.registerScreen_error_register,
            message = "Unknown login error",
            cause = cause,
        )
    }
}
