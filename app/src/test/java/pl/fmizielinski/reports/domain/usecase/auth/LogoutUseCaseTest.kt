package pl.fmizielinski.reports.domain.usecase.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.data.db.dao.TokenDao

class LogoutUseCaseTest {

    private val tokenDao: TokenDao = mockk()

    private val useCase = LogoutUseCase(tokenDao)

    @Test
    fun `WHEN invoke THEN delete token`() = runTest {
        coEvery { tokenDao.deleteToken() } returns 1
        useCase()

        coVerify(exactly = 1) { tokenDao.deleteToken() }
    }
}
