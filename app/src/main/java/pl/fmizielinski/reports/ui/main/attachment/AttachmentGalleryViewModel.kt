package pl.fmizielinski.reports.ui.main.attachment

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.destinations.destinations.AttachmentGalleryDestination
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.Event
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.State
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.UiState
import java.io.File

@KoinViewModel
class AttachmentGalleryViewModel(
    dispatcher: CoroutineDispatcher,
    handle: SavedStateHandle,
) : BaseViewModel<State, Event, UiState, UiEvent>(
    dispatcher = dispatcher,
    mState = createState(handle),
) {

    override fun handleEvent(state: State, event: Event): State = state

    override fun mapState(state: State): UiState {
        return UiState(
            initialIndex = state.initialIndex,
            attachments = state.attachments,
        )
    }

    data class State(
        val initialIndex: Int,
        val attachments: List<File>,
    )

    data class UiState(
        val initialIndex: Int,
        val attachments: List<File>,
    )

    sealed interface Event

    sealed interface UiEvent : Event
}

private fun createState(handle: SavedStateHandle): State {
    val args = AttachmentGalleryDestination.argsFrom(handle)
    return State(
        initialIndex = args.initialIndex,
        attachments = args.attachments,
    )
}
