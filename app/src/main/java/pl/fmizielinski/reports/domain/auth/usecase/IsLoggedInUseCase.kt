package pl.fmizielinski.reports.domain.auth.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.domain.base.BaseUseCase

@Factory
class IsLoggedInUseCase(
    private val tokenDao: TokenDao,
) : BaseUseCase() {

    suspend operator fun invoke(): Boolean {
        return tokenDao.hasToken()
    }
}
