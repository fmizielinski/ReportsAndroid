package pl.fmizielinski.reports.domain.usecase.auth

import okhttp3.Credentials
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.data.network.auth.model.LoginResponseModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.mapper.toTokenModel
import pl.fmizielinski.reports.domain.usecase.base.BaseUseCase
import retrofit2.HttpException

@Factory
class LoginUseCase(
    private val authService: AuthService,
    private val tokenDao: TokenDao,
): BaseUseCase() {

    @Throws(SimpleErrorException::class)
    suspend operator fun invoke(
        username: String,
        password: String,
    ) {
        val credentials = Credentials.basic(username, password)
        val loginResponseModel = login(credentials)
        val tokenModel = loginResponseModel.toTokenModel()
        if (!tokenDao.addToken(tokenModel)) {
            throw SimpleErrorException(
                uiMessage = R.string.loginScreen_error_login,
                message = "Cannot save credentials",
            )
        }
    }

    @Throws(SimpleErrorException::class)
    private suspend fun login(credentials: String): LoginResponseModel {
        return catchHttpExceptions(
            body = { authService.login(credentials) },
            handler = { it.toErrorException() },
        )
    }

    private fun HttpException.toErrorException(): SimpleErrorException {
        return when (code()) {
            401 -> SimpleErrorException(
                uiMessage = R.string.loginScreen_error_invalidCredentials,
                message = "Invalid credentials",
                cause = this,
            )

            else -> genericErrorException(this)
        }
    }

    override fun genericErrorException(cause: Throwable): SimpleErrorException {
        return SimpleErrorException(
            uiMessage = R.string.loginScreen_error_login,
            message = "Unknown login error",
            cause = cause,
        )
    }
}
