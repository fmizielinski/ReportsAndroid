package pl.fmizielinski.reports.domain.usecase.auth

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.db.dao.TokenDao

@Factory
class IsLoggedInUseCase(
    private val tokenDao: TokenDao,
) {

    suspend operator fun invoke(): Boolean {
        return tokenDao.hasToken()
    }
}
