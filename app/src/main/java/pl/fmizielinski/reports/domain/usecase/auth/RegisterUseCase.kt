package pl.fmizielinski.reports.domain.usecase.auth

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.data.network.auth.model.RegisterRequestModel
import pl.fmizielinski.reports.data.network.auth.model.RegisterResponseModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.mapper.toTokenModel
import pl.fmizielinski.reports.domain.model.RegistrationData
import pl.fmizielinski.reports.domain.model.toRegisterRequestModel
import retrofit2.HttpException

@Factory
class RegisterUseCase(
    private val authService: AuthService,
    private val tokenDao: TokenDao,
) {

    @Suppress("TooGenericExceptionCaught")
    @Throws(ErrorException::class)
    suspend operator fun invoke(data: RegistrationData) {
        val requestModel = data.toRegisterRequestModel()
        val responseModel = register(requestModel)
        val tokenModel = responseModel.toTokenModel()
        if (!tokenDao.addToken(tokenModel)) {
            throw ErrorException(
                uiMessage = R.string.registerScreen_error_register,
                message = "Cannot save credentials",
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    @Throws(ErrorException::class)
    private suspend fun register(requestModel: RegisterRequestModel): RegisterResponseModel {
        return try {
            authService.register(requestModel)
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
            message = "Unknown register error",
            cause = cause,
        )
    }
}
