package pl.fmizielinski.reports.ui

import com.ramcosta.composedestinations.generated.destinations.LoginDestination
import com.ramcosta.composedestinations.generated.destinations.RegisterDestination
import com.ramcosta.composedestinations.generated.navgraphs.AuthNavGraph
import com.ramcosta.composedestinations.generated.navgraphs.MainNavGraph
import com.ramcosta.composedestinations.generated.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.IsLoggedInUseCase
import pl.fmizielinski.reports.ui.MainViewModel.Event
import pl.fmizielinski.reports.ui.MainViewModel.State
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.model.TopBarAction
import java.util.Optional
import java.util.concurrent.TimeUnit

@KoinViewModel
class MainViewModel(
    dispatcher: CoroutineDispatcher,
    private val eventsRepository: EventsRepository,
    private val isLoggedInUseCase: IsLoggedInUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _showSnackBar = MutableSharedFlow<SnackBarData>()
    val showSnackBar: SharedFlow<SnackBarData> = _showSnackBar

    private val _navigationEvents = MutableSharedFlow<Optional<DestinationSpec>>()
    val navigationEvents: SharedFlow<Optional<DestinationSpec>> = _navigationEvents

    init {
        scope.launch {
            eventsRepository.navigationEvent.collect(::postNavigationEvent)
        }
        scope.launch {
            eventsRepository.showSnackBar.collect(::postSnackBarEvent)
        }
    }

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoggedInStateChecked -> handleLoggedInStateChecked(state, event)
            is Event.CheckIfLoggedIn -> handleCheckIfLoggedIn(state)
            is UiEvent.BackClicked -> handleBackClicked(state)
            is UiEvent.RegisterClicked -> handleRegisterClicked(state)
            is UiEvent.NavDestinationChanged -> handleNavDestinationChanged(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        val actions = buildList {
            if (state.currentDestination == LoginDestination.baseRoute) {
                add(TopBarAction.REGISTER)
            }
        }
        val isBackVisible = RootNavGraph.nestedNavGraphs.any { graph ->
            graph.startDestination.baseRoute == state.currentDestination
        }
        return UiState(
            actions = actions,
            isBackVisible = isBackVisible,
        )
    }

    // region handle Event

    private fun handleLoggedInStateChecked(
        state: State,
        event: Event.LoggedInStateChecked,
    ): State {
        scope.launch {
            if (event.isLoggedIn) {
                postNavigationEvent(MainNavGraph.startDestination)
            } else {
                postNavigationEvent(AuthNavGraph.startDestination)
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

    // endregion handle Event

    // region handle UiEvent

    private fun handleBackClicked(state: State): State {
        scope.launch {
            postNavigationUpEvent()
        }
        return state
    }

    private fun handleRegisterClicked(state: State): State {
        scope.launch {
            postNavigationEvent(RegisterDestination)
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
                _isInitialLoading.emit(false)
            }
        }
        return state.copy(currentDestination = event.route)
    }

    // endregion handle UiEvent

    private suspend fun postNavigationUpEvent() {
        postNavigationEvent(Optional.empty())
    }

    private suspend fun postNavigationEvent(direction: DestinationSpec) {
        postNavigationEvent(Optional.of(direction))
    }

    private suspend fun postNavigationEvent(direction: Optional<DestinationSpec>) {
        _navigationEvents.emit(direction)
    }

    private suspend fun postSnackBarEvent(snackBarData: SnackBarData) {
        _showSnackBar.emit(snackBarData)
        delay(TimeUnit.SECONDS.toMillis(snackBarData.secondsAlive))
        _showSnackBar.emit(SnackBarData.empty())
    }

    private fun validateNavDestination(route: String): Boolean {
        return RootNavGraph.nestedNavGraphs.any { graph ->
            graph.destinations.any { it.baseRoute == route }
        }
    }

    data class State(
        val currentDestination: String? = null,
        val isInitialized: Boolean = false,
    )

    data class UiState(
        val actions: List<TopBarAction>,
        val isBackVisible: Boolean,
    )

    sealed interface Event {
        data class LoggedInStateChecked(val isLoggedIn: Boolean) : Event
        data object CheckIfLoggedIn : Event
    }

    sealed interface UiEvent : Event {
        data object BackClicked : UiEvent
        data object RegisterClicked : UiEvent
        data class NavDestinationChanged(val route: String) : UiEvent
    }
}

suspend inline fun <TYPE : Optional<SUBTYPE>, reified SUBTYPE> Flow<TYPE>.collectDestination(
    collector: FlowCollector<SUBTYPE>,
) {
    collect { value -> collector.emit(value.orElse(null)) }
}
