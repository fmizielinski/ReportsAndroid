package pl.fmizielinski.reports.domain.auth.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.domain.networkError
import strikt.api.expectDoesNotThrow

class LogoutUseCaseTest {

    private val authService: AuthService = mockk()
    private val tokenDao: TokenDao = mockk()

    private val useCase = LogoutUseCase(authService, tokenDao)

    @Test
    fun `GIVEN logout success WHEN invoke THEN delete token`() = runTest {
        coJustRun { authService.logout() }
        coEvery { tokenDao.deleteToken() } returns 1

        expectDoesNotThrow { useCase() }

        coVerify(exactly = 1) { tokenDao.deleteToken() }
    }

    @Test
    fun `GIVEN 400 http error WHEN invoke THEN delete token`() = runTest {
        val errorMessage = "errorMessage"
        val errorCode = "errorCode"
        val exception = httpException<Unit>(
            code = 400,
            error = networkError(errorCode, errorMessage),
        )
        coEvery { authService.logout() } throws exception
        coEvery { tokenDao.deleteToken() } returns 1

        expectDoesNotThrow { useCase() }

        coVerify(exactly = 1) { tokenDao.deleteToken() }
    }

    @Test
    fun `GIVEN unknown http error WHEN invoke THEN delete token`() = runTest {
        coEvery { authService.logout() } throws httpException<Unit>(999)
        coEvery { tokenDao.deleteToken() } returns 1

        expectDoesNotThrow { useCase() }

        coVerify(exactly = 1) { tokenDao.deleteToken() }
    }
}
