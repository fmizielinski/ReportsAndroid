package pl.fmizielinski.reports.ui.login

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
import pl.fmizielinski.reports.domain.usecase.auth.LoginUseCase
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel.UiEvent
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class LoginViewModelTest : BaseViewModelTest<LoginViewModel>() {
    private val loginUseCase: LoginUseCase = mockk()
    private val eventsRepository = spyk(EventsRepository())

    override fun createViewModel(dispatcher: TestDispatcher) = LoginViewModel(
        dispatcher = dispatcher,
        loginUseCase = loginUseCase,
        eventsRepository = eventsRepository,
    )

    @ParameterizedTest
    @CsvSource(
        ", , false",
        ", password, false",
        "email, , false",
        "email, password, true",
    )
    fun `WHEN credentials passed THEN disable login button`(
        email: String?,
        password: String?,
        expected: Boolean,
    ) = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.postUiEvent(UiEvent.EmailChanged(email.orEmpty())) }
        context.launch { viewModel.postUiEvent(UiEvent.PasswordChanged(password.orEmpty())) }
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.isLoginButtonEnabled) isEqualTo expected

        uiState.cancel()
    }

    @Test
    fun `GIVEN valid credentials WHEN login clicked THEN disable login button`() = runTurbineTest {
        val email = "email"
        val password = "password"
        coJustRun { loginUseCase(email, password) }

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.postUiEvent(UiEvent.EmailChanged(email)) }
        context.launch { viewModel.postUiEvent(UiEvent.PasswordChanged(password)) }
        context.launch { viewModel.postUiEvent(UiEvent.LoginClicked) }
        scheduler.advanceUntilIdle()
        uiState.skipItems(2)

        val result = uiState.awaitItem()
        expectThat(result.isLoginButtonEnabled).isFalse()

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN valid credentials WHEN login clicked AND login error THEN show snackbar`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val errorException = simpleErrorException()
        val snackBarData = errorException.toSnackBarData()
        coEvery { loginUseCase(email, password) } throws errorException

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.postUiEvent(UiEvent.EmailChanged(email)) }
        context.launch { viewModel.postUiEvent(UiEvent.PasswordChanged(password)) }
        context.launch { viewModel.postUiEvent(UiEvent.LoginClicked) }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(snackBarData) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN password passed WHEN show password clicked THEN toggle show password`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.postUiEvent(UiEvent.PasswordChanged("password")) }
        context.launch { viewModel.postUiEvent(UiEvent.ShowPasswordClicked) }
        scheduler.advanceUntilIdle()

        var result = uiState.expectMostRecentItem()
        expectThat(result.showPassword).isTrue()

        context.launch { viewModel.postUiEvent(UiEvent.ShowPasswordClicked) }
        scheduler.advanceUntilIdle()

        result = uiState.expectMostRecentItem()
        expectThat(result.showPassword).isFalse()

        uiState.cancel()
    }
}
