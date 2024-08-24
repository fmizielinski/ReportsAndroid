package pl.fmizielinski.reports.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.db.dao.TokenDao

@Factory
class IsLoggedInUseCase(
    private val tokenDao: TokenDao,
) {

    operator fun invoke(): Flow<Boolean> {
        return tokenDao.hasToken()
    }
}
