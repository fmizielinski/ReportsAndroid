package pl.fmizielinski.reports.ui.register

import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.register.RegisterViewModel.Event
import pl.fmizielinski.reports.ui.register.RegisterViewModel.State
import pl.fmizielinski.reports.ui.register.RegisterViewModel.UiEvent
import pl.fmizielinski.reports.ui.register.RegisterViewModel.UiState

@KoinViewModel
class RegisterViewModel(
    dispatcher: CoroutineDispatcher,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    override fun handleEvent(state: State, event: Event): State = state

    override fun mapState(state: State): UiState {
        val passwordsMatch = state.password == state.passwordConfirmation
        val allDataFilled = state.email.isNotBlank() &&
                state.password.isNotBlank() &&
                state.passwordConfirmation.isNotBlank() &&
                state.name.isNotBlank() &&
                state.surname.isNotBlank()
        val isRegisterButtonEnabled = allDataFilled &&
                !state.registerInProgress &&
                passwordsMatch
        return UiState(
            loginData = UiState.LoginData(
                email = state.email,
                password = state.password,
                passwordConfirmation = state.passwordConfirmation,
                showPassword = state.showPassword,
            ),
            userData = UiState.UserData(
                name = state.name,
                surname = state.surname,
            ),
            isRegisterButtonEnabled = isRegisterButtonEnabled,
        )
    }

    data class State(
        val email: String = "",
        val password: String = "",
        val passwordConfirmation: String = "",
        val name: String = "",
        val surname: String = "",
        val showPassword: Boolean = false,
        val registerInProgress: Boolean = false,
    )

    data class UiState(
        val loginData: LoginData,
        val userData: UserData,
        val isRegisterButtonEnabled: Boolean,
    ) {
        data class LoginData(
            val email: String,
            val password: String,
            val passwordConfirmation: String,
            val showPassword: Boolean,
        )

        data class UserData(
            val name: String,
            val surname: String,
        )
    }

    sealed interface Event

    sealed interface UiEvent : Event
}