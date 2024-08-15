package pl.fmizielinski.reports.ui.register

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.model.RegistrationData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.RegisterUseCase
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.register.RegisterViewModel.Event
import pl.fmizielinski.reports.ui.register.RegisterViewModel.State
import pl.fmizielinski.reports.ui.register.RegisterViewModel.UiEvent
import pl.fmizielinski.reports.ui.register.RegisterViewModel.UiState
import timber.log.Timber

@KoinViewModel
class RegisterViewModel(
    dispatcher: CoroutineDispatcher,
    private val registerUseCase: RegisterUseCase,
    private val eventsRepository: EventsRepository,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.Verify -> handleVerify(state)
            is Event.RegisterSuccess -> handleRegisterSuccess(state)
            is Event.RegisterFailed -> handleRegisterFailed(state, event)
            is UiEvent.EmailChanged -> handleEmailChanged(state, event)
            is UiEvent.PasswordChanged -> handlePasswordChanged(state, event)
            is UiEvent.PasswordConfirmationChanged -> handlePasswordConfirmationChanged(
                state,
                event,
            )

            is UiEvent.NameChanged -> handleNameChanged(state, event)
            is UiEvent.SurnameChanged -> handleSurnameChanged(state, event)
            is UiEvent.ShowPasswordClicked -> handleShowPasswordClicked(state)
            is UiEvent.RegisterClicked -> handleRegisterClicked(state)
        }
    }

    override fun mapState(state: State): UiState {
        val allDataFilled = state.email.isNotBlank() &&
                state.password.isNotBlank() &&
                state.passwordConfirmation.isNotBlank() &&
                state.name.isNotBlank() &&
                state.surname.isNotBlank()
        val isRegisterButtonEnabled = allDataFilled && !state.registerInProgress
        return UiState(
            loginData = UiState.LoginData(
                email = state.email,
                password = state.password,
                passwordConfirmation = state.passwordConfirmation,
                showPassword = state.showPassword,
                passwordVerificationError = state.passwordVerificationError,
            ),
            userData = UiState.UserData(
                name = state.name,
                surname = state.surname,
            ),
            isRegisterButtonEnabled = isRegisterButtonEnabled,
        )
    }

    // region handleEvent

    private fun handleVerify(state: State): State {
        if (state.password != state.passwordConfirmation) {
            return state.copy(passwordVerificationError = true, registerInProgress = false)
        } else {
            scope.launch {
                try {
                    registerUseCase(
                        RegistrationData(
                            email = state.email,
                            password = state.password,
                            name = state.name,
                            surname = state.surname,
                        )
                    )
                    postEvent(Event.RegisterSuccess)
                } catch (e: ErrorException) {
                    Timber.e(e)
                    postEvent(Event.RegisterFailed(e))
                }
            }
            return state.copy(registerInProgress = true)
        }
    }

    private fun handleRegisterSuccess(state: State): State {
        return state.copy(registerInProgress = false)
    }

    private fun handleRegisterFailed(state: State, event: Event.RegisterFailed): State {
        scope.launch {
            eventsRepository.postSnackBarEvent(event.error.toSnackBarData())
        }
        return state.copy(registerInProgress = false)
    }

    private fun handleEmailChanged(state: State, event: UiEvent.EmailChanged): State {
        return state.copy(email = event.email)
    }

    private fun handlePasswordChanged(state: State, event: UiEvent.PasswordChanged): State {
        return state.copy(
            password = event.password,
            passwordVerificationError = false,
        )
    }

    private fun handlePasswordConfirmationChanged(
        state: State,
        event: UiEvent.PasswordConfirmationChanged,
    ): State {
        return state.copy(
            passwordConfirmation = event.passwordConfirmation,
            passwordVerificationError = false,
        )
    }

    private fun handleNameChanged(state: State, event: UiEvent.NameChanged): State {
        return state.copy(name = event.name)
    }

    private fun handleSurnameChanged(state: State, event: UiEvent.SurnameChanged): State {
        return state.copy(surname = event.surname)
    }

    private fun handleShowPasswordClicked(state: State): State {
        return state.copy(showPassword = !state.showPassword)
    }

    private fun handleRegisterClicked(state: State): State {
        scope.launch { postEvent(Event.Verify) }
        return state.copy(registerInProgress = true)
    }

    // endregion

    data class State(
        val email: String = "",
        val password: String = "",
        val passwordConfirmation: String = "",
        val name: String = "",
        val surname: String = "",
        val showPassword: Boolean = false,
        val registerInProgress: Boolean = false,
        val passwordVerificationError: Boolean = false,
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
            val passwordVerificationError: Boolean,
        )

        data class UserData(
            val name: String,
            val surname: String,
        )
    }

    sealed interface Event {
        data object Verify : Event
        data object RegisterSuccess : Event
        data class RegisterFailed(val error: ErrorException) : Event
    }

    sealed interface UiEvent : Event {
        data class EmailChanged(val email: String) : UiEvent
        data class PasswordChanged(val password: String) : UiEvent
        data class PasswordConfirmationChanged(val passwordConfirmation: String) : UiEvent
        data class NameChanged(val name: String) : UiEvent
        data class SurnameChanged(val surname: String) : UiEvent
        data object ShowPasswordClicked : UiEvent
        data object RegisterClicked : UiEvent
    }
}