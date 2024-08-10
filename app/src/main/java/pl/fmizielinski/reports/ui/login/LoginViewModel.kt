package pl.fmizielinski.reports.ui.login

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.LoginUseCase
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
            is UiEvent.EmailChanged -> handleEmailChanged(state, event)
            is UiEvent.PasswordChanged -> handlePasswordChanged(state, event)
            is UiEvent.LoginClicked -> handleLoginClicked(state)
        }
    }

    override fun mapState(state: State): UiState = UiState(
        email = state.email,
        password = state.password,
    )

    // region handleEvent

    private fun handleLoginSuccess(state: State): State {
        return state
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
            } catch (e: RuntimeException) {
                Timber.e(e)
                val snackBarData = SnackBarData(R.string.loginScreen_error_login)
                eventsRepository.postSnackBarEvent(snackBarData)
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
    )

    sealed interface Event {
        data object LoginSuccess : Event
    }

    sealed interface UiEvent : Event {
        data class EmailChanged(val email: String) : UiEvent
        data class PasswordChanged(val password: String) : UiEvent
        data object LoginClicked : UiEvent
    }
}
