package pl.fmizielinski.reports.ui.login

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.LoginUseCase
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.login.LoginViewModel.Event
import pl.fmizielinski.reports.ui.login.LoginViewModel.State
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiEvent
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiState
import timber.log.Timber

@KoinViewModel
class LoginViewModel(
    dispatcher: CoroutineDispatcher,
    private val loginUseCase: LoginUseCase,
    private val eventsRepository: EventsRepository,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoginSuccess -> handleLoginSuccess(state)
            is Event.LoginFailed -> handleLoginFailed(state, event)
            is UiEvent.EmailChanged -> handleEmailChanged(state, event)
            is UiEvent.PasswordChanged -> handlePasswordChanged(state, event)
            is UiEvent.LoginClicked -> handleLoginClicked(state)
        }
    }

    override fun mapState(state: State): UiState = UiState(
        email = state.email,
        password = state.password,
        isLoginButtonEnabled = state.email.isNotBlank() && state.password.isNotBlank(),
    )

    // region handleEvent

    private fun handleLoginSuccess(state: State): State {
        return state
    }

    private fun handleLoginFailed(state: State, event: Event.LoginFailed): State {
        scope.launch {
            eventsRepository.postSnackBarEvent(event.error.toSnackBarData())
        }
        return state.copy(password = "")
    }

    private fun handleEmailChanged(state: State, event: UiEvent.EmailChanged): State {
        return state.copy(email = event.email)
    }

    private fun handlePasswordChanged(state: State, event: UiEvent.PasswordChanged): State {
        return state.copy(password = event.password)
    }

    private fun handleLoginClicked(state: State): State {
        scope.launch {
            try {
                loginUseCase(state.email, state.password)
                postEvent(Event.LoginSuccess)
            } catch (error: ErrorException) {
                Timber.e(error)
                postEvent(Event.LoginFailed(error))
            }
        }
        return state
    }

    // endregion handleEvent

    data class State(
        val email: String = "",
        val password: String = "",
    )

    data class UiState(
        val email: String,
        val password: String,
        val isLoginButtonEnabled: Boolean,
    )

    sealed interface Event {
        data object LoginSuccess : Event
        data class LoginFailed(val error: ErrorException) : Event
    }

    sealed interface UiEvent : Event {
        data class EmailChanged(val email: String) : UiEvent
        data class PasswordChanged(val password: String) : UiEvent
        data object LoginClicked : UiEvent
    }
}
