package pl.fmizielinski.reports.ui.login

import app.cash.turbine.testIn
import com.ramcosta.composedestinations.utils.startDestination
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.auth.usecase.LoginUseCase
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel.UiEvent
import pl.fmizielinski.reports.ui.destinations.navgraphs.MainNavGraph
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class LoginViewModelTest : BaseViewModelTest<LoginViewModel, UiEvent>() {
    private val loginUseCase: LoginUseCase = mockk()
    private val eventsRepository = spyk(EventsRepository())

    override fun createViewModel(dispatcher: TestDispatcher) = LoginViewModel(
        dispatcher = dispatcher,
        loginUseCase = loginUseCase,
        eventsRepository = eventsRepository,
    )

    @Test
    fun `GIVEN valid credentials WHEN Login global event posted THEN post MainNavGraph start destination navigation event`() = runTurbineTest {
        val email = "email"
        val password = "password"
        coJustRun { loginUseCase(email, password) }

        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.EmailChanged(email))
        postUiEvent(UiEvent.PasswordChanged(password))
        context.launch { eventsRepository.postGlobalEvent(GlobalEvent.Login) }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { loginUseCase(email, password) }
        coVerify(exactly = 1) { eventsRepository.postNavEvent(MainNavGraph.startDestination.toDestinationData()) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN valid credentials WHEN Login global event posted AND login error THEN show snackbar`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val errorException = simpleErrorException()
        val snackBarData = errorException.toSnackBarData()
        coEvery { loginUseCase(email, password) } throws errorException

        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.EmailChanged(email))
        postUiEvent(UiEvent.PasswordChanged(password))
        context.launch { eventsRepository.postGlobalEvent(GlobalEvent.Login) }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(snackBarData) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN password passed WHEN show password clicked THEN toggle show password`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.PasswordChanged("password"))
        postUiEvent(UiEvent.ShowPasswordClicked)
        scheduler.advanceUntilIdle()

        var result = uiState.expectMostRecentItem()
        expectThat(result.showPassword).isTrue()

        postUiEvent(UiEvent.ShowPasswordClicked)
        scheduler.advanceUntilIdle()

        result = uiState.expectMostRecentItem()
        expectThat(result.showPassword).isFalse()

        uiState.cancel()
    }
}
