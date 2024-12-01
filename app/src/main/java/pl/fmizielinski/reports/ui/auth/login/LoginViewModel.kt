package pl.fmizielinski.reports.ui.auth.login

import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.domain.auth.usecase.LoginUseCase
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel.Event
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel.State
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel.UiEvent
import pl.fmizielinski.reports.ui.auth.login.LoginViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.destinations.navgraphs.MainNavGraph
import pl.fmizielinski.reports.ui.navigation.toDestinationData

@KoinViewModel
class LoginViewModel(
    dispatcher: CoroutineDispatcher,
    private val loginUseCase: LoginUseCase,
    private val eventsRepository: EventsRepository,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    init {
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.Login>()
                .collect { postEvent(Event.Login) }
        }
    }

    override fun handleEvent(
        state: State,
        event: Event,
    ): State {
        return when (event) {
            is Event.LoginSuccess -> handleLoginSuccess(state)
            is Event.LoginFailed -> handleLoginFailed(state, event)
            is Event.Login -> handleLogin(state)
            is UiEvent.EmailChanged -> handleEmailChanged(state, event)
            is UiEvent.PasswordChanged -> handlePasswordChanged(state, event)
            is UiEvent.ShowPasswordClicked -> handleShowPasswordClicked(state)
        }
    }

    override fun mapState(state: State): UiState {
        return UiState(
            showPassword = state.showPassword,
            isLoading = state.loginInProgress,
        )
    }

    // region handle Event

    private fun handleLoginSuccess(state: State): State {
        scope.launch {
            eventsRepository.postNavEvent(MainNavGraph.startDestination.toDestinationData())
            eventsRepository.postGlobalEvent(GlobalEvent.Loading(isLoading = false))
        }
        return state.copy(loginInProgress = false)
    }

    private fun handleLoginFailed(
        state: State,
        event: Event.LoginFailed,
    ): State {
        scope.launch {
            eventsRepository.postSnackBarEvent(event.error.toSnackBarData())
            eventsRepository.postGlobalEvent(GlobalEvent.Loading(isLoading = false))
        }
        return state.copy(loginInProgress = false)
    }

    private fun handleLogin(state: State): State {
        scope.launch {
            eventsRepository.postGlobalEvent(GlobalEvent.Loading(isLoading = true))
            try {
                loginUseCase(state.email, state.password)
                postEvent(Event.LoginSuccess)
            } catch (error: SimpleErrorException) {
                logError(error)
                postEvent(Event.LoginFailed(error))
            }
        }
        return state.copy(loginInProgress = true)
    }

    // endregion handle Event

    // region handle UiEvent

    private fun handleEmailChanged(
        state: State,
        event: UiEvent.EmailChanged,
    ): State {
        return state.copy(email = event.email)
    }

    private fun handlePasswordChanged(
        state: State,
        event: UiEvent.PasswordChanged,
    ): State {
        return state.copy(password = event.password)
    }

    private fun handleShowPasswordClicked(state: State): State {
        return state.copy(showPassword = !state.showPassword)
    }

    // endregion handle UiEvent

    data class State(
        val email: String = "",
        val password: String = "",
        val showPassword: Boolean = false,
        val loginInProgress: Boolean = false,
    )

    data class UiState(
        val showPassword: Boolean,
        val isLoading: Boolean,
    )

    sealed interface Event {
        data object LoginSuccess : Event
        data class LoginFailed(val error: SimpleErrorException) : Event
        data object Login : Event
    }

    sealed interface UiEvent : Event {
        data class EmailChanged(val email: String) : UiEvent
        data class PasswordChanged(val password: String) : UiEvent
        data object ShowPasswordClicked : UiEvent
    }
}
