package pl.fmizielinski.reports.domain.usecase.auth

import androidx.annotation.StringRes
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.data.network.auth.model.RegisterRequestModel
import pl.fmizielinski.reports.data.network.auth.model.RegisterResponseModel
import pl.fmizielinski.reports.domain.error.CompositeErrorException
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.NetworkError
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.mapper.parseErrorBody
import pl.fmizielinski.reports.domain.mapper.toTokenModel
import pl.fmizielinski.reports.domain.model.RegistrationData
import pl.fmizielinski.reports.domain.model.toRegisterRequestModel
import pl.fmizielinski.reports.domain.usecase.BaseUseCase
import retrofit2.HttpException

@Factory
class RegisterUseCase(
    private val authService: AuthService,
    private val tokenDao: TokenDao,
): BaseUseCase() {

    @Suppress("TooGenericExceptionCaught")
    @Throws(ErrorException::class)
    suspend operator fun invoke(data: RegistrationData) {
        val requestModel = data.toRegisterRequestModel()
        val responseModel = register(requestModel)
        val tokenModel = responseModel.toTokenModel()
        if (!tokenDao.addToken(tokenModel)) {
            throw SimpleErrorException(
                uiMessage = R.string.registerScreen_error_register,
                message = "Cannot save credentials",
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    @Throws(ErrorException::class)
    private suspend fun register(requestModel: RegisterRequestModel): RegisterResponseModel {
        return catchHttpExceptions(
            body = { authService.register(requestModel) },
            handler = { it.toErrorException() },
        )
    }

    private fun HttpException.toErrorException(): ErrorException {
        return if (code() == 400) {
            val exceptions = parseErrorBody().map { error ->
                when (error.code) {
                    ErrorReasons.Auth.Register.INVALID_CREDENTIALS -> error.errorException(
                        uiMessage = R.string.registerScreen_error_register,
                        exception = this,
                    )

                    ErrorReasons.Auth.Register.USER_ALREADY_EXISTS -> error.errorException(
                        uiMessage = R.string.registerScreen_error_userAlreadyExists,
                        exception = this,
                    )

                    ErrorReasons.Auth.Register.EMAIL_NOT_VALID -> error.errorException(
                        uiMessage = R.string.registerScreen_error_emailNotValid,
                        exception = this,
                        isVerificationError = true,
                    )

                    ErrorReasons.Auth.Register.NAME_EMPTY -> error.errorException(
                        uiMessage = R.string.registerScreen_error_nameEmpty,
                        exception = this,
                        isVerificationError = true,
                    )

                    ErrorReasons.Auth.Register.SURNAME_EMPTY -> error.errorException(
                        uiMessage = R.string.registerScreen_error_surnameEmpty,
                        exception = this,
                        isVerificationError = true,
                    )

                    ErrorReasons.Auth.Register.PASSWORD_EMPTY -> error.errorException(
                        uiMessage = R.string.registerScreen_error_passwordEmpty,
                        exception = this,
                        isVerificationError = true,
                    )

                    else -> genericErrorException(this)
                }
            }
            when {
                exceptions.size == 1 -> exceptions.first()
                else -> CompositeErrorException(exceptions)
            }
        } else {
            genericErrorException(this)
        }
    }

    private fun NetworkError.errorException(
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

    override fun genericErrorException(cause: Throwable): SimpleErrorException {
        return SimpleErrorException(
            uiMessage = R.string.registerScreen_error_register,
            message = "Unknown register error",
            cause = cause,
        )
    }
}
