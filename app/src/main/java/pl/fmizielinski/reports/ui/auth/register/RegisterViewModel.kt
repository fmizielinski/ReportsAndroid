package pl.fmizielinski.reports.ui.auth.register

import androidx.annotation.StringRes
import com.ramcosta.composedestinations.generated.navgraphs.MainNavGraph
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.domain.error.CompositeErrorException
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.EMAIL_NOT_VALID
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.NAME_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.PASSWORD_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Auth.Register.SURNAME_EMPTY
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.model.RegistrationData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.RegisterUseCase
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.Event
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.State
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.State.VerificationError.Email
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.State.VerificationError.Name
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.State.VerificationError.Password
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.State.VerificationError.Surname
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.UiEvent
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.UiState

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
            is Event.PostVerificationError -> handlePostVerificationError(state, event)
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
        val emailVerificationError = state.verificationErrors.firstOrNull { it is Email }
            ?.let { UiState.VerificationError(it.messageResId) }
        val passwordVerificationError = state.verificationErrors.firstOrNull { it is Password }
            ?.let { UiState.VerificationError(it.messageResId) }
        val nameVerificationError = state.verificationErrors.firstOrNull { it is Name }
            ?.let { UiState.VerificationError(it.messageResId) }
        val surnameVerificationError = state.verificationErrors.firstOrNull { it is Surname }
            ?.let { UiState.VerificationError(it.messageResId) }
        return UiState(
            loginData = UiState.LoginData(
                showPassword = state.showPassword,
                emailVerificationError = emailVerificationError,
                passwordVerificationError = passwordVerificationError,
            ),
            userData = UiState.UserData(
                nameVerificationError = nameVerificationError,
                surnameVerificationError = surnameVerificationError,
            ),
            isRegisterButtonEnabled = isRegisterButtonEnabled,
        )
    }

    // region handle Event

    private fun handleVerify(state: State): State {
        if (state.password != state.passwordConfirmation) {
            return state.copy(
                verificationErrors = listOf(Password(R.string.registerScreen_error_password)),
                registerInProgress = false,
            )
        } else {
            scope.launch {
                try {
                    registerUseCase(
                        RegistrationData(
                            email = state.email,
                            password = state.password,
                            name = state.name,
                            surname = state.surname,
                        ),
                    )
                    postEvent(Event.RegisterSuccess)
                } catch (error: ErrorException) {
                    logError(error)
                    postEvent(Event.RegisterFailed(error))
                }
            }
            return state.copy(registerInProgress = true)
        }
    }

    private fun handleRegisterSuccess(state: State): State {
        scope.launch {
            eventsRepository.postNavEvent(MainNavGraph.startDestination.toDestinationData())
        }
        return state.copy(registerInProgress = false)
    }

    private fun handleRegisterFailed(state: State, event: Event.RegisterFailed): State {
        scope.launch {
            if (event.error is SimpleErrorException) {
                if (event.error.isVerificationError) {
                    val verificationError = parseVerificationError(event.error)
                    postEvent(Event.PostVerificationError(listOf(verificationError)))
                } else {
                    eventsRepository.postSnackBarEvent(event.error.toSnackBarData())
                }
            } else if (event.error is CompositeErrorException) {
                val verificationErrors = event.error.exceptions
                    .filter { it.isVerificationError }
                    .map(::parseVerificationError)
                postEvent(Event.PostVerificationError(verificationErrors))
                event.error.exceptions
                    .filter { !it.isVerificationError }
                    .forEach { eventsRepository.postSnackBarEvent(it.toSnackBarData()) }
            }
        }
        return state.copy(registerInProgress = false)
    }

    private fun handlePostVerificationError(
        state: State,
        event: Event.PostVerificationError,
    ): State {
        return state.copy(verificationErrors = event.errors)
    }

    // endregion handle Event

    // region handle UiEvent

    private fun handleEmailChanged(state: State, event: UiEvent.EmailChanged): State {
        return state.copy(
            email = event.email,
            verificationErrors = state.filterVerificationErrors<Email>(),
        )
    }

    private fun handlePasswordChanged(state: State, event: UiEvent.PasswordChanged): State {
        return state.copy(
            password = event.password,
            verificationErrors = state.filterVerificationErrors<Password>(),
        )
    }

    private fun handlePasswordConfirmationChanged(
        state: State,
        event: UiEvent.PasswordConfirmationChanged,
    ): State {
        return state.copy(
            passwordConfirmation = event.passwordConfirmation,
            verificationErrors = state.filterVerificationErrors<Password>(),
        )
    }

    private fun handleNameChanged(state: State, event: UiEvent.NameChanged): State {
        return state.copy(
            name = event.name,
            verificationErrors = state.filterVerificationErrors<Name>(),
        )
    }

    private fun handleSurnameChanged(state: State, event: UiEvent.SurnameChanged): State {
        return state.copy(
            surname = event.surname,
            verificationErrors = state.filterVerificationErrors<Surname>(),
        )
    }

    private fun handleShowPasswordClicked(state: State): State {
        return state.copy(showPassword = !state.showPassword)
    }

    private fun handleRegisterClicked(state: State): State {
        scope.launch { postEvent(Event.Verify) }
        return state.copy(registerInProgress = true)
    }

    // endregion handle UiEvent

    private fun parseVerificationError(error: SimpleErrorException): State.VerificationError {
        return when (error.code) {
            EMAIL_NOT_VALID -> Email(error.uiMessage)
            PASSWORD_EMPTY -> Password(error.uiMessage)
            NAME_EMPTY -> Name(error.uiMessage)
            SURNAME_EMPTY -> Surname(error.uiMessage)
            else -> throw IllegalArgumentException("Unknown verification error")
        }
    }

    private inline fun <reified T : State.VerificationError> State.filterVerificationErrors() =
        verificationErrors.filter { it !is T }

    data class State(
        val email: String = "",
        val password: String = "",
        val passwordConfirmation: String = "",
        val name: String = "",
        val surname: String = "",
        val showPassword: Boolean = false,
        val registerInProgress: Boolean = false,
        val verificationErrors: List<VerificationError> = emptyList(),
    ) {

        sealed class VerificationError(@StringRes val messageResId: Int) {
            data class Email(val message: Int) : VerificationError(message)
            data class Password(val message: Int) : VerificationError(message)
            data class Name(val message: Int) : VerificationError(message)
            data class Surname(val message: Int) : VerificationError(message)
        }
    }

    data class UiState(
        val loginData: LoginData,
        val userData: UserData,
        val isRegisterButtonEnabled: Boolean,
    ) {
        data class LoginData(
            val showPassword: Boolean,
            val emailVerificationError: VerificationError?,
            val passwordVerificationError: VerificationError?,
        )

        data class UserData(
            val nameVerificationError: VerificationError?,
            val surnameVerificationError: VerificationError?,
        )

        data class VerificationError(
            @StringRes val messageResId: Int,
        )
    }

    sealed interface Event {
        data object Verify : Event
        data object RegisterSuccess : Event
        data class RegisterFailed(val error: ErrorException) : Event
        data class PostVerificationError(val errors: List<State.VerificationError>) : Event
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
