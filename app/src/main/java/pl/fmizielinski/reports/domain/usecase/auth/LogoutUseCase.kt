package pl.fmizielinski.reports.domain.usecase.auth

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.db.dao.TokenDao

@Factory
class LogoutUseCase(private val tokenDao: TokenDao) {

    suspend operator fun invoke() {
        tokenDao.deleteToken()
    }
}
