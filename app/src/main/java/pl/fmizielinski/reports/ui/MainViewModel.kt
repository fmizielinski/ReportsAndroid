package pl.fmizielinski.reports.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.domain.usecase.auth.IsLoggedInUseCase
import pl.fmizielinski.reports.domain.usecase.auth.LogoutUseCase
import pl.fmizielinski.reports.ui.MainViewModel.Event
import pl.fmizielinski.reports.ui.MainViewModel.State
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.common.model.AlertDialogUiState
import pl.fmizielinski.reports.ui.common.model.ReportsTopAppBarUiState
import pl.fmizielinski.reports.ui.common.model.TopBarAction
import pl.fmizielinski.reports.ui.common.model.TopBarAction.FILES
import pl.fmizielinski.reports.ui.common.model.TopBarAction.LOGOUT
import pl.fmizielinski.reports.ui.common.model.TopBarAction.PHOTO
import pl.fmizielinski.reports.ui.common.model.TopBarAction.REGISTER
import pl.fmizielinski.reports.ui.destinations.destinations.CreateReportDestination
import pl.fmizielinski.reports.ui.destinations.destinations.LoginDestination
import pl.fmizielinski.reports.ui.destinations.destinations.RegisterDestination
import pl.fmizielinski.reports.ui.destinations.destinations.ReportsDestination
import pl.fmizielinski.reports.ui.destinations.navgraphs.AuthNavGraph
import pl.fmizielinski.reports.ui.destinations.navgraphs.MainNavGraph
import pl.fmizielinski.reports.ui.destinations.navgraphs.ReportsNavGraph
import pl.fmizielinski.reports.ui.navigation.DestinationData
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import timber.log.Timber
import java.io.File
import java.util.Optional
import java.util.concurrent.TimeUnit

