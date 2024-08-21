package pl.fmizielinski.reports.domain.usecase.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.data.network.auth.model.RegisterResponseModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.data.registerResponseModel
import pl.fmizielinski.reports.fixtures.data.tokenModel
import pl.fmizielinski.reports.fixtures.domain.registrationData
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class RegisterUseCaseTest {

    private val authService: AuthService = mockk()
    private val tokenDao: TokenDao = mockk()

    private val useCase = RegisterUseCase(authService, tokenDao)

    @Test
    fun `GIVEN valid registration data WHEN register THEN save token`() = runTest {
        val token = "token"
        val tokenModel = tokenModel(token = token)
        coEvery { authService.register(any()) } returns registerResponseModel(token = token)
        coEvery { tokenDao.addToken(tokenModel) } returns true

        useCase(registrationData())

        coVerify(exactly = 1) { tokenDao.addToken(tokenModel) }
    }

    @Test
    fun `GIVEN invalid registration data WHEN register THEN throw invalid credentials exception`() =
        runTest {
            val exception = httpException<RegisterResponseModel>(400)
            coEvery { authService.register(any()) } throws exception

            expectThrows<ErrorException> {
                useCase(registrationData())
            }.and {
                get { uiMessage } isEqualTo R.string.registerScreen_error_register
                get { message } isEqualTo "Invalid credentials"
            }
        }

    @Test
    fun `GIVEN unknown error WHEN register THEN throw unknown error exception`() = runTest {
        coEvery { authService.register(any()) } throws Exception()

        expectThrows<ErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo "Unknown register error"
        }
    }

    @Test
    fun `GIVEN unknown http error WHEN register THEN throw unknown error exception`() = runTest {
        val exception = httpException<RegisterResponseModel>(999)
        coEvery { authService.register(any()) } throws exception

        expectThrows<ErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo "Unknown register error"
        }
    }

    @Test
    fun `GIVEN cannot save token WHEN register THEN throw error exception`() = runTest {
        val token = "token"
        val tokenModel = tokenModel(token = token)
        coEvery { authService.register(any()) } returns registerResponseModel(token = token)
        coEvery { tokenDao.addToken(tokenModel) } returns false

        expectThrows<ErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo "Cannot save credentials"
        }
    }
}
