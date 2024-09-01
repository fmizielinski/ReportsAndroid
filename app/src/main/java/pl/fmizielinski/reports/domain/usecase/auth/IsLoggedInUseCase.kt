package pl.fmizielinski.reports.domain.usecase.auth

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.domain.usecase.BaseUseCase

@Factory
class IsLoggedInUseCase(
    private val tokenDao: TokenDao,
) : BaseUseCase() {

    suspend operator fun invoke(): Boolean {
        return tokenDao.hasToken()
    }
}
