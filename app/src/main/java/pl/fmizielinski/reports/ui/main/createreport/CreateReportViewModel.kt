package pl.fmizielinski.reports.ui.main.createreport

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.report.model.AddTemporaryAttachmentData
import pl.fmizielinski.reports.domain.report.model.AttachmentData
import pl.fmizielinski.reports.domain.report.model.CreateReportData
import pl.fmizielinski.reports.domain.report.model.TemporaryAttachmentUploadResult
import pl.fmizielinski.reports.domain.report.usecase.AddTemporaryAttachmentUseCase
import pl.fmizielinski.reports.domain.report.usecase.CreateReportUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetCreateReportAttachmentGalleryNavArgsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.ui.base.BaseViewModel
import pl.fmizielinski.reports.ui.base.ErrorHandler
import pl.fmizielinski.reports.ui.base.ErrorHandler.VerificationError
import pl.fmizielinski.reports.ui.base.filterIsNotInstance
import pl.fmizielinski.reports.ui.base.findVerificationError
import pl.fmizielinski.reports.ui.destinations.destinations.AttachmentGalleryDestination
import pl.fmizielinski.reports.ui.destinations.destinations.ReportsDestination
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.Event
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.State
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.DestinationData
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime

@KoinViewModel
class CreateReportViewModel(
    dispatcher: CoroutineDispatcher,
    private val eventsRepository: EventsRepository,
    private val createReportUseCase: CreateReportUseCase,
    private val addTemporaryAttachmentUseCase: AddTemporaryAttachmentUseCase,
    private val getAttachmentGalleryNavArgsUseCase: GetCreateReportAttachmentGalleryNavArgsUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()), ErrorHandler {

    init {
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.SaveReport>()
                .collect { postEvent(Event.SaveAttachments) }
        }
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<GlobalEvent.AddAttachment>()
                .collect { postEvent(Event.AddAttachment(it.photoFile)) }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.SaveReport -> handleSaveReport(state)
            is Event.CreateReportSuccess -> handleCreateReportSuccess(state)
            is Event.CreateReportFailed -> handleCreateReportFailed(state, event)
            is Event.PostVerificationError -> handleVerificationError(state, event)
            is Event.AddAttachment -> handleAddAttachment(state, event)
            is Event.AttachmentUploaded -> handleAttachmentUploaded(state, event)
            is Event.AttachmentUploadProgress -> handleAttachmentUploadProgress(state, event)
            is Event.AttachmentUploadFailed -> handleAttachmentUploadFailed(state, event)
            is Event.SaveAttachments -> handleSaveAttachments(state)
            is UiEvent.TitleChanged -> handleTitleChanged(state, event)
            is UiEvent.DescriptionChanged -> handleDescriptionChanged(state, event)
            is UiEvent.DeleteAttachment -> handleDeleteAttachment(state, event)
            is UiEvent.ListScrolled -> handleListScrolled(state, event)
            is UiEvent.PreviewAttachment -> handlePreviewAttachment(state, event)
        }
    }

    override fun mapState(state: State): UiState {
        val titleVerificationError = state.verificationErrors
            .findVerificationError<Title>()
        val descriptionVerificationError = state.verificationErrors
            .findVerificationError<Description>()
        return UiState(
            titleLength = state.title.length,
            descriptionLength = state.description.length,
            titleVerificationError = titleVerificationError,
            descriptionVerificationError = descriptionVerificationError,
            attachments = getAttachments(state.attachments),
            isLoading = state.savingInProgress,
        )
    }

    private fun getAttachments(attachments: List<State.Attachment>): List<UiState.Attachment> {
        return attachments.map { attachment ->
            val isUploading = attachment.isUploading &&
                    !attachment.isUploaded &&
                    !attachment.uploadFailed
            UiState.Attachment(
                localId = attachment.localId,
                file = attachment.file,
                isUploading = isUploading,
                progress = attachment.progress ?: 0f,
                isUploaded = attachment.isUploaded,
                uploadFailed = attachment.uploadFailed,
            )
        }
    }

    // region handle Event

    private fun handleSaveReport(state: State): State {
        scope.launch {
            try {
                val attachments = state.attachments.mapNotNull { it.uuid }
                val data = CreateReportData(
                    title = state.title,
                    description = state.description,
                    reportDate = LocalDateTime.now(),
                    attachments = attachments,
                )
                createReportUseCase(data)
                postEvent(Event.CreateReportSuccess)
            } catch (error: ErrorException) {
                logError(error)
                postEvent(Event.CreateReportFailed(error))
            }
        }
        return state
    }

    private fun handleCreateReportSuccess(state: State): State {
        scope.launch {
            eventsRepository.postNavEvent(ReportsDestination.toDestinationData())
            eventsRepository.postGlobalEvent(GlobalEvent.Loading(isLoading = false))
        }
        return state.copy(savingInProgress = false)
    }

    private fun handleCreateReportFailed(state: State, event: Event.CreateReportFailed): State {
        scope.launch {
            handleError(event.error)
            eventsRepository.postGlobalEvent(GlobalEvent.Loading(isLoading = false))
        }
        return state.copy(savingInProgress = false)
    }

    private fun handleVerificationError(state: State, event: Event.PostVerificationError): State {
        return state.copy(verificationErrors = event.errors)
    }

    private fun handleAddAttachment(state: State, event: Event.AddAttachment): State {
        val attachments = buildList {
            addAll(state.attachments)
            val localId = state.attachments.size + 1
            add(State.Attachment(localId, event.photoFile))
        }
        return state.copy(attachments = attachments)
    }

    private fun handleAttachmentUploaded(state: State, event: Event.AttachmentUploaded): State {
        val attachments = state.attachments.updateUploaded(event.localId, event.uuid)
        scope.launch {
            if (attachments.allUploadsFinished()) {
                postEvent(Event.SaveReport)
            }
        }
        return state.copy(attachments = attachments)
    }

    private fun handleAttachmentUploadProgress(
        state: State,
        event: Event.AttachmentUploadProgress,
    ): State {
        val attachments = state.attachments.map { attachment ->
            if (attachment.localId == event.localId) {
                attachment.copy(progress = event.progress)
            } else {
                attachment
            }
        }
        return state.copy(attachments = attachments)
    }

    private fun handleAttachmentUploadFailed(
        state: State,
        event: Event.AttachmentUploadFailed,
    ): State {
        val attachments = state.attachments.updateUploaded(event.localId)
        val allUploadsFinished = attachments.allUploadsFinished()
        scope.launch {
            if (allUploadsFinished) {
                handleError(event.error)
                eventsRepository.postGlobalEvent(GlobalEvent.Loading(isLoading = false))
            }
        }
        return state.copy(attachments = attachments, savingInProgress = !allUploadsFinished)
    }

    private fun handleSaveAttachments(state: State): State {
        val attachments = state.attachments.map { it.copy(uploadFailed = false) }
        val filteredAttachments = attachments.filter { !it.isUploaded }
        scope.launch {
            eventsRepository.postGlobalEvent(GlobalEvent.Loading(isLoading = true))

            if (filteredAttachments.isEmpty()) {
                postEvent(Event.SaveReport)
            } else {
                filteredAttachments.forEach { attachment ->
                    val data = AddTemporaryAttachmentData(attachment.file)
                    addTemporaryAttachmentUseCase(data)
                        .catch { error ->
                            if (error is ErrorException) {
                                logError(error)
                                postEvent(Event.AttachmentUploadFailed(attachment.localId, error))
                            }
                        }
                        .map { result ->
                            when (result) {
                                is TemporaryAttachmentUploadResult.Progress -> {
                                    Timber.d("Attachment ${attachment.localId} upload progress: ${result.progress}")
                                    Event.AttachmentUploadProgress(
                                        localId = attachment.localId,
                                        progress = result.progress,
                                    )
                                }

                                is TemporaryAttachmentUploadResult.Complete -> Event.AttachmentUploaded(
                                    localId = attachment.localId,
                                    uuid = result.uuid,
                                )
                            }
                        }
                        .collectLatest { postEvent(it) }
                }
            }
        }
        return state.copy(savingInProgress = true, attachments = attachments)
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
        val attachments = state.attachments.filterNot { it.localId == event.localId }
        return state.copy(attachments = attachments)
    }

    private fun handleListScrolled(state: State, event: UiEvent.ListScrolled): State {
        scope.launch {
            val globalEvent = GlobalEvent.ChangeFabVisibility(event.firstItemIndex == 0)
            eventsRepository.postGlobalEvent(globalEvent)
        }
        return state
    }

    private fun handlePreviewAttachment(state: State, event: UiEvent.PreviewAttachment): State {
        scope.launch {
            val navArgs = getAttachmentGalleryNavArgsUseCase(event.localId, state.attachments)
            val destination = DestinationData(AttachmentGalleryDestination(navArgs))
            eventsRepository.postNavEvent(destination)
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

    private fun List<State.Attachment>.updateUploaded(
        localId: Int,
        uuid: String? = null,
    ): List<State.Attachment> {
        return map { attachment ->
            if (attachment.localId == localId) {
                attachment.copy(
                    uuid = uuid,
                    uploadFailed = uuid == null,
                    progress = null,
                )
            } else {
                attachment
            }
        }
    }

    private fun List<State.Attachment>.allUploadsFinished(): Boolean {
        return all { it.isUploaded || it.uploadFailed }
    }

    data class State(
        val title: String = "",
        val description: String = "",
        val verificationErrors: List<VerificationError> = emptyList(),
        val attachments: List<Attachment> = emptyList(),
        val savingInProgress: Boolean = false,
    ) {

        data class Attachment(
            override val localId: Int,
            override val file: File,
            val progress: Float? = null,
            val uuid: String? = null,
            val uploadFailed: Boolean = false,
        ) : AttachmentData {
            val isUploaded: Boolean
                get() = uuid != null

            val isUploading: Boolean
                get() = progress != null
        }
    }

    data class UiState(
        val titleLength: Int,
        val descriptionLength: Int,
        val titleVerificationError: Int?,
        val descriptionVerificationError: Int?,
        val attachments: List<Attachment>,
        val isLoading: Boolean,
    ) {

        data class Attachment(
            val localId: Int,
            val file: File,
            val isUploading: Boolean,
            val progress: Float,
            val isUploaded: Boolean,
            val uploadFailed: Boolean,
        )
    }

    sealed interface Event {
        data object SaveReport : Event
        data object CreateReportSuccess : Event
        data class CreateReportFailed(val error: ErrorException) : Event
        data class PostVerificationError(val errors: List<VerificationError>) : Event
        data class AddAttachment(val photoFile: File) : Event
        data class AttachmentUploaded(val localId: Int, val uuid: String) : Event
        data class AttachmentUploadProgress(val localId: Int, val progress: Float) : Event
        data class AttachmentUploadFailed(
            val localId: Int,
            val error: ErrorException,
        ) : Event

        data object SaveAttachments : Event
    }

    sealed interface UiEvent : Event {
        data class TitleChanged(val title: String) : UiEvent
        data class DescriptionChanged(val description: String) : UiEvent
        data class DeleteAttachment(val localId: Int) : UiEvent
        data class ListScrolled(val firstItemIndex: Int) : UiEvent
        data class PreviewAttachment(val localId: Int) : UiEvent
    }

    data class Title(override val messageResId: Int) : VerificationError
    data class Description(override val messageResId: Int) : VerificationError
}
