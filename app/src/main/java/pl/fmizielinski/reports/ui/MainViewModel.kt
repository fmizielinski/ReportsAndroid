package pl.fmizielinski.reports.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ramcosta.composedestinations.generated.destinations.CreateReportDestination
import com.ramcosta.composedestinations.generated.destinations.LoginDestination
import com.ramcosta.composedestinations.generated.destinations.RegisterDestination
import com.ramcosta.composedestinations.generated.destinations.ReportsDestination
import com.ramcosta.composedestinations.generated.navgraphs.AuthNavGraph
import com.ramcosta.composedestinations.generated.navgraphs.MainNavGraph
import com.ramcosta.composedestinations.generated.navgraphs.ReportsNavGraph
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
import pl.fmizielinski.reports.domain.usecase.auth.IsLoggedInUseCase
import pl.fmizielinski.reports.domain.usecase.auth.LogoutUseCase
import pl.fmizielinski.reports.ui.MainViewModel.Event
import pl.fmizielinski.reports.ui.MainViewModel.State
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.model.TopBarAction
import pl.fmizielinski.reports.ui.model.TopBarAction.PHOTO
import pl.fmizielinski.reports.ui.model.TopBarAction.REGISTER
import pl.fmizielinski.reports.ui.navigation.DestinationData
import pl.fmizielinski.reports.ui.navigation.toDestinationData
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

    init {
        scope.launch {
            eventsRepository.navigationEvent.collect(::postNavigationEvent)
        }
        scope.launch {
            eventsRepository.showSnackBar.collect(::postSnackBarEvent)
        }
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<EventsRepository.GlobalEvent.Logout>()
                .collect { postLogoutEvent() }
        }
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<EventsRepository.GlobalEvent.SaveReportFailed>()
                .collect {
                    postEvent(Event.ChangeFabVisibility(isHidden = false))
                }
        }
    }

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoggedInStateChecked -> handleLoggedInStateChecked(state, event)
            is Event.CheckIfLoggedIn -> handleCheckIfLoggedIn(state)
            is Event.Logout -> handleLogout(state)
            is Event.LogoutSuccess -> handleLogoutSuccess(state)
            is Event.ChangeFabVisibility -> handleChangeFabVisibility(state, event)
            is UiEvent.BackClicked -> handleBackClicked(state)
            is UiEvent.ActionClicked -> handleActionClicked(state, event)
            is UiEvent.NavDestinationChanged -> handleNavDestinationChanged(state, event)
            is UiEvent.FabClicked -> handleFabClicked(state)
        }
    }

    override fun mapState(state: State): UiState {
        val actions = when (state.currentDestination) {
            LoginDestination.baseRoute -> listOf(REGISTER)
            CreateReportDestination.baseRoute -> listOf(PHOTO)
            else -> emptyList()
        }
        val isBackVisible = ReportsNavGraph.nestedNavGraphs.none { graph ->
            graph.startDestination.baseRoute == state.currentDestination
        }
        return UiState(
            actions = actions,
            isBackVisible = isBackVisible,
            title = getTitle(state.currentDestination),
            fabConfig = getFabConfig(state.currentDestination).takeUnless { state.isFabHidden },
        )
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

    private fun handleLogout(state: State): State {
        scope.launch {
            logoutUseCase()
            postEvent(Event.LogoutSuccess)
        }
        return state
    }

    private fun handleLogoutSuccess(state: State): State {
        scope.launch {
            postNavigationEvent(AuthNavGraph.startDestination.toDestinationData())

            val snackBarData = SnackBarData(
                messageResId = R.string.common_error_unauthorized,
            )
            postSnackBarEvent(snackBarData)
        }
        return state
    }

    private fun handleChangeFabVisibility(state: State, event: Event.ChangeFabVisibility): State {
        return state.copy(isFabHidden = event.isHidden)
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
                PHOTO -> Unit // FIXME
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
        return state.copy(currentDestination = event.route, isFabHidden = false)
    }

    private fun handleFabClicked(state: State): State {
        scope.launch {
            when (state.currentDestination) {
                CreateReportDestination.baseRoute -> {
                    postEvent(Event.ChangeFabVisibility(isHidden = true))
                    eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReport)
                }

                ReportsDestination.baseRoute -> {
                    postNavigationEvent(CreateReportDestination.toDestinationData())
                }
            }
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

    private suspend fun postLogoutEvent() {
        postEvent(Event.Logout)
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

    private fun getTitle(currentDestination: String?) = when (currentDestination) {
        CreateReportDestination.baseRoute -> R.string.createReportScreen_title
        RegisterDestination.baseRoute -> R.string.registerScreen_title
        ReportsDestination.baseRoute -> R.string.reportsScreen_title
        else -> null
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

            else -> null
        }
    }

    data class State(
        val currentDestination: String? = null,
        val isInitialized: Boolean = false,
        val isFabHidden: Boolean = false,
    )

    data class UiState(
        val actions: List<TopBarAction>,
        val isBackVisible: Boolean,
        @StringRes val title: Int?,
        val fabConfig: FabConfig?,
    ) {

        data class FabConfig(
            @DrawableRes val icon: Int,
            @StringRes val contentDescription: Int,
        )
    }

    sealed interface Event {
        data class LoggedInStateChecked(val isLoggedIn: Boolean) : Event
        data object CheckIfLoggedIn : Event
        data object Logout : Event
        data object LogoutSuccess : Event
        data class ChangeFabVisibility(val isHidden: Boolean) : Event
    }

    sealed interface UiEvent : Event {
        data object BackClicked : UiEvent
        data class ActionClicked(val action: TopBarAction) : UiEvent
        data class NavDestinationChanged(val route: String) : UiEvent
        data object FabClicked : UiEvent
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
