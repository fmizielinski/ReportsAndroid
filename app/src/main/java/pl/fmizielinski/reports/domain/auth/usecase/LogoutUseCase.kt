package pl.fmizielinski.reports.domain.auth.usecase

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.di.NetworkModule
import pl.fmizielinski.reports.domain.base.BaseUseCase
import timber.log.Timber

@Factory
class LogoutUseCase(
    @Named(NetworkModule.BEARER_AUTH_SERVICE) private val authService: AuthService,
    private val tokenDao: TokenDao,
) : BaseUseCase() {

    suspend operator fun invoke() {
        catchHttpExceptions(
            body = { authService.logout() },
            fallback = { Timber.i(it, "Logout network error") },
        )
        tokenDao.deleteToken()
    }
}
