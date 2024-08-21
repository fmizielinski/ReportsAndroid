package pl.fmizielinski.reports.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn

abstract class BaseViewModel<State, Event, UiState, in UiEvent : Event>(
    protected val dispatcher: CoroutineDispatcher,
    mState: State,
) : ViewModel() {

    protected val scope = CoroutineScope(dispatcher)
    private val events = MutableSharedFlow<Event>()
    private val state: Flow<State> = events
        .runningFold(mState, ::handleEvent)
        .shareIn(
            scope = scope,
            started = SharingStarted.Lazily,
            replay = 1,
        )
    val uiState: Flow<UiState> = state.map(::mapState)

    val initialUiState: UiState = this.mapState(mState)

    protected abstract fun handleEvent(
        state: State,
        event: Event,
    ): State

    protected abstract fun mapState(state: State): UiState

    suspend fun postUiEvent(event: UiEvent) {
        events.emit(event)
    }

    protected suspend fun postEvent(event: Event) {
        events.emit(event)
    }

    open suspend fun onStart(): Unit = Unit

    open suspend fun onStop(): Unit = Unit

    override fun onCleared() {
        scope.cancel("onCleared")
        super.onCleared()
    }
}
