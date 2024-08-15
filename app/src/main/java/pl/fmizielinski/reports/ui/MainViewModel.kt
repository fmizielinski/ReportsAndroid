package pl.fmizielinski.reports.ui

import com.ramcosta.composedestinations.spec.Direction
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

    override fun handleEvent(
        state: State,
        event: Event,
    ): State = state

    override fun mapState(state: State): UiState = UiState()

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

    class State

    class UiState

    sealed interface Event

    sealed interface UiEvent : Event
}

suspend inline fun <TYPE : Optional<SUBTYPE>, reified SUBTYPE> Flow<TYPE>.collectDestination(collector: FlowCollector<SUBTYPE>) {
    collect { value -> collector.emit(value.orElse(null)) }
}
