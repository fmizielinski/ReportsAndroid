package pl.fmizielinski.reports.ui.auth.register

import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.R
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
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.Event
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.State
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.UiEvent
import pl.fmizielinski.reports.ui.auth.register.RegisterViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.base.ErrorHandler
import pl.fmizielinski.reports.ui.base.ErrorHandler.VerificationError
import pl.fmizielinski.reports.ui.base.filterIsNotInstance
import pl.fmizielinski.reports.ui.base.findVerificationError
import pl.fmizielinski.reports.ui.destinations.navgraphs.MainNavGraph
import pl.fmizielinski.reports.ui.navigation.toDestinationData

@KoinViewModel
class RegisterViewModel(
    dispatcher: CoroutineDispatcher,
    private val registerUseCase: RegisterUseCase,
    private val eventsRepository: EventsRepository,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()), ErrorHandler {

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
        return UiState(
            loginData = getLoginData(state),
            userData = getUserData(state),
            isRegisterButtonEnabled = isRegisterButtonEnabled,
            isLoading = state.registerInProgress,
        )
    }

    private fun getLoginData(state: State): UiState.LoginData {
        val emailVerificationError = state.verificationErrors
            .findVerificationError<Email>()
        val passwordVerificationError = state.verificationErrors
            .findVerificationError<Password>()
        return UiState.LoginData(
            showPassword = state.showPassword,
            emailVerificationError = emailVerificationError,
            passwordVerificationError = passwordVerificationError,
        )
    }

    private fun getUserData(state: State): UiState.UserData {
        val nameVerificationError = state.verificationErrors
            .findVerificationError<Name>()
        val surnameVerificationError = state.verificationErrors
            .findVerificationError<Surname>()
        return UiState.UserData(
            nameVerificationError = nameVerificationError,
            surnameVerificationError = surnameVerificationError,
        )
    }

    // region handle Event

    private fun handleVerify(state: State): State {
        if (state.password != state.passwordConfirmation) {
            return state.copy(
                verificationErrors = listOf(Password(R.string.registerScreen_error_password)),
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
            handleError(event.error)
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
            verificationErrors = state.verificationErrors.filterIsNotInstance<Email>(),
        )
    }

    private fun handlePasswordChanged(state: State, event: UiEvent.PasswordChanged): State {
        return state.copy(
            password = event.password,
            verificationErrors = state.verificationErrors.filterIsNotInstance<Password>(),
        )
    }

    private fun handlePasswordConfirmationChanged(
        state: State,
        event: UiEvent.PasswordConfirmationChanged,
    ): State {
        return state.copy(
            passwordConfirmation = event.passwordConfirmation,
            verificationErrors = state.verificationErrors.filterIsNotInstance<Password>(),
        )
    }

    private fun handleNameChanged(state: State, event: UiEvent.NameChanged): State {
        return state.copy(
            name = event.name,
            verificationErrors = state.verificationErrors.filterIsNotInstance<Name>(),
        )
    }

    private fun handleSurnameChanged(state: State, event: UiEvent.SurnameChanged): State {
        return state.copy(
            surname = event.surname,
            verificationErrors = state.verificationErrors.filterIsNotInstance<Surname>(),
        )
    }

    private fun handleShowPasswordClicked(state: State): State {
        return state.copy(showPassword = !state.showPassword)
    }

    private fun handleRegisterClicked(state: State): State {
        scope.launch { postEvent(Event.Verify) }
        return state
    }

    // endregion handle UiEvent

    // region ErrorHandler

    override fun parseVerificationError(error: SimpleErrorException): VerificationError {
        return when (error.code) {
            EMAIL_NOT_VALID -> Email(error.uiMessage)
            PASSWORD_EMPTY -> Password(error.uiMessage)
            NAME_EMPTY -> Name(error.uiMessage)
            SURNAME_EMPTY -> Surname(error.uiMessage)
            else -> throw IllegalArgumentException("Unknown verification error")
        }
    }

    override suspend fun handleVerificationError(verificationErrors: List<VerificationError>) {
        postEvent(Event.PostVerificationError(verificationErrors))
    }

    override suspend fun handleNonVerificationError(error: SimpleErrorException) {
        eventsRepository.postSnackBarEvent(error.toSnackBarData())
    }

    // endregion ErrorHandler

    data class State(
        val email: String = "",
        val password: String = "",
        val passwordConfirmation: String = "",
        val name: String = "",
        val surname: String = "",
        val showPassword: Boolean = false,
        val registerInProgress: Boolean = false,
        val verificationErrors: List<VerificationError> = emptyList(),
    )

    data class UiState(
        val loginData: LoginData,
        val userData: UserData,
        val isRegisterButtonEnabled: Boolean,
        val isLoading: Boolean,
    ) {
        data class LoginData(
            val showPassword: Boolean,
            val emailVerificationError: Int?,
            val passwordVerificationError: Int?,
        )

        data class UserData(
            val nameVerificationError: Int?,
            val surnameVerificationError: Int?,
        )
    }

    sealed interface Event {
        data object Verify : Event
        data object RegisterSuccess : Event
        data class RegisterFailed(val error: ErrorException) : Event
        data class PostVerificationError(val errors: List<VerificationError>) : Event
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

    data class Email(override val messageResId: Int) : VerificationError
    data class Password(override val messageResId: Int) : VerificationError
    data class Name(override val messageResId: Int) : VerificationError
    data class Surname(override val messageResId: Int) : VerificationError
}
