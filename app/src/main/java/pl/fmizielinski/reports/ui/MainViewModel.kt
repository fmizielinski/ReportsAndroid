package pl.fmizielinski.reports.ui

import com.ramcosta.composedestinations.generated.destinations.LoginDestination
import com.ramcosta.composedestinations.generated.destinations.RegisterScreenDestination
import com.ramcosta.composedestinations.generated.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
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
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

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
    }

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
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
        )
    }

    // region handleEvent

    private fun handleBackClicked(state: State): State {
        scope.launch {
            eventsRepository.postNavUpEvent()
        }
        return state
    }

    private fun handleRegisterClicked(state: State): State {
        scope.launch {
            eventsRepository.postNavEvent(RegisterScreenDestination)
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
    )

    data class UiState(
        val actions: List<TopBarAction>,
        val isBackVisible: Boolean,
    )

    sealed interface Event

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