@KoinViewModel
class MainViewModel(
    dispatcher: CoroutineDispatcher,
    private val eventsRepository: EventsRepository,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val logoutUseCase: LogoutUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _showSnackBar = MutableSharedFlow<SnackBarData>()
    val showSnackBar: SharedFlow<SnackBarData> = _showSnackBar

    private val _navigationEvents = MutableSharedFlow<Optional<DestinationData>>()
    val navigationEvents: SharedFlow<Optional<DestinationData>> = _navigationEvents

    private val _takePicture = MutableSharedFlow<Unit>()
    val takePicture: SharedFlow<Unit> = _takePicture

    private val _openSettings = MutableSharedFlow<Unit>()
    val openSettings: SharedFlow<Unit> = _openSettings

    private val _pickFile = MutableSharedFlow<Unit>()
    val pickFile: SharedFlow<Unit> = _pickFile

    init {
        scope.launch {
            eventsRepository.navigationEvent.collect(::postNavigationEvent)
        }
        scope.launch {
            eventsRepository.showSnackBar.collect(::postSnackBarEvent)
        }
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.Logout>()
                .collect { postEvent(Event.Logout(withMessage = true)) }
        }
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.ChangeFabVisibility>()
                .collect {
                    postEvent(Event.ChangeFabVisibility(isVisible = it.isVisible))
                }
        }
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.Loading>()
                .collect {
                    postEvent(Event.ChangeLoadingState(isLoading = it.isLoading))
                }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoggedInStateChecked -> handleLoggedInStateChecked(state, event)
            is Event.CheckIfLoggedIn -> handleCheckIfLoggedIn(state)
            is Event.Logout -> handleLogout(state, event)
            is Event.LogoutSuccess -> handleLogoutSuccess(state, event)
            is Event.ChangeFabVisibility -> handleChangeFabVisibility(state, event)
            is Event.ChangeLoadingState -> handleChangeLoadingState(state, event)
            is Event.OpenSettings -> handleOpenSettings(state)
            is UiEvent.BackClicked -> handleBackClicked(state)
            is UiEvent.ActionClicked -> handleActionClicked(state, event)
            is UiEvent.NavDestinationChanged -> handleNavDestinationChanged(state, event)
            is UiEvent.FabClicked -> handleFabClicked(state)
            is UiEvent.PictureTaken -> handlePictureTaken(state, event)
            is UiEvent.TakePictureFailed -> handleTakePictureFailed(state)
            is UiEvent.ShowPermissionRationale -> handleShowPermissionRationale(state, event)
            is UiEvent.AlertDialogDismissed -> handleAlertDialogDismissed(state)
            is UiEvent.AlertDialogPositiveClicked -> handleAlertDialogPositiveClicked(state)
            is UiEvent.FilePicked -> handleFilePicked(state, event)
            is UiEvent.PickFileFailed -> handlePickFileFailed(state)
        }
    }

    override fun mapState(state: State): UiState {
        val fabConfig = getFabConfig(state.currentDestination)
        return UiState(
            appBarUiState = getAppBarUiState(state.currentDestination, state.isLoading),
            fabConfig = fabConfig.takeIf { !state.isLoading && state.isFabVisible },
            alertDialogUiState = getAlertDialogUiState(state.alert),
        )
    }

    private fun getAppBarUiState(currentDestination: String?, isLoading: Boolean) =
        when (currentDestination) {
            LoginDestination.baseRoute -> ReportsTopAppBarUiState(
                destination = currentDestination,
                actions = listOf(REGISTER),
                isEnabled = !isLoading,
            )

            RegisterDestination.baseRoute -> ReportsTopAppBarUiState(
                title = R.string.registerScreen_title,
                destination = currentDestination,
                isEnabled = !isLoading,
            )

            CreateReportDestination.baseRoute -> ReportsTopAppBarUiState(
                title = R.string.createReportScreen_title,
                destination = currentDestination,
                actions = arrayListOf(
                    FILES,
                    PHOTO,
                ),
                isEnabled = !isLoading,
            )

            ReportsDestination.baseRoute -> ReportsTopAppBarUiState(
                title = R.string.reportsScreen_title,
                destination = currentDestination,
                actions = arrayListOf(LOGOUT),
                isEnabled = !isLoading,
            )

            else -> ReportsTopAppBarUiState(isEnabled = !isLoading)
        }

    private fun getFabConfig(currentDestination: String?): UiState.FabConfig? {
        return when (currentDestination) {
            CreateReportDestination.baseRoute -> UiState.FabConfig(
                icon = R.drawable.ic_save_24dp,
                contentDescription = R.string.common_button_saveReport,
            )

            ReportsDestination.baseRoute -> UiState.FabConfig(
                icon = R.drawable.ic_add_24dp,
                contentDescription = R.string.common_button_createReport,
            )

            LoginDestination.baseRoute -> UiState.FabConfig(
                icon = R.drawable.ic_login_24dp,
                contentDescription = R.string.common_button_login,
            )

            RegisterDestination.baseRoute -> UiState.FabConfig(
                icon = R.drawable.ic_person_add_24dp,
                contentDescription = R.string.common_button_register,
            )

            else -> null
        }
    }

    private fun getAlertDialogUiState(
        alert: State.Alert?,
    ): AlertDialogUiState? {
        return alert?.let {
            AlertDialogUiState(
                iconResId = it.icon.resId,
                titleResId = it.titleResId,
                messageResId = it.messageResId,
                positiveButtonResId = it.positiveButton.titleResId,
                negativeButtonResId = it.negativeButton.titleResId,
            )
        }
    }

    // region handle Event

    private fun handleLoggedInStateChecked(
        state: State,
        event: Event.LoggedInStateChecked,
    ): State {
        scope.launch {
            if (event.isLoggedIn) {
                postNavigationEvent(MainNavGraph.startDestination.toDestinationData())
            } else {
                setInitialLoadingFinished()
            }
        }
        return state.copy(isInitialized = true)
    }

    private fun handleCheckIfLoggedIn(state: State): State {
        scope.launch {
            val isLoggedIn = isLoggedInUseCase()
            postEvent(Event.LoggedInStateChecked(isLoggedIn))
        }
        return state
    }

    private fun handleLogout(state: State, event: Event.Logout): State {
        scope.launch {
            logoutUseCase()
            postEvent(Event.LogoutSuccess(withMessage = event.withMessage))
        }
        return state
    }

    private fun handleLogoutSuccess(state: State, event: Event.LogoutSuccess): State {
        scope.launch {
            postNavigationEvent(AuthNavGraph.startDestination.toDestinationData())

            if (event.withMessage) {
                val snackBarData = SnackBarData(
                    messageResId = R.string.common_error_unauthorized,
                )
                postSnackBarEvent(snackBarData)
            }
        }
        return state
    }

    private fun handleChangeFabVisibility(state: State, event: Event.ChangeFabVisibility): State {
        return state.copy(isFabVisible = event.isVisible)
    }

    private fun handleChangeLoadingState(state: State, event: Event.ChangeLoadingState): State {
        return state.copy(isLoading = event.isLoading)
    }

    private fun handleOpenSettings(state: State): State {
        scope.launch {
            _openSettings.emit(Unit)
        }
        return state
    }

    // endregion handle Event

    // region handle UiEvent

    private fun handleBackClicked(state: State): State {
        scope.launch {
            postNavigationUpEvent()
        }
        return state
    }

    private fun handleActionClicked(state: State, event: UiEvent.ActionClicked): State {
        scope.launch {
            when (event.action) {
                REGISTER -> postNavigationEvent(RegisterDestination.toDestinationData())
                PHOTO -> _takePicture.emit(Unit)
                FILES -> _pickFile.emit(Unit)
                LOGOUT -> postEvent(Event.Logout(withMessage = false))
            }
        }
        return state
    }

    private fun handleNavDestinationChanged(
        state: State,
        event: UiEvent.NavDestinationChanged,
    ): State {
        if (!validateNavDestination(event.route)) {
            error("Unknown destination - ${event.route}")
        }
        // When !state.isInitialized
        // this event is the first navigation destination after displaying the nav host
        scope.launch {
            if (!state.isInitialized) {
                postEvent(Event.CheckIfLoggedIn)
            } else {
                setInitialLoadingFinished()
            }
        }
        return state.copy(currentDestination = event.route, isFabVisible = true)
    }

    private fun handleFabClicked(state: State): State {
        scope.launch {
            when (state.currentDestination) {
                CreateReportDestination.baseRoute -> {
                    eventsRepository.postGlobalEvent(GlobalEvent.SaveReport)
                }

                ReportsDestination.baseRoute -> {
                    postNavigationEvent(CreateReportDestination.toDestinationData())
                }

                LoginDestination.baseRoute -> {
                    eventsRepository.postGlobalEvent(GlobalEvent.Login)
                }

                RegisterDestination.baseRoute -> {
                    eventsRepository.postGlobalEvent(GlobalEvent.Register)
                }
            }
        }
        return state
    }

    private fun handlePictureTaken(state: State, event: UiEvent.PictureTaken): State {
        scope.launch {
            eventsRepository.postGlobalEvent(GlobalEvent.AddAttachment(event.file))
        }
        return state
    }

    private fun handleTakePictureFailed(state: State): State {
        scope.launch {
            Timber.e("Take picture failed")
            val snackBarData = SnackBarData(messageResId = R.string.common_error_oops)
            postSnackBarEvent(snackBarData)
        }
        return state
    }

    private fun handleShowPermissionRationale(
        state: State,
        event: UiEvent.ShowPermissionRationale,
    ): State {
        val message = when (event.action) {
            PHOTO -> R.string.common_label_cameraPermissionRationale
            FILES -> R.string.common_label_imagesPermissionRationale
            else -> null
        } ?: return state

        val alert = State.Alert(
            icon = State.Alert.IconType.INFO,
            titleResId = R.string.common_label_permission,
            messageResId = message,
            positiveButton = State.Alert.ButtonType.SETTINGS,
            negativeButton = State.Alert.ButtonType.CANCEL,
            pendingEvent = Event.OpenSettings,
        )
        return state.copy(alert = alert)
    }

    private fun handleAlertDialogDismissed(state: State): State {
        return state.copy(alert = null)
    }

    private fun handleAlertDialogPositiveClicked(state: State): State {
        scope.launch {
            if (state.alert != null) {
                postEvent(state.alert.pendingEvent)
            }
        }
        return state.copy(alert = null)
    }

    private fun handleFilePicked(state: State, event: UiEvent.FilePicked): State {
        scope.launch {
            eventsRepository.postGlobalEvent(GlobalEvent.AddAttachment(event.file))
        }
        return state
    }

    private fun handlePickFileFailed(state: State): State {
        scope.launch {
            Timber.e("Pick file failed")
            val snackBarData = SnackBarData(messageResId = R.string.common_error_oops)
            postSnackBarEvent(snackBarData)
        }
        return state
    }

    // endregion handle UiEvent

    private suspend fun postNavigationUpEvent() {
        postNavigationEvent(Optional.empty())
    }

    private suspend fun postNavigationEvent(destination: DestinationData) {
        postNavigationEvent(Optional.of(destination))
    }

    private suspend fun postNavigationEvent(destination: Optional<DestinationData>) {
        _navigationEvents.emit(destination)
    }

    private suspend fun postSnackBarEvent(snackBarData: SnackBarData) {
        _showSnackBar.emit(snackBarData)
        delay(TimeUnit.SECONDS.toMillis(snackBarData.secondsAlive))
        _showSnackBar.emit(SnackBarData.empty())
    }

    private fun validateNavDestination(route: String): Boolean {
        return ReportsNavGraph.nestedNavGraphs.any { graph ->
            graph.destinations.any { it.baseRoute == route }
        }
    }

    private suspend fun setInitialLoadingFinished() {
        // Delay needed to prevent blinking initial navigation after splash screen
        delay(POST_INITIALIZATION_DELAY)
        _isInitialLoading.value = false
    }

    data class State(
        val currentDestination: String? = null,
        val isInitialized: Boolean = false,
        val isFabVisible: Boolean = true,
        val isLoading: Boolean = false,
        val alert: Alert? = null,
    ) {

        data class Alert(
            val icon: IconType,
            @StringRes val titleResId: Int,
            @StringRes val messageResId: Int,
            val positiveButton: ButtonType,
            val negativeButton: ButtonType,
            val pendingEvent: Event,
        ) {

            enum class IconType(@DrawableRes val resId: Int) {
                INFO(R.drawable.ic_info_24dp),
                QUESTION(R.drawable.ic_info_24dp),
            }

            enum class ButtonType(@StringRes val titleResId: Int) {
                SETTINGS(R.string.common_label_settings),
                CANCEL(R.string.common_label_cancel),
                YES(R.string.common_label_yes),
                NO(R.string.common_label_no),
            }
        }
    }

    data class UiState(
        val appBarUiState: ReportsTopAppBarUiState,
        val fabConfig: FabConfig?,
        val alertDialogUiState: AlertDialogUiState?,
    ) {

        data class FabConfig(
            @DrawableRes val icon: Int,
            @StringRes val contentDescription: Int,
        )
    }

    sealed interface Event {
        data class LoggedInStateChecked(val isLoggedIn: Boolean) : Event
        data object CheckIfLoggedIn : Event
        data class Logout(val withMessage: Boolean) : Event
        data class LogoutSuccess(val withMessage: Boolean) : Event
        data class ChangeFabVisibility(val isVisible: Boolean) : Event
        data class ChangeLoadingState(val isLoading: Boolean) : Event
        data object OpenSettings : Event
    }

    sealed interface UiEvent : Event {
        data object BackClicked : UiEvent
        data class ActionClicked(val action: TopBarAction) : UiEvent
        data class NavDestinationChanged(val route: String) : UiEvent
        data object FabClicked : UiEvent
        data class PictureTaken(val file: File) : UiEvent
        data object TakePictureFailed : UiEvent
        data class ShowPermissionRationale(val action: TopBarAction) : UiEvent
        data object AlertDialogDismissed : UiEvent
        data object AlertDialogPositiveClicked : UiEvent
        data class FilePicked(val file: File) : UiEvent
        data object PickFileFailed : UiEvent
    }

    companion object {
        private const val POST_INITIALIZATION_DELAY = 500L
    }
}

suspend inline fun <TYPE : Optional<SUBTYPE>, reified SUBTYPE> Flow<TYPE>.collectDestination(
    collector: FlowCollector<SUBTYPE>,
) {
    collect { value -> collector.emit(value.orElse(null)) }
}
