package pl.fmizielinski.reports.ui.register

import app.cash.turbine.ReceiveTurbine
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
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.EMAIL_NOT_VALID
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.NAME_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.PASSWORD_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.SURNAME_EMPTY
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.domain.usecase.auth.RegisterUseCase
import pl.fmizielinski.reports.fixtures.domain.compositeErrorException
import pl.fmizielinski.reports.fixtures.domain.registrationData
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.UiEvent
import pl.fmizielinski.reports.ui.destinations.navgraphs.MainNavGraph
import pl.fmizielinski.reports.ui.navigation.toDestinationData
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

    @Test
    fun `GIVEN valid data passed WHEN Register global event posted THEN post MainNavGraph start destination navigation event`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        coJustRun { registerUseCase(registrationData(email, name, surname, password)) }

        val uiState = postData(email, password, passwordConfirmation, name, surname)
        context.launch { eventsRepository.postGlobalEvent(GlobalEvent.Register) }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { registerUseCase(registrationData(email, name, surname, password)) }
        coVerify(exactly = 1) { eventsRepository.postNavEvent(MainNavGraph.startDestination.toDestinationData()) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN invalid credentials passed WHEN Register global event posted THEN show password verification error`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "passwordConfirmation"
        val name = "name"
        val surname = "surname"
        coJustRun { registerUseCase.invoke(registrationData(email, name, surname, password)) }

        val uiState = postData(email, password, passwordConfirmation, name, surname)
        context.launch { eventsRepository.postGlobalEvent(GlobalEvent.Register) }
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
    fun `GIVEN valid data passed WHEN Register global event posted AND register error THEN show snackbar`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        val errorException = simpleErrorException(isVerificationError = false)
        val snackBarData = errorException.toSnackBarData()
        coEvery { registerUseCase.invoke(registrationData(email, name, surname, password)) } throws errorException

        val uiState = postData(email, password, passwordConfirmation, name, surname)

        context.launch { eventsRepository.postGlobalEvent(GlobalEvent.Register) }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(snackBarData) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN valid data passed WHEN Register global event posted AND register verification errors THEN show verification errors`() = runTurbineTest {
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

        context.launch { eventsRepository.postGlobalEvent(GlobalEvent.Register) }
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
    fun `GIVEN valid data passed WHEN Register global event posted AND register verification error THEN show verification error`() = runTurbineTest {
        val email = "email"
        val password = "password"
        val passwordConfirmation = "password"
        val name = "name"
        val surname = "surname"
        val errorException = simpleErrorException(code = EMAIL_NOT_VALID, isVerificationError = true)
        coEvery { registerUseCase.invoke(registrationData(email, name, surname, password)) } throws errorException

        val uiState = postData(email, password, passwordConfirmation, name, surname)

        context.launch { eventsRepository.postGlobalEvent(GlobalEvent.Register) }
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
