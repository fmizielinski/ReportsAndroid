package pl.fmizielinski.reports.ui.main.createreport

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.report.CreateReportUseCase
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.Event
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.State
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiState

@KoinViewModel
class CreateReportViewModel(
    dispatcher: CoroutineDispatcher,
    private val eventsRepository: EventsRepository,
    private val createReportUseCase: CreateReportUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    init {
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<EventsRepository.GlobalEvent.SaveReport>()
                .collect { postSaveEvent() }
        }
    }

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.SaveReport -> handleSaveReport(state)
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

    private fun handleSaveReport(state: State): State {
        scope.launch {

        }
        return state
    }

    // endregion handle Event

    // region handle UiEvent

    private fun handleTitleChanged(state: State, event: UiEvent.TitleChanged): State {
        return state.copy(title = event.title)
    }

    private fun handleDescriptionChanged(state: State, event: UiEvent.DescriptionChanged): State {
        return state.copy(description = event.description)
    }

    // endregion handle UiEvent

    private suspend fun postSaveEvent() {
        postEvent(Event.SaveReport)
    }

    data class State(
        val title: String = "",
        val description: String = "",
    )

    data class UiState(
        val titleLength: Int,
        val descriptionLength: Int,
    )

    sealed interface Event {
        data object SaveReport : Event
    }

    sealed interface UiEvent : Event {
        data class TitleChanged(val title: String) : UiEvent
        data class DescriptionChanged(val description: String) : UiEvent
    }
}
