package pl.fmizielinski.reports.ui.register

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.EMAIL_NOT_VALID
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.NAME_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.PASSWORD_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.SURNAME_EMPTY
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.RegisterUseCase
import pl.fmizielinski.reports.fixtures.domain.compositeErrorException
import pl.fmizielinski.reports.fixtures.domain.registrationData
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.UiEvent
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class RegisterViewModelTest : BaseViewModelTest<RegisterViewModel, UiEvent>() {

    private val registerUseCase: RegisterUseCase = mockk()
    private val eventsRepository = spyk(EventsRepository())

    override fun createViewModel(dispatcher: TestDispatcher) = RegisterViewModel(
        dispatcher = dispatcher,
        registerUseCase = registerUseCase,
        eventsRepository = eventsRepository,
    )

    @ParameterizedTest
    @CsvSource(
        "email, password, passwordConfirmation, name, surname, true",
        "email, password, passwordConfirmation, name, , false",
        "email, password, passwordConfirmation, , surname, false",
        "email, password, passwordConfirmation, , , false",
        "email, password, , name, surname, false",
        "email, password, , name, , false",
        "email, password, , , surname, false",
        "email, password, , , , false",
        "email, , passwordConfirmation, name, surname, false",
        "email, , passwordConfirmation, name, , false",
        "email, , passwordConfirmation, , surname, false",
        "email, , passwordConfirmation, , , false",
        "email, , , name, surname, false",
        "email, , , name, , false",
        "email, , , , surname, false",
        "email, , , , , false",
        ", password, passwordConfirmation, name, surname, false",
        ", password, passwordConfirmation, name, , false",
        ", password, passwordConfirmation, , surname, false",
        ", password, passwordConfirmation, , , false",
        ", password, , name, surname, false",
        ", password, , name, , false",
        ", password, , , surname, false",
        ", password, , , , false",
        ", , passwordConfirmation, name, surname, false",
        ", , passwordConfirmation, name, , false",
        ", , passwordConfirmation, , surname, false",
        ", , passwordConfirmation, , , false",
        ", , , name, surname, false",
        ", , , name, , false",
        ", , , , surname, false",
        ", , , , , false",
    )
    fun `WHEN data passed THEN change register button`(
        email: String?,
        password: String?,
        passwordConfirmation: String?,
        name: String?,
        surname: String?,
        expected: Boolean?,
    ) = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.EmailChanged(email.orEmpty()))
        postUiEvent(UiEvent.PasswordChanged(password.orEmpty()))
        postUiEvent(UiEvent.PasswordConfirmationChanged(passwordConfirmation.orEmpty()))
        postUiEvent(UiEvent.NameChanged(name.orEmpty()))
        postUiEvent(UiEvent.SurnameChanged(surname.orEmpty()))
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.isRegisterButtonEnabled) isEqualTo expected

        uiState.cancel()
    }

    @Test
    fun `GIVEN valid data passed WHEN register clicked THEN disable register button`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        coJustRun { registerUseCase.invoke(registrationData(email, name, surname, password)) }

        val uiState = postData(email, password, passwordConfirmation, name, surname)
        postUiEvent(UiEvent.RegisterClicked)

        val result = uiState.awaitItem()
        expectThat(result.isRegisterButtonEnabled).isFalse()

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN invalid credentials passed WHEN register clicked THEN show password verification error`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "passwordConfirmation"
        val name = "name"
        val surname = "surname"
        coJustRun { registerUseCase.invoke(registrationData(email, name, surname, password)) }

        val uiState = postData(email, password, passwordConfirmation, name, surname)
        postUiEvent(UiEvent.RegisterClicked)
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.loginData.passwordVerificationError)
            .isNotNull()
            .isEqualTo(R.string.registerScreen_error_password)

        uiState.cancel()
    }

    @Test
    fun `GIVEN password passed WHEN show password clicked THEN toggle show password`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.PasswordChanged("password"))
        postUiEvent(UiEvent.ShowPasswordClicked)
        scheduler.advanceUntilIdle()

        var result = uiState.expectMostRecentItem()
        expectThat(result.loginData.showPassword).isTrue()

        postUiEvent(UiEvent.ShowPasswordClicked)
        scheduler.advanceUntilIdle()

        result = uiState.expectMostRecentItem()
        expectThat(result.loginData.showPassword).isFalse()

        uiState.cancel()
    }

    @Test
    fun `GIVEN valid data passed WHEN register clicked AND register error THEN show snackbar`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        val errorException = simpleErrorException(isVerificationError = false)
        val snackBarData = errorException.toSnackBarData()
        coEvery { registerUseCase.invoke(registrationData(email, name, surname, password)) } throws errorException

        val uiState = postData(email, password, passwordConfirmation, name, surname)

        postUiEvent(UiEvent.RegisterClicked)
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(snackBarData) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN valid data passed WHEN register clicked AND register verification errors THEN show verification errors`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        val errorException = compositeErrorException(
            exceptions = listOf(
                simpleErrorException(code = EMAIL_NOT_VALID, isVerificationError = true),
                simpleErrorException(code = PASSWORD_EMPTY, isVerificationError = true),
                simpleErrorException(code = NAME_EMPTY, isVerificationError = true),
                simpleErrorException(code = SURNAME_EMPTY, isVerificationError = true),
            ),
        )
        coEvery { registerUseCase.invoke(registrationData(email, name, surname, password)) } throws errorException

        val uiState = postData(email, password, passwordConfirmation, name, surname)

        postUiEvent(UiEvent.RegisterClicked)
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result) {
            get { loginData.emailVerificationError }.isNotNull()
            get { loginData.passwordVerificationError }.isNotNull()
            get { userData.nameVerificationError }.isNotNull()
            get { userData.surnameVerificationError }.isNotNull()
        }

        uiState.cancel()
    }

    @Test
    fun `GIVEN valid data passed WHEN register clicked AND register verification error THEN show verification error`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        val errorException = simpleErrorException(code = EMAIL_NOT_VALID, isVerificationError = true)
        coEvery { registerUseCase.invoke(registrationData(email, name, surname, password)) } throws errorException

        val uiState = postData(email, password, passwordConfirmation, name, surname)

        postUiEvent(UiEvent.RegisterClicked)
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.loginData.emailVerificationError).isNotNull()

        uiState.cancel()
    }

    private fun TestContext<RegisterViewModel, UiEvent>.postData(
        email: String,
        password: String,
        passwordConfirmation: String,
        name: String,
        surname: String,
    ): ReceiveTurbine<RegisterViewModel.UiState> {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.EmailChanged(email))
        postUiEvent(UiEvent.PasswordChanged(password))
        postUiEvent(UiEvent.PasswordConfirmationChanged(passwordConfirmation))
        postUiEvent(UiEvent.NameChanged(name))
        postUiEvent(UiEvent.SurnameChanged(surname))
        scheduler.advanceUntilIdle()

        return uiState
    }
}
