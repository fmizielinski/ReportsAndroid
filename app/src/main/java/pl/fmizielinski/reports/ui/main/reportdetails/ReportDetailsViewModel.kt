package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.destinations.destinations.ReportDetailsDestination
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.Event
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.State
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState

@KoinViewModel
class ReportDetailsViewModel(
    dispatcher: CoroutineDispatcher,
    handle: SavedStateHandle,
    private val eventsRepository: EventsRepository,
    private val getReportDetailsUseCase: GetReportDetailsUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(
    dispatcher = dispatcher,
    mState = createState(handle),
) {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoadReportDetails -> handleLoadReportDetails(state)
            is Event.ReportDetailsLoaded -> handleReportDetailsLoaded(state, event)
            is Event.LoadReportDetailsFailed -> handleLoadReportDetailsFailed(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        return UiState(
            isLoading = state.isLoading,
            report = state.report,
        )
    }

    override suspend fun onStart() {
        super.onStart()
        postEvent(Event.LoadReportDetails)
    }

    // region handle Event

    private fun handleLoadReportDetails(state: State): State {
        scope.launch {
            try {
                val report = getReportDetailsUseCase(state.id)
                postEvent(Event.ReportDetailsLoaded(report))
            } catch (error: SimpleErrorException) {
                logError(error)
                postEvent(Event.LoadReportDetailsFailed(error))
            }
        }
        return state.copy(isLoading = true)
    }

    private fun handleReportDetailsLoaded(state: State, event: Event.ReportDetailsLoaded): State {
        return state.copy(isLoading = false, report = event.report)
    }

    private fun handleLoadReportDetailsFailed(
        state: State,
        event: Event.LoadReportDetailsFailed,
    ): State {
        scope.launch {
            eventsRepository.postSnackBarEvent(event.error.toSnackBarData())
            eventsRepository.postNavUpEvent()
        }
        return state.copy(isLoading = false)
    }

    // endregion handle Event

    data class State(
        val id: Int,
        val isLoading: Boolean = true,
        val report: ReportDetails? = null,
    )

    data class UiState(
        val isLoading: Boolean,
        val report: ReportDetails?,
    )

    sealed interface Event {
        data object LoadReportDetails : Event
        data class ReportDetailsLoaded(val report: ReportDetails) : Event
        data class LoadReportDetailsFailed(val error: SimpleErrorException) : Event
    }

    sealed interface UiEvent : Event
}

private fun createState(handle: SavedStateHandle): State {
    val args = ReportDetailsDestination.argsFrom(handle)
    return State(id = args.id)
}