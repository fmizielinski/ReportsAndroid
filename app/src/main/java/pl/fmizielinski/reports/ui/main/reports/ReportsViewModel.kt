package pl.fmizielinski.reports.ui.main.reports

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.model.Report
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.domain.usecase.report.GetReportsUseCase
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.Event
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.State
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiState
import timber.log.Timber

@KoinViewModel
class ReportsViewModel(
    dispatcher: CoroutineDispatcher,
    private val getReportsUseCase: GetReportsUseCase,
    private val eventsRepository: EventsRepository,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()) {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoadReports -> handleLoadReports(state)
            is Event.ReportsLoaded -> handleReportsLoaded(state, event)
            is Event.LoadReportsFailed -> handleLoadReportsFailed(state)
            is UiEvent.ListScrolled -> handleListScrolled(state, event)
            is UiEvent.Refresh -> handleRefresh(state)
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
        return UiState(
            reports = reports,
            isLoading = state.loadingInProgress,
            isRefreshing = state.isRefreshing,
        )
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
                postEvent(Event.LoadReportsFailed)
            }
        }
        return state.copy(loadingInProgress = true)
    }

    private fun handleReportsLoaded(state: State, event: Event.ReportsLoaded): State {
        return state.copy(
            reports = event.reports,
            loadingInProgress = false,
            isRefreshing = false,
        )
    }

    private fun handleLoadReportsFailed(state: State): State {
        scope.launch {
            Timber.e("Reports loading failed")
            val snackBarData = SnackBarData(messageResId = R.string.common_error_oops)
            eventsRepository.postSnackBarEvent(snackBarData)
        }
        return state.copy(loadingInProgress = false, isRefreshing = false)
    }

    // endregion handle Event

    // region handle UiEvent

    private fun handleListScrolled(state: State, event: UiEvent.ListScrolled): State {
        scope.launch {
            val globalEvent = GlobalEvent.ChangeFabVisibility(event.firstItemIndex == 0)
            eventsRepository.postGlobalEvent(globalEvent)
        }
        return state
    }

    private fun handleRefresh(state: State): State {
        scope.launch {
            postEvent(Event.LoadReports)
        }
        return state.copy(isRefreshing = true)
    }

    // endregion handle UiEvent

    data class State(
        val reports: List<Report> = emptyList(),
        val loadingInProgress: Boolean = false,
        val isRefreshing: Boolean = false,
    )

    data class UiState(
        val reports: List<Report>,
        val isLoading: Boolean,
        val isRefreshing: Boolean,
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
        data object LoadReportsFailed : Event
    }

    sealed interface UiEvent : Event {
        data class ListScrolled(val firstItemIndex: Int) : UiEvent
        data object Refresh : UiEvent
    }
}
