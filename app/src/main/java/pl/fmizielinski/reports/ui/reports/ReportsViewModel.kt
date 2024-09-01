package pl.fmizielinski.reports.ui.reports

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.model.Report
import pl.fmizielinski.reports.domain.usecase.report.GetReportsUseCase
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.reports.ReportsViewModel.Event
import pl.fmizielinski.reports.ui.reports.ReportsViewModel.State
import pl.fmizielinski.reports.ui.reports.ReportsViewModel.UiEvent
import pl.fmizielinski.reports.ui.reports.ReportsViewModel.UiState

@KoinViewModel
class ReportsViewModel(
    dispatcher: CoroutineDispatcher,
    private val getReportsUseCase: GetReportsUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoadReports -> handleLoadReports(state)
            is Event.ReportsLoaded -> handleReportsLoaded(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        val reports = state.reports.map { report ->
            UiState.Report(
                id = report.id,
                title = report.title,
                description = report.description,
                reportDate = report.reportDate,
                comments = report.comments,
            )
        }
        return UiState(reports)
    }

    override suspend fun onStart() {
        super.onStart()
        postEvent(Event.LoadReports)
    }

    // region handle Event

    private fun handleLoadReports(state: State): State {
        scope.launch {
            try {
                val reports = getReportsUseCase()
                postEvent(Event.ReportsLoaded(reports))
            } catch (error: ErrorException) {
                logError(error)
            }
        }
        return state
    }

    private fun handleReportsLoaded(state: State, event: Event.ReportsLoaded): State {
        return state.copy(reports = event.reports)
    }

    // endregion handle Event

    data class State(
        val reports: List<Report> = emptyList(),
    )

    data class UiState(
        val reports: List<Report>,
    ) {

        data class Report(
            val id: Int,
            val title: String,
            val description: String,
            val reportDate: String,
            val comments: Int,
        )
    }

    sealed interface Event {
        data object LoadReports : Event
        data class ReportsLoaded(val reports: List<Report>) : Event
    }

    sealed interface UiEvent : Event
}
