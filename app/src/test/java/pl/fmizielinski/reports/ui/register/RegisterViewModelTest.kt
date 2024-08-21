package pl.fmizielinski.reports.ui.register

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.RegisterUseCase
import pl.fmizielinski.reports.fixtures.domain.errorException
import pl.fmizielinski.reports.fixtures.domain.registrationData
import pl.fmizielinski.reports.ui.register.RegisterViewModel.UiEvent
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class RegisterViewModelTest : BaseViewModelTest<RegisterViewModel>() {

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
        uiState.skipItems(1)

        viewModel.postUiEvent(UiEvent.EmailChanged(email.orEmpty()))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.PasswordChanged(password.orEmpty()))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.PasswordConfirmationChanged(passwordConfirmation.orEmpty()))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.NameChanged(name.orEmpty()))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.SurnameChanged(surname.orEmpty()))

        val result = uiState.awaitItem()
        expectThat(result.isRegisterButtonEnabled) isEqualTo expected

        uiState.cancelAndIgnoreRemainingEvents()
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
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.RegisterClicked)

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
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.RegisterClicked)

        val result = uiState.awaitItem()
        expectThat(result.loginData.passwordVerificationError).isFalse()

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN password passed WHEN show password clicked THEN toggle show password`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)
        uiState.skipItems(1)

        viewModel.postUiEvent(UiEvent.PasswordChanged("password"))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.ShowPasswordClicked)

        var result = uiState.awaitItem()
        expectThat(result.loginData.showPassword).isTrue()

        viewModel.postUiEvent(UiEvent.ShowPasswordClicked)

        result = uiState.awaitItem()
        expectThat(result.loginData.showPassword).isFalse()

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN valid data passed WHEN register clicked AND register error THEN show snackbar`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        val errorException = errorException()
        val snackBarData = errorException.toSnackBarData()
        coEvery { registerUseCase.invoke(registrationData(email, name, surname, password)) } throws errorException

        val uiState = postData(email, password, passwordConfirmation, name, surname)
        uiState.skipItems(1)

        viewModel.postUiEvent(UiEvent.RegisterClicked)
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(snackBarData) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    private suspend fun TestContext<RegisterViewModel>.postData(
        email: String,
        password: String,
        passwordConfirmation: String,
        name: String,
        surname: String,
    ): ReceiveTurbine<RegisterViewModel.UiState> {
        val uiState = viewModel.uiState.testIn(context)
        uiState.skipItems(1)

        viewModel.postUiEvent(UiEvent.EmailChanged(email))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.PasswordChanged(password))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.PasswordConfirmationChanged(passwordConfirmation))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.NameChanged(name))
        uiState.skipItems(1)
        viewModel.postUiEvent(UiEvent.SurnameChanged(surname))

        return uiState
    }
}
