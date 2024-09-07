package pl.fmizielinski.reports.ui.main.createreport

import android.net.Uri
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
import java.time.LocalDateTime

@KoinViewModel
class CreateReportViewModel(
    dispatcher: CoroutineDispatcher,
    private val eventsRepository: EventsRepository,
    private val createReportUseCase: CreateReportUseCase,
) : BaseViewModel<State, Event, UiState, UiEvent>(dispatcher, State()), ErrorHandler {

    init {
        scope.launch {
            eventsRepository.globalEvent
                .filterIsInstance<EventsRepository.GlobalEvent.SaveReport>()
                .collect { postEvent(Event.SaveReport) }
            eventsRepository.globalEvent
                .filterIsInstance<EventsRepository.GlobalEvent.PictureTaken>()
                .collect { postEvent(Event.PictureTaken(it.photoUri)) }
        }
    }

    override fun handleEvent(state: State, event: Event): State {
        return when (event) {
            is Event.SaveReport -> handleSaveReport(state)
            is Event.CreateReportSuccess -> handleCreateReportSuccess(state)
            is Event.CreateReportFailed -> handleCreateReportFailed(state, event)
            is Event.PostVerificationError -> handleVerificationError(state, event)
            is Event.PictureTaken -> handlePictureTaken(state, event)
            is UiEvent.TitleChanged -> handleTitleChanged(state, event)
            is UiEvent.DescriptionChanged -> handleDescriptionChanged(state, event)
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
            attachments = state.attachments,
        )
    }

    // region handle Event

    private fun handleSaveReport(state: State): State {
        scope.launch {
            try {
                createReportUseCase(
                    CreateReportData(
                        title = state.title,
                        description = state.description,
                        reportDate = LocalDateTime.now(),
                    ),
                )
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
        }
        return state
    }

    private fun handlePictureTaken(state: State, event: Event.PictureTaken): State {
        val attachments = buildList {
            addAll(state.attachments)
            add(event.photoUri)
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

    private fun handleCreateReportFailed(state: State, event: Event.CreateReportFailed): State {
        scope.launch {
            handleError(event.error)
            eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReportFailed)
        }
        return state
    }

    private fun handleVerificationError(state: State, event: Event.PostVerificationError): State {
        return state.copy(verificationErrors = event.errors)
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

    data class State(
        val title: String = "",
        val description: String = "",
        val verificationErrors: List<VerificationError> = emptyList(),
        val attachments: List<Uri> = emptyList(),
    )

    data class UiState(
        val titleLength: Int,
        val descriptionLength: Int,
        val titleVerificationError: Int?,
        val descriptionVerificationError: Int?,
        val attachments: List<Uri>,
    )

    sealed interface Event {
        data object SaveReport : Event
        data object CreateReportSuccess : Event
        data class CreateReportFailed(val error: ErrorException) : Event
        data class PostVerificationError(val errors: List<VerificationError>) : Event
        data class PictureTaken(val photoUri: Uri) : Event
    }

    sealed interface UiEvent : Event {
        data class TitleChanged(val title: String) : UiEvent
        data class DescriptionChanged(val description: String) : UiEvent
    }

    data class Title(override val messageResId: Int) : VerificationError
    data class Description(override val messageResId: Int) : VerificationError
}
