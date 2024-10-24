package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.report.model.AddCommentData
import pl.fmizielinski.reports.domain.report.model.Comment
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import pl.fmizielinski.reports.domain.report.usecase.AddCommentUseCase
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
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState.Comment.Status
import pl.fmizielinski.reports.ui.navigation.DestinationData
import java.time.LocalDateTime

@KoinViewModel
class ReportDetailsViewModel(
    dispatcher: CoroutineDispatcher,
    handle: SavedStateHandle,
    private val eventsRepository: EventsRepository,
    private val getReportDetailsUseCase: GetReportDetailsUseCase,
    private val getAttachmentGalleryNavArgsUseCase: GetReportDetailsAttachmentGalleryNavArgsUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
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
            is Event.CommentAdded -> handleCommentAdded(state, event)
            is Event.AddCommentFailed -> handleAddCommentFailed(state)
            is UiEvent.PreviewAttachment -> handlePreviewAttachment(state, event)
            is UiEvent.TabClicked -> handleTabClicked(state, event)
            is UiEvent.CommentChanged -> handleCommentChanged(state, event)
            is UiEvent.SendClicked -> handleSendClicked(state)
            is UiEvent.CommentClicked -> handleCommentClicked(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        val selectedTab = when (state.selectedTab) {
            State.Tab.DETAILS -> UiState.Tab.DETAILS
            State.Tab.COMMENTS -> UiState.Tab.COMMENTS
        }
        val comments = UiState.Comments(
            list = state.getCommentListUiState(),
            isLoading = state.isCommentsLoading,
        )
        return UiState(
            isLoading = state.isReportLoading || state.isCommentsLoading,
            report = getReportDetailsUiState(state.report),
            comments = comments,
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

    private fun State.getCommentListUiState(): List<UiState.Comment> {
        return buildList {
            val mappedComments = comments.map { comment ->
                UiState.Comment(
                    id = comment.id,
                    comment = comment.comment,
                    user = comment.user,
                    createDate = comment.createDate,
                    isMine = comment.isMine,
                    status = Status.SENT,
                )
            }
            addAll(mappedComments)
            if (sendingCommentData != null) {
                val sendingComment = UiState.Comment(
                    id = null,
                    comment = sendingCommentData.data.comment,
                    user = "",
                    createDate = "",
                    isMine = true,
                    status = if (sendingCommentData.isFailed) {
                        Status.SENDING_FAILED
                    } else {
                        Status.SENDING
                    },
                )
                add(sendingComment)
            }
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
            eventsRepository.postNavUpEvent()
        }
        return state.copy(isCommentsLoading = false)
    }

    private fun handleCommentAdded(state: State, event: Event.CommentAdded): State {
        val comments = buildList {
            addAll(state.comments)
            add(event.comment)
        }
        return state.copy(
            comments = comments,
            sendingCommentData = null,
        )
    }

    private fun handleAddCommentFailed(state: State): State {
        val sendingCommentData = state.sendingCommentData?.copy(isFailed = true)
        return state.copy(sendingCommentData = sendingCommentData)
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

    private fun handleCommentChanged(state: State, event: UiEvent.CommentChanged): State {
        return state.copy(commentText = event.comment)
    }

    private fun handleSendClicked(state: State): State {
        if (state.commentText.isBlank()) {
            return state
        }
        val data = AddCommentData(
            comment = state.commentText,
            createDate = LocalDateTime.now(),
        )
        val sendingCommentData = state.sendComment(data)
        return state.copy(
            sendingCommentData = sendingCommentData,
            commentText = "",
        )
    }

    private fun handleCommentClicked(state: State, event: UiEvent.CommentClicked): State {
        return if (state.sendingCommentData?.isFailed == true && event.id == null) {
            val sendingCommentData = state.sendComment(state.sendingCommentData.data)
            state.copy(sendingCommentData = sendingCommentData)
        } else {
            state
        }
    }

    // endregion handle UiEvent

    private fun State.sendComment(data: AddCommentData): State.SendingCommentData {
        scope.launch {
            try {
                val comment = addCommentUseCase(id, data)
                postEvent(Event.CommentAdded(comment))
            } catch (error: SimpleErrorException) {
                logError(error)
                postEvent(Event.AddCommentFailed)
            }
        }
        return State.SendingCommentData(
            data = data,
            isFailed = false,
        )
    }

    data class State(
        val id: Int,
        val isReportLoading: Boolean = true,
        val report: ReportDetails? = null,
        val isCommentsLoading: Boolean = true,
        val comments: List<Comment> = emptyList(),
        val selectedTab: Tab = Tab.DETAILS,
        val commentText: String = "",
        val sendingCommentData: SendingCommentData? = null,
    ) {

        enum class Tab {
            DETAILS,
            COMMENTS,
        }

        data class SendingCommentData(
            val data: AddCommentData,
            val isFailed: Boolean,
        )
    }

    data class UiState(
        val isLoading: Boolean,
        val report: ReportDetails?,
        val comments: Comments,
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

        data class Comments(
            val list: List<Comment>,
            val isLoading: Boolean,
        ) {
            val isSending: Boolean
                get() = list.any { it.status == Status.SENDING }
            val scrollToFirst: Boolean
                get() = isSending || !isLoading
        }

        data class Comment(
            val id: Int?,
            val comment: String,
            val user: String,
            val createDate: String,
            val isMine: Boolean,
            val status: Status,
        ) {

            enum class Status {
                SENDING,
                SENDING_FAILED,
                SENT,
            }
        }

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
        data class CommentAdded(val comment: Comment) : Event
        data object AddCommentFailed : Event
    }

    sealed interface UiEvent : Event {
        data class PreviewAttachment(val id: Int) : UiEvent
        data class TabClicked(val tab: UiState.Tab) : UiEvent
        data class CommentChanged(val comment: String) : UiEvent
        data object SendClicked : UiEvent
        data class CommentClicked(val id: Int?) : UiEvent
    }
}

private fun createState(handle: SavedStateHandle): State {
    val args = ReportDetailsDestination.argsFrom(handle)
    return State(id = args.id)
}
