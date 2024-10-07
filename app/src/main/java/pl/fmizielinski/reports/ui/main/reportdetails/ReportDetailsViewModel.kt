package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.report.model.Comment
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import pl.fmizielinski.reports.domain.report.usecase.GetCommentsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsAttachmentGalleryNavArgsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.destinations.destinations.AttachmentGalleryDestination
import pl.fmizielinski.reports.ui.destinations.destinations.ReportDetailsDestination
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.Event
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.State
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.DestinationData

@KoinViewModel
class ReportDetailsViewModel(
    dispatcher: CoroutineDispatcher,
    handle: SavedStateHandle,
    private val eventsRepository: EventsRepository,
    private val getReportDetailsUseCase: GetReportDetailsUseCase,
    private val getAttachmentGalleryNavArgsUseCase: GetReportDetailsAttachmentGalleryNavArgsUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(
    dispatcher = dispatcher,
    mState = createState(handle),
) {

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.LoadReportDetails -> handleLoadReportDetails(state)
            is Event.ReportDetailsLoaded -> handleReportDetailsLoaded(state, event)
            is Event.LoadReportDetailsFailed -> handleLoadReportDetailsFailed(state, event)
            is Event.LoadComments -> handleLoadComments(state)
            is Event.CommentsLoaded -> handleCommentsLoaded(state, event)
            is Event.LoadCommentsFailed -> handleLoadCommentsFailed(state, event)
            is UiEvent.PreviewAttachment -> handlePreviewAttachment(state, event)
            is UiEvent.TabClicked -> handleTabClicked(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        val selectedTab = when (state.selectedTab) {
            State.Tab.DETAILS -> UiState.Tab.DETAILS
            State.Tab.COMMENTS -> UiState.Tab.COMMENTS
        }
        return UiState(
            isLoading = state.isReportLoading || state.isCommentsLoading,
            report = getReportDetailsUiState(state.report),
            comments = getCommentsUiState(state.comments),
            selectedTab = selectedTab,
        )
    }

    private fun getReportDetailsUiState(report: ReportDetails?): UiState.ReportDetails? {
        return report?.let {
            UiState.ReportDetails(
                id = it.id,
                title = it.title,
                description = it.description,
                reportDate = it.reportDate,
                attachments = it.attachments.map { attachment ->
                    UiState.ReportDetails.Attachment(
                        id = attachment.id,
                        path = attachment.path,
                    )
                },
            )
        }
    }

    private fun getCommentsUiState(comments: List<Comment>): List<UiState.Comment> {
        return comments.map { comment ->
            UiState.Comment(
                id = comment.id,
                comment = comment.comment,
                user = comment.user,
                createDate = comment.createDate,
                isMine = comment.isMine,
            )
        }
    }

    override suspend fun onStart() {
        super.onStart()
        postEvent(Event.LoadReportDetails)
        postEvent(Event.LoadComments)
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
        return state.copy(isReportLoading = true)
    }

    private fun handleReportDetailsLoaded(state: State, event: Event.ReportDetailsLoaded): State {
        return state.copy(isReportLoading = false, report = event.report)
    }

    private fun handleLoadReportDetailsFailed(
        state: State,
        event: Event.LoadReportDetailsFailed,
    ): State {
        scope.launch {
            eventsRepository.postSnackBarEvent(event.error.toSnackBarData())
            eventsRepository.postNavUpEvent()
        }
        return state.copy(isReportLoading = false)
    }

    private fun handleLoadComments(state: State): State {
        scope.launch {
            try {
                val comments = getCommentsUseCase(state.id)
                postEvent(Event.CommentsLoaded(comments))
            } catch (error: SimpleErrorException) {
                logError(error)
                postEvent(Event.LoadCommentsFailed(error))
            }
        }
        return state.copy(isCommentsLoading = true)
    }

    private fun handleCommentsLoaded(state: State, event: Event.CommentsLoaded): State {
        return state.copy(isCommentsLoading = false, comments = event.comments)
    }

    private fun handleLoadCommentsFailed(
        state: State,
        event: Event.LoadCommentsFailed,
    ): State {
        scope.launch {
            eventsRepository.postSnackBarEvent(event.error.toSnackBarData())
        }
        return state.copy(isCommentsLoading = false)
    }

    // endregion handle Event

    // region handle UiEvent

    private fun handlePreviewAttachment(state: State, event: UiEvent.PreviewAttachment): State {
        scope.launch {
            checkNotNull(state.report)
            val navArgs = getAttachmentGalleryNavArgsUseCase(event.id, state.report.attachments)
            val destination = DestinationData(AttachmentGalleryDestination(navArgs))
            eventsRepository.postNavEvent(destination)
        }
        return state
    }

    private fun handleTabClicked(state: State, event: UiEvent.TabClicked): State {
        val selectedTab = when (event.tab) {
            UiState.Tab.DETAILS -> State.Tab.DETAILS
            UiState.Tab.COMMENTS -> State.Tab.COMMENTS
        }
        return state.copy(selectedTab = selectedTab)
    }

    // endregion handle UiEvent

    data class State(
        val id: Int,
        val isReportLoading: Boolean = true,
        val report: ReportDetails? = null,
        val isCommentsLoading: Boolean = true,
        val comments: List<Comment> = emptyList(),
        val selectedTab: Tab = Tab.DETAILS,
    ) {

        enum class Tab {
            DETAILS,
            COMMENTS,
        }
    }

    data class UiState(
        val isLoading: Boolean,
        val report: ReportDetails?,
        val comments: List<UiState.Comment>,
        val selectedTab: Tab,
    ) {

        val selectedTabIndex: Int
            get() = when (selectedTab) {
                Tab.DETAILS -> 0
                Tab.COMMENTS -> 1
            }

        data class ReportDetails(
            val id: Int,
            val title: String,
            val description: String,
            val reportDate: String,
            val attachments: List<Attachment>,
        ) {

            data class Attachment(
                val id: Int,
                val path: String,
            )
        }

        data class Comment(
            val id: Int,
            val comment: String,
            val user: String,
            val createDate: String,
            val isMine: Boolean,
        )

        enum class Tab {
            DETAILS,
            COMMENTS,
        }
    }

    sealed interface Event {
        data object LoadReportDetails : Event
        data class ReportDetailsLoaded(val report: ReportDetails) : Event
        data class LoadReportDetailsFailed(val error: SimpleErrorException) : Event
        data object LoadComments : Event
        data class CommentsLoaded(val comments: List<Comment>) : Event
        data class LoadCommentsFailed(val error: SimpleErrorException) : Event
    }

    sealed interface UiEvent : Event {
        data class PreviewAttachment(val id: Int) : UiEvent
        data class TabClicked(val tab: UiState.Tab) : UiEvent
    }
}

private fun createState(handle: SavedStateHandle): State {
    val args = ReportDetailsDestination.argsFrom(handle)
    return State(id = args.id)
}
