package pl.fmizielinski.reports.ui.login

import app.cash.turbine.turbineScope
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.LoginUseCase
import pl.fmizielinski.reports.fixtures.domain.errorException
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiEvent
import strikt.api.expectThat
import strikt.assertions.isBlank
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class LoginViewModelTest {
    private val loginUseCase: LoginUseCase = mockk()
    private val eventsRepository = spyk(EventsRepository())

    private fun viewModel(scheduler: TestCoroutineScheduler) =
        LoginViewModel(
            dispatcher = StandardTestDispatcher(scheduler),
            loginUseCase = loginUseCase,
            eventsRepository = eventsRepository,
        )

    @Test
    fun `WHEN no credentials passed THEN disable login button`() =
        runTest {
            val viewModel = viewModel(testScheduler)

            turbineScope {
                val uiState = viewModel.uiState.testIn(this)

                val result = uiState.awaitItem()
                expectThat(result.isLoginButtonEnabled).isFalse()

                uiState.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `WHEN no email passed THEN disable login button`() =
        runTest {
            val viewModel = viewModel(testScheduler)

            turbineScope {
                val uiState = viewModel.uiState.testIn(this)
                uiState.skipItems(1)

                viewModel.postUiEvent(UiEvent.PasswordChanged("password"))

                val result = uiState.awaitItem()
                expectThat(result.isLoginButtonEnabled).isFalse()

                uiState.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `WHEN no password passed THEN disable login button`() =
        runTest {
            val viewModel = viewModel(testScheduler)

            turbineScope {
                val uiState = viewModel.uiState.testIn(this)
                uiState.skipItems(1)

                viewModel.postUiEvent(UiEvent.EmailChanged("email"))

                val result = uiState.awaitItem()
                expectThat(result.isLoginButtonEnabled).isFalse()

                uiState.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `WHEN email and password passed THEN enable login button`() =
        runTest {
            val viewModel = viewModel(testScheduler)

            turbineScope {
                val uiState = viewModel.uiState.testIn(this)
                uiState.skipItems(1)

                viewModel.postUiEvent(UiEvent.EmailChanged("email"))
                uiState.skipItems(1)
                viewModel.postUiEvent(UiEvent.PasswordChanged("password"))

                val result = uiState.awaitItem()
                expectThat(result.isLoginButtonEnabled).isTrue()

                uiState.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN valid credentials WHEN login clicked THEN disable login button AND clear password`() =
        runTest {
            val email = "email"
            val password = "password"
            coJustRun { loginUseCase(email, password) }

            val viewModel = viewModel(testScheduler)

            turbineScope {
                val uiState = viewModel.uiState.testIn(this)
                uiState.skipItems(1)

                viewModel.postUiEvent(UiEvent.EmailChanged(email))
                uiState.skipItems(1)
                viewModel.postUiEvent(UiEvent.PasswordChanged(password))
                uiState.skipItems(1)
                viewModel.postUiEvent(UiEvent.LoginClicked)

                var result = uiState.awaitItem()
                expectThat(result.isLoginButtonEnabled).isFalse()
                result = uiState.awaitItem()
                expectThat(result.password).isBlank()

                uiState.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN valid credentials WHEN login clicked AND login error THEN show snackbar AND clear password`() =
        runTest {
            val email = "email"
            val password = "password"
            val errorException =
                errorException(
                    uiMessage = 1,
                    message = "message",
                    cause = Exception("cause"),
                )
            val snackBarData = errorException.toSnackBarData()
            coEvery { loginUseCase(email, password) } throws errorException

            val viewModel = viewModel(testScheduler)

            turbineScope {
                val uiState = viewModel.uiState.testIn(this)
                uiState.skipItems(1)

                viewModel.postUiEvent(UiEvent.EmailChanged(email))
                uiState.skipItems(1)
                viewModel.postUiEvent(UiEvent.PasswordChanged(password))
                uiState.skipItems(1)
                viewModel.postUiEvent(UiEvent.LoginClicked)
                uiState.skipItems(1)

                val result = uiState.awaitItem()
                expectThat(result.password).isBlank()

                coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(snackBarData) }

                uiState.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN password passed WHEN show password clicked THEN toggle show password`() =
        runTest {
            val viewModel = viewModel(testScheduler)

            turbineScope {
                val uiState = viewModel.uiState.testIn(this)
                uiState.skipItems(1)

                viewModel.postUiEvent(UiEvent.PasswordChanged("password"))
                uiState.skipItems(1)
                viewModel.postUiEvent(UiEvent.ShowPasswordClicked)

                var result = uiState.awaitItem()
                expectThat(result.showPassword).isTrue()

                viewModel.postUiEvent(UiEvent.ShowPasswordClicked)

                result = uiState.awaitItem()
                expectThat(result.showPassword).isFalse()

                uiState.cancelAndIgnoreRemainingEvents()
            }
        }
}
