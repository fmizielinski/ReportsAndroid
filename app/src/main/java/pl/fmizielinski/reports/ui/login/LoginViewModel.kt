package pl.fmizielinski.reports.ui.login

import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.login.LoginViewModel.Event
import pl.fmizielinski.reports.ui.login.LoginViewModel.State
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiEvent
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiState

@KoinViewModel
class LoginViewModel(
    dispatcher: CoroutineDispatcher,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    override fun handleEvent(state: State, event: Event): State {
        TODO("Not yet implemented")
    }

    override fun mapState(state: State): UiState = UiState(
        email = state.email,
        password = state.password,
    )

    data class State(
        val email: String = "",
        val password: String = "",
    )

    data class UiState(
        val email: String,
        val password: String,
    )

    sealed interface Event

    sealed interface UiEvent : Event
}
