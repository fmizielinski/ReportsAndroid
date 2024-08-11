package pl.fmizielinski.reports.domain.usecase.auth

import okhttp3.Credentials
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.db.dao.UserDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.mapper.toUserModel
import retrofit2.HttpException

@Factory
class LoginUseCase(
    private val authService: AuthService,
    private val userDao: UserDao,
) {

    suspend operator fun invoke(username: String, password: String) {
        val credentials = Credentials.basic(username, password)
        val loginResponseModel = try {
            authService.login(credentials)
        } catch (e: HttpException) {
            throw e.toErrorException()
        } catch (e: Exception) {
            throw genericErrorException(e)
        }
        val userModel = loginResponseModel.toUserModel()
        if (!userDao.addUser(userModel)) {
            throw ErrorException(
                uiMessage = R.string.loginScreen_error_login,
                message = "Cannot save credentials",
            )
        }
    }

    private fun HttpException.toErrorException(): ErrorException {
        return when (code()) {
            401 -> ErrorException(
                uiMessage = R.string.loginScreen_error_invalidCredentials,
                message = "Invalid credentials",
                cause = this,
            )

            else -> genericErrorException(this)
        }
    }

    private fun genericErrorException(cause: Throwable): ErrorException {
        return ErrorException(
            uiMessage = R.string.loginScreen_error_login,
            message = "Unknown login error",
            cause = cause,
        )
    }
}
