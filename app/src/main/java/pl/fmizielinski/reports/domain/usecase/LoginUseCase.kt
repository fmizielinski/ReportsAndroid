package pl.fmizielinski.reports.domain.usecase

import okhttp3.Credentials
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.auth.AuthService

@Factory
class LoginUseCase(private val authService: AuthService) {

    suspend operator fun invoke(username: String, password: String) {
        val credentials = Credentials.basic(username, password)
        authService.login(credentials)
    }
}
