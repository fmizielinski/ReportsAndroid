package pl.fmizielinski.reports.ui

import com.ramcosta.composedestinations.generated.destinations.LoginDestination
import com.ramcosta.composedestinations.generated.destinations.RegisterDestination
import com.ramcosta.composedestinations.generated.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.spec.Direction
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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showSnackBar = MutableSharedFlow<SnackBarData>()
    val showSnackBar: SharedFlow<SnackBarData> = _showSnackBar

    private val _navigationEvents = MutableSharedFlow<Optional<Direction>>()
    val navigationEvents: SharedFlow<Optional<Direction>> = _navigationEvents

    init {
        scope.launch {
            eventsRepository.navigationEvent.collect(::postNavigationEvent)
        }
        scope.launch {
            eventsRepository.showSnackBar.collect(::postSnackBarEvent)
        }
        scope.launch {
            isLoggedInUseCase().collect { isLoggedIn ->
                postEvent(Event.LoggedInStateChanged(isLoggedIn))
                _isLoading.emit(isLoggedIn)
            }
        }
    }

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoggedInStateChanged -> handleLoggedInStateChanged(state, event)
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
        val isBackVisible = state.currentDestination != RootNavGraph.startDestination.baseRoute
        return UiState(
            actions = actions,
            isBackVisible = isBackVisible,
            isLoggedIn = state.isLoggedIn,
        )
    }

    // region handleEvent

    private fun handleLoggedInStateChanged(state: State, event: Event.LoggedInStateChanged): State {
        return state.copy(isLoggedIn = event.isLoggedIn)
    }

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
        if (RootNavGraph.destinations.none { it.baseRoute == event.route }) {
            error("Unknown destination - ${event.route}")
        }
        return state.copy(currentDestination = event.route)
    }

    // endregion handleEvent

    private suspend fun postNavigationUpEvent() {
        postNavigationEvent(Optional.empty())
    }

    private suspend fun postNavigationEvent(direction: Direction) {
        postNavigationEvent(Optional.of(direction))
    }

    private suspend fun postNavigationEvent(direction: Optional<Direction>) {
        _navigationEvents.emit(direction)
    }

    private suspend fun postSnackBarEvent(snackBarData: SnackBarData) {
        _showSnackBar.emit(snackBarData)
        delay(TimeUnit.SECONDS.toMillis(snackBarData.secondsAlive))
        _showSnackBar.emit(SnackBarData.empty())
    }

    data class State(
        val currentDestination: String? = null,
        val isLoggedIn: Boolean = false,
    )

    data class UiState(
        val actions: List<TopBarAction>,
        val isBackVisible: Boolean,
        val isLoggedIn: Boolean,
    )

    sealed interface Event {
        data class LoggedInStateChanged(val isLoggedIn: Boolean) : Event
    }

    sealed interface UiEvent : Event {
        object BackClicked : UiEvent
        object RegisterClicked : UiEvent
        data class NavDestinationChanged(val route: String) : UiEvent
    }
}

suspend inline fun <TYPE : Optional<SUBTYPE>, reified SUBTYPE> Flow<TYPE>.collectDestination(
    collector: FlowCollector<SUBTYPE>,
) {
    collect { value -> collector.emit(value.orElse(null)) }
}
