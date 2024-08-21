package pl.fmizielinski.reports.domain.usecase.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.data.network.auth.model.LoginResponseModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.data.loginResponseModel
import pl.fmizielinski.reports.fixtures.data.tokenModel
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class LoginUseCaseTest {

    private val authService: AuthService = mockk()
    private val tokenDao: TokenDao = mockk()

    private val useCase = LoginUseCase(authService, tokenDao)

    @Test
    fun `GIVEN valid credentials WHEN login THEN save token`() = runTest {
        val token = "token"
        val tokenModel = tokenModel(token = token)
        coEvery { authService.login(any()) } returns loginResponseModel(token = token)
        coEvery { tokenDao.addToken(tokenModel) } returns true

        useCase("username", "password")

        coVerify(exactly = 1) { tokenDao.addToken(tokenModel) }
    }

    @Test
    fun `GIVEN invalid credentials WHEN login THEN throw invalid credentials exception`() =
        runTest {
            val exception = httpException<LoginResponseModel>(401)
            coEvery { authService.login(any()) } throws exception

            expectThrows<ErrorException> {
                useCase("username", "password")
            }.and {
                get { uiMessage } isEqualTo R.string.loginScreen_error_invalidCredentials
                get { message } isEqualTo "Invalid credentials"
            }
        }

    @Test
    fun `GIVEN unknown error WHEN login THEN throw unknown error exception`() = runTest {
        coEvery { authService.login(any()) } throws Exception()

        expectThrows<ErrorException> {
            useCase("username", "password")
        }.and {
            get { uiMessage } isEqualTo R.string.loginScreen_error_login
            get { message } isEqualTo "Unknown login error"
        }
    }

    @Test
    fun `GIVEN unknown http error WHEN login THEN throw unknown error exception`() = runTest {
        val exception = httpException<LoginResponseModel>(999)
        coEvery { authService.login(any()) } throws exception

        expectThrows<ErrorException> {
            useCase("username", "password")
        }.and {
            get { uiMessage } isEqualTo R.string.loginScreen_error_login
            get { message } isEqualTo "Unknown login error"
        }
    }

    @Test
    fun `GIVEN valid credentials WHEN saving token error THEN throw save credentials exception`() = runTest {
        val token = "token"
        val tokenModel = tokenModel(token = token)
        coEvery { authService.login(any()) } returns loginResponseModel(token = token)
        coEvery { tokenDao.addToken(tokenModel) } returns false

        expectThrows<ErrorException> {
            useCase("username", "password")
        }.and {
            get { uiMessage } isEqualTo R.string.loginScreen_error_login
            get { message } isEqualTo "Cannot save credentials"
        }
    }
}
