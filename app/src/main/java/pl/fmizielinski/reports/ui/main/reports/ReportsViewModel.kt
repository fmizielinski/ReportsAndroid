package pl.fmizielinski.reports.ui.main.reports

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.report.model.Report
import pl.fmizielinski.reports.domain.report.usecase.GetReportsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.base.PagingContentProvider
import pl.fmizielinski.reports.ui.destinations.destinations.ReportDetailsDestination
import pl.fmizielinski.reports.ui.main.reportdetails.model.ReportDetailsNavArgs
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.Event
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.State
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.DestinationData

@KoinViewModel
class ReportsViewModel(
    dispatcher: CoroutineDispatcher,
    private val getReportsUseCase: GetReportsUseCase,
    private val eventsRepository: EventsRepository,
) : BaseViewModel<State, Event, UiState, UiEvent>(
    dispatcher = dispatcher,
    mState = State(),
),
    PagingContentProvider<Report, UiState.Report> {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is UiEvent.ListScrolled -> handleListScrolled(state, event)
            is UiEvent.Refresh -> handleRefresh(state)
            is UiEvent.ReportClicked -> handleReportClicked(state, event)
        }
    }

    override fun mapState(state: State): UiState = UiState()

    override fun providePagingContentFlow(): Flow<PagingData<Report>> {
        return getReportsUseCase.data
    }

    override fun mapPagingContent(data: PagingData<Report>): PagingData<UiState.Report> {
        return data.map { report ->
            UiState.Report(
                id = report.id,
                title = report.title,
                description = report.description,
                reportDate = report.reportDate,
                comments = report.comments,
            )
        }
    }

    // region handle UiEvent

    private fun handleListScrolled(state: State, event: UiEvent.ListScrolled): State {
        scope.launch {
            val globalEvent = GlobalEvent.ChangeFabVisibility(event.firstItemIndex == 0)
            eventsRepository.postGlobalEvent(globalEvent)
        }
        return state
    }

    private fun handleRefresh(state: State): State {
        getReportsUseCase()
        return state
    }

    private fun handleReportClicked(state: State, event: UiEvent.ReportClicked): State {
        scope.launch {
            val navArgs = ReportDetailsNavArgs(id = event.reportId)
            val destination = DestinationData(ReportDetailsDestination(navArgs))
            eventsRepository.postNavEvent(destination)
        }
        return state
    }

    // endregion handle UiEvent

    class State

    class UiState {

        data class Report(
            val id: Int,
            val title: String,
            val description: String,
            val reportDate: String,
            val comments: Int,
        )
    }

    sealed interface Event

    sealed interface UiEvent : Event {
        data class ListScrolled(val firstItemIndex: Int) : UiEvent
        data object Refresh : UiEvent
        data class ReportClicked(val reportId: Int) : UiEvent
    }
}
