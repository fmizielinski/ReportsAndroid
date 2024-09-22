package pl.fmizielinski.reports.ui.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import pl.fmizielinski.reports.domain.error.CompositeErrorException
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import timber.log.Timber

abstract class BaseViewModel<State, Event, UiState, in UiEvent : Event>(
    protected val dispatcher: CoroutineDispatcher,
    mState: State,
) : ViewModel() {

    private val tag = this::class.java.simpleName

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
        .distinctUntilChanged()

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

    @CallSuper
    open suspend fun onStart() {
        Timber.tag(tag).i("onStart")
    }

    @CallSuper
    open suspend fun onStop() {
        Timber.tag(tag).i("onStart")
    }

    override fun onCleared() {
        scope.cancel("onCleared")
        super.onCleared()
    }

    protected fun logError(exception: ErrorException) {
        if (exception is SimpleErrorException) {
            Timber.tag(tag).e(exception)
        } else if (exception is CompositeErrorException) {
            exception.exceptions.forEach { Timber.tag(tag).e(it) }
        }
    }
}
