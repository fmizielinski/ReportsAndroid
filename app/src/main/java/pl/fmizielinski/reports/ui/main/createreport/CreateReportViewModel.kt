package pl.fmizielinski.reports.ui.main.createreport

import com.ramcosta.composedestinations.generated.destinations.ReportsDestination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.model.CreateReportData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.domain.usecase.report.AddAttachmentUseCase
import pl.fmizielinski.reports.domain.usecase.report.CreateReportUseCase
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.base.ErrorHandler
import pl.fmizielinski.reports.ui.base.ErrorHandler.VerificationError
import pl.fmizielinski.reports.ui.base.filterIsNotInstance
import pl.fmizielinski.reports.ui.base.findVerificationError
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.Event
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.State
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import java.io.File
import java.time.LocalDateTime

@KoinViewModel
class CreateReportViewModel(
    dispatcher: CoroutineDispatcher,
    private val eventsRepository: EventsRepository,
    private val createReportUseCase: CreateReportUseCase,
    private val addAttachmentUseCase: AddAttachmentUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()), ErrorHandler {

    init {
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.SaveReport>()
                .collect { postEvent(Event.SaveReport) }
        }
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.AddAttachment>()
                .collect { postEvent(Event.AddAttachment(it.photoFile)) }
        }
    }

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.SaveReport -> handleSaveReport(state)
            is Event.CreateReportSuccess -> handleCreateReportSuccess(state, event)
            is Event.CreateReportFailed -> handleCreateReportFailed(state, event)
            is Event.PostVerificationError -> handleVerificationError(state, event)
            is Event.AddAttachment -> handleAddAttachment(state, event)
            is Event.AttachmentUploaded -> handleAttachmentUploaded(state, event)
            is Event.AttachmentUploadFailed -> handleAttachmentUploadFailed(state, event)
            is UiEvent.TitleChanged -> handleTitleChanged(state, event)
            is UiEvent.DescriptionChanged -> handleDescriptionChanged(state, event)
            is UiEvent.DeleteAttachment -> handleDeleteAttachment(state, event)
            is UiEvent.ListScrolled -> handleListScrolled(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        val titleVerificationError = state.verificationErrors
            .findVerificationError<Title>()
        val descriptionVerificationError = state.verificationErrors
            .findVerificationError<Description>()
        val attachments = state.attachments.map { it.file }
        return UiState(
            titleLength = state.title.length,
            descriptionLength = state.description.length,
            titleVerificationError = titleVerificationError,
            descriptionVerificationError = descriptionVerificationError,
            attachments = attachments,
        )
    }

    // region handle Event

    private fun handleSaveReport(state: State): State {
        scope.launch {
            try {
                val reportId = createReportUseCase(
                    CreateReportData(
                        title = state.title,
                        description = state.description,
                        reportDate = LocalDateTime.now(),
                    ),
                )
                postEvent(Event.CreateReportSuccess(reportId))
            } catch (error: ErrorException) {
                logError(error)
                postEvent(Event.CreateReportFailed(error))
            }
        }
        return state
    }

    private fun handleCreateReportSuccess(state: State, event: Event.CreateReportSuccess): State {
        scope.launch {
            if (state.attachments.isNotEmpty()) {
                state.attachments.forEach { attachment ->
                    try {
                        addAttachmentUseCase(event.reportId, attachment.file)
                        postEvent(Event.AttachmentUploaded(attachment.file))
                    } catch (error: ErrorException) {
                        logError(error)
                        postEvent(Event.AttachmentUploadFailed(attachment.file, error))
                    }
                }
            } else {
                eventsRepository.postNavEvent(ReportsDestination.toDestinationData())
            }
        }
        return state
    }

    private fun handleCreateReportFailed(state: State, event: Event.CreateReportFailed): State {
        scope.launch {
            handleError(event.error)
            eventsRepository.postGlobalEvent(GlobalEvent.ChangeFabVisibility(isVisible = true))
        }
        return state
    }

    private fun handleVerificationError(state: State, event: Event.PostVerificationError): State {
        return state.copy(verificationErrors = event.errors)
    }

    private fun handleAddAttachment(state: State, event: Event.AddAttachment): State {
        val attachments = buildList {
            addAll(state.attachments)
            add(State.Attachment(event.photoFile))
        }
        return state.copy(attachments = attachments)
    }

    private fun handleAttachmentUploaded(state: State, event: Event.AttachmentUploaded): State {
        val attachments = state.attachments.updateUploaded(event.attachmentFile)
        scope.launch {
            if (attachments.all { it.isUploaded }) {
                eventsRepository.postNavEvent(ReportsDestination.toDestinationData())
            }
        }
        return state.copy(attachments = attachments)
    }

    private fun handleAttachmentUploadFailed(
        state: State,
        event: Event.AttachmentUploadFailed,
    ): State {
        val attachments = state.attachments.updateUploaded(event.attachmentFile)
        scope.launch {
            if (attachments.all { it.isUploaded }) {
                handleError(event.error)
                eventsRepository.postNavEvent(ReportsDestination.toDestinationData())
            }
        }
        return state.copy(attachments = attachments)
    }

    // endregion handle Event

    // region handle UiEvent

    private fun handleTitleChanged(state: State, event: UiEvent.TitleChanged): State {
        return state.copy(
            title = event.title,
            verificationErrors = state.verificationErrors.filterIsNotInstance<Title>(),
        )
    }

    private fun handleDescriptionChanged(state: State, event: UiEvent.DescriptionChanged): State {
        return state.copy(
            description = event.description,
            verificationErrors = state.verificationErrors.filterIsNotInstance<Description>(),
        )
    }

    private fun handleDeleteAttachment(state: State, event: UiEvent.DeleteAttachment): State {
        val attachments = state.attachments.filterNot { it.file == event.attachmentFile }
        return state.copy(attachments = attachments)
    }

    private fun handleListScrolled(state: State, event: UiEvent.ListScrolled): State {
        scope.launch {
            val globalEvent = GlobalEvent.ChangeFabVisibility(event.firstItemIndex == 0)
            eventsRepository.postGlobalEvent(globalEvent)
        }
        return state
    }

    // endregion handle UiEvent

    // region ErrorHandler

    override fun parseVerificationError(error: SimpleErrorException): VerificationError {
        return when (error.code) {
            ErrorReasons.Report.Create.TITLE_EMPTY -> Title(error.uiMessage)
            ErrorReasons.Report.Create.DESCRIPTION_EMPTY -> Description(error.uiMessage)
            else -> throw IllegalArgumentException("Unsupported verification error")
        }
    }

    override suspend fun handleVerificationError(verificationErrors: List<VerificationError>) {
        postEvent(Event.PostVerificationError(verificationErrors))
    }

    override suspend fun handleNonVerificationError(error: SimpleErrorException) {
        eventsRepository.postSnackBarEvent(error.toSnackBarData())
    }

    // endregion ErrorHandler

    private fun List<State.Attachment>.updateUploaded(attachmentFile: File): List<State.Attachment> {
        return map { attachment ->
            if (attachment.file == attachmentFile) {
                attachment.copy(isUploaded = true)
            } else {
                attachment
            }
        }
    }

    data class State(
        val title: String = "",
        val description: String = "",
        val verificationErrors: List<VerificationError> = emptyList(),
        val attachments: List<Attachment> = emptyList(),
    ) {

        data class Attachment(val file: File, val isUploaded: Boolean = false)
    }

    data class UiState(
        val titleLength: Int,
        val descriptionLength: Int,
        val titleVerificationError: Int?,
        val descriptionVerificationError: Int?,
        val attachments: List<File>,
    )

    sealed interface Event {
        data object SaveReport : Event
        data class CreateReportSuccess(val reportId: Int) : Event
        data class CreateReportFailed(val error: ErrorException) : Event
        data class PostVerificationError(val errors: List<VerificationError>) : Event
        data class AddAttachment(val photoFile: File) : Event
        data class AttachmentUploaded(val attachmentFile: File) : Event
        data class AttachmentUploadFailed(
            val attachmentFile: File,
            val error: ErrorException,
        ) : Event
    }

    sealed interface UiEvent : Event {
        data class TitleChanged(val title: String) : UiEvent
        data class DescriptionChanged(val description: String) : UiEvent
        data class DeleteAttachment(val attachmentFile: File) : UiEvent
        data class ListScrolled(val firstItemIndex: Int) : UiEvent
    }

    data class Title(override val messageResId: Int) : VerificationError
    data class Description(override val messageResId: Int) : VerificationError
}
