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
import pl.fmizielinski.reports.domain.auth.usecase.RegisterUseCase
import pl.fmizielinski.reports.domain.error.CompositeErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.EMAIL_NOT_VALID
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.INVALID_CREDENTIALS
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.NAME_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.PASSWORD_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.SURNAME_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.USER_ALREADY_EXISTS
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.data.registerResponseModel
import pl.fmizielinski.reports.fixtures.data.tokenModel
import pl.fmizielinski.reports.fixtures.domain.networkError
import pl.fmizielinski.reports.fixtures.domain.registrationData
import strikt.api.expectThrows
import strikt.assertions.hasSize
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
    fun `GIVEN invalid registration data WHEN register THEN throw invalid credentials exception`() = runTest {
        val errorMessage = "Invalid credentials"
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            error = networkError(INVALID_CREDENTIALS, errorMessage),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo errorMessage
            get { isVerificationError } isEqualTo false
        }
    }

    @Test
    fun `GIVEN invalid registration data WHEN register THEN throw user already exists exception`() = runTest {
        val errorMessage = "User already exists"
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            error = networkError(USER_ALREADY_EXISTS, errorMessage),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_userAlreadyExists
            get { message } isEqualTo errorMessage
            get { isVerificationError } isEqualTo false
        }
    }

    @Test
    fun `GIVEN invalid registration data WHEN register THEN throw email not valid exception`() = runTest {
        val errorMessage = "Email not valid"
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            error = networkError(EMAIL_NOT_VALID, errorMessage),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_emailNotValid
            get { message } isEqualTo errorMessage
            get { isVerificationError } isEqualTo true
        }
    }

    @Test
    fun `GIVEN invalid registration data WHEN register THEN throw name empty exception`() = runTest {
        val errorMessage = "Name empty"
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            error = networkError(NAME_EMPTY, errorMessage),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_nameEmpty
            get { message } isEqualTo errorMessage
            get { isVerificationError } isEqualTo true
        }
    }

    @Test
    fun `GIVEN invalid registration data WHEN register THEN throw surname empty exception`() = runTest {
        val errorMessage = "Surname empty"
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            error = networkError(SURNAME_EMPTY, errorMessage),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_surnameEmpty
            get { message } isEqualTo errorMessage
            get { isVerificationError } isEqualTo true
        }
    }

    @Test
    fun `GIVEN invalid registration data WHEN register THEN throw password empty exception`() = runTest {
        val errorMessage = "Surname empty"
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            error = networkError(PASSWORD_EMPTY, errorMessage),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_passwordEmpty
            get { message } isEqualTo errorMessage
            get { isVerificationError } isEqualTo true
        }
    }

    @Test
    fun `GIVEN invalid registration data WHEN register THEN throw composite exception`() = runTest {
        val errorMessage = "message"
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            errors = listOf(
                networkError(PASSWORD_EMPTY, errorMessage),
                networkError(EMAIL_NOT_VALID, errorMessage),
            ),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<CompositeErrorException> {
            useCase(registrationData())
        }.get { exceptions }.hasSize(2)
    }

    @Test
    fun `GIVEN unknown error WHEN register THEN throw unknown error exception`() = runTest {
        coEvery { authService.register(any()) } throws Exception()

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo "Unknown register error"
            get { isVerificationError } isEqualTo false
        }
    }

    @Test
    fun `GIVEN unknown http error WHEN register THEN throw unknown error exception`() = runTest {
        val exception = httpException<RegisterResponseModel>(999)
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo "Unknown register error"
            get { isVerificationError } isEqualTo false
        }
    }

    @Test
    fun `GIVEN unknown 400 http error WHEN register THEN throw unknown error exception`() = runTest {
        val exception = httpException<RegisterResponseModel>(
            code = 400,
            error = networkError("unknown", "Unknown error"),
        )
        coEvery { authService.register(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo "Unknown register error"
            get { isVerificationError } isEqualTo false
        }
    }

    @Test
    fun `GIVEN cannot save token WHEN register THEN throw error exception`() = runTest {
        val token = "token"
        val tokenModel = tokenModel(token = token)
        coEvery { authService.register(any()) } returns registerResponseModel(token = token)
        coEvery { tokenDao.addToken(tokenModel) } returns false

        expectThrows<SimpleErrorException> {
            useCase(registrationData())
        }.and {
            get { uiMessage } isEqualTo R.string.registerScreen_error_register
            get { message } isEqualTo "Cannot save credentials"
            get { isVerificationError } isEqualTo false
        }
    }
}
