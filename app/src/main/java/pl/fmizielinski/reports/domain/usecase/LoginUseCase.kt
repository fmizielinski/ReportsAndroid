package pl.fmizielinski.reports.domain.usecase

import okhttp3.Credentials
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.db.dao.UserDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.domain.mapper.toUserModel

@Factory
class LoginUseCase(
    private val authService: AuthService,
    private val userDao: UserDao,
) {

    suspend operator fun invoke(username: String, password: String) {
        val credentials = Credentials.basic(username, password)
        val loginResponseModel = authService.login(credentials)
        val userModel = loginResponseModel.toUserModel()
        val addUserResult = userDao.addUser(userModel)
        if (!addUserResult) {
            throw RuntimeException("Cannot create user")
        }
    }
}
