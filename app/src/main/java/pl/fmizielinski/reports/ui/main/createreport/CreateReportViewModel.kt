package pl.fmizielinski.reports.ui.main.createreport

import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.Event
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.State
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiState

@KoinViewModel
class CreateReportViewModel(
    dispatcher: CoroutineDispatcher,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is UiEvent.TitleChanged -> handleTitleChanged(state, event)
            is UiEvent.DescriptionChanged -> handleDescriptionChanged(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        return UiState(
            titleLength = state.title.length,
            descriptionLength = state.description.length,
        )
    }

    // region handle Event

    private fun handleTitleChanged(state: State, event: UiEvent.TitleChanged): State {
        return state.copy(title = event.title)
    }

    private fun handleDescriptionChanged(state: State, event: UiEvent.DescriptionChanged): State {
        return state.copy(description = event.description)
    }

    // endregion handle Event

    data class State(
        val title: String = "",
        val description: String = "",
    )

    data class UiState(
        val titleLength: Int,
        val descriptionLength: Int,
    )

    sealed interface Event

    sealed interface UiEvent : Event {
        data class TitleChanged(val title: String) : UiEvent
        data class DescriptionChanged(val description: String) : UiEvent
    }
}
