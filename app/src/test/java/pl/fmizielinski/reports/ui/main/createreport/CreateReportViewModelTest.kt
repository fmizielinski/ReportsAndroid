package pl.fmizielinski.reports.ui.main.createreport

import app.cash.turbine.testIn
import com.ramcosta.composedestinations.generated.destinations.ReportsDestination
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.ErrorReasons.Report.Create.DESCRIPTION_EMPTY
import pl.fmizielinski.reports.domain.error.ErrorReasons.Report.Create.INVALID_DATA
import pl.fmizielinski.reports.domain.error.ErrorReasons.Report.Create.TITLE_EMPTY
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.model.CreateReportData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.domain.usecase.report.AddTemporaryAttachmentUseCase
import pl.fmizielinski.reports.domain.usecase.report.CreateReportUseCase
import pl.fmizielinski.reports.fixtures.domain.addTemporaryAttachmentData
import pl.fmizielinski.reports.fixtures.domain.completeTemporaryAttachmentUploadResult
import pl.fmizielinski.reports.fixtures.domain.compositeErrorException
import pl.fmizielinski.reports.fixtures.domain.progressTemporaryAttachmentUploadResult
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import pl.fmizielinski.reports.utils.exceptionFlow
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import strikt.assertions.withFirst
import java.io.File

class CreateReportViewModelTest : BaseViewModelTest<CreateReportViewModel, UiEvent>() {

    private val createReportUseCase: CreateReportUseCase = mockk()
    private val addTemporaryAttachmentUseCase: AddTemporaryAttachmentUseCase = mockk()
    private val eventsRepository = spyk(EventsRepository())

    override fun createViewModel(dispatcher: TestDispatcher) = CreateReportViewModel(
        dispatcher = dispatcher,
        eventsRepository = eventsRepository,
        createReportUseCase = createReportUseCase,
        addTemporaryAttachmentUseCase = addTemporaryAttachmentUseCase,
    )

    @Test
    fun `WHEN title changed THEN update titleLength`() = runTurbineTest {
        val title = "title"

        val uiState = viewModel.uiState.testIn(context)
        uiState.skipItems(1)

        postUiEvent(UiEvent.TitleChanged(title))

        expectThat(uiState.awaitItem().titleLength) isEqualTo title.length

        uiState.cancel()
    }

    @Test
    fun `WHEN description changed THEN update titleLength`() = runTurbineTest {
        val description = "description"

        val uiState = viewModel.uiState.testIn(context)
        uiState.skipItems(1)

        postUiEvent(UiEvent.DescriptionChanged(description))

        expectThat(uiState.awaitItem().descriptionLength) isEqualTo description.length

        uiState.cancel()
    }

    @Test
    fun `WHEN save event posted AND create report verification errors THEH show verification error`() =
        runTurbineTest {
            val errorException = compositeErrorException(
                exceptions = listOf(
                    simpleErrorException(code = TITLE_EMPTY, isVerificationError = true),
                    simpleErrorException(code = DESCRIPTION_EMPTY, isVerificationError = true),
                ),
            )
            coEvery { createReportUseCase(any()) } throws errorException

            val uiState = viewModel.uiState.testIn(context, name = "uiState")
            val globalEvent = eventsRepository.globalEvent.testIn(context, name = "globalEvent")

            context.launch { eventsRepository.postGlobalEvent(GlobalEvent.SaveReport) }
            scheduler.advanceUntilIdle()

            expectThat(uiState.expectMostRecentItem()) {
                get { titleVerificationError }.isNotNull()
                get { descriptionVerificationError }.isNotNull()
            }

            uiState.cancel()
            globalEvent.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `GIVEN empty attachments WHEN save event posted AND create report success THEH post Reports navigation event`() =
        runTurbineTest {
            coJustRun { createReportUseCase(any()) }

            val uiState = viewModel.uiState.testIn(context, name = "uiState")

            context.launch {
                eventsRepository.postGlobalEvent(GlobalEvent.SaveReport)
            }
            scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { addTemporaryAttachmentUseCase(any()) }
            coVerify(exactly = 1) { eventsRepository.postNavEvent(ReportsDestination.toDestinationData()) }

            uiState.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `GIVEN has attachments WHEN save event posted THEN upload attachments AND save report AND post Reports navigation event`() =
        runTurbineTest {
            val file = File.createTempFile("test", "jpg")
            val attachmentUuid = "attachmentUuid"
            val createReportDataSlot = slot<CreateReportData>()

            coJustRun { createReportUseCase(capture(createReportDataSlot)) }
            coEvery { addTemporaryAttachmentUseCase(any()) } returns flowOf(
                completeTemporaryAttachmentUploadResult(attachmentUuid),
            )

            val uiState = viewModel.uiState.testIn(context, name = "uiState")

            context.launch {
                eventsRepository.postGlobalEvent(GlobalEvent.AddAttachment(file))
                eventsRepository.postGlobalEvent(GlobalEvent.SaveReport)
            }
            scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { addTemporaryAttachmentUseCase(addTemporaryAttachmentData(file)) }
            coVerify(exactly = 1) { eventsRepository.postNavEvent(ReportsDestination.toDestinationData()) }

            expectThat(createReportDataSlot.captured.attachments)
                .hasSize(1)
                .withFirst {
                    get { attachmentUuid } isEqualTo attachmentUuid
                }

            expectThat(uiState.expectMostRecentItem().attachments).withFirst {
                get { isUploaded }.isTrue()
            }

            uiState.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `GIVEN has attachments WHEN save event posted THEN update upload progress`() =
        runTurbineTest {
            val file = File.createTempFile("test", "jpg")

            coJustRun { createReportUseCase(any()) }
            coEvery { addTemporaryAttachmentUseCase(any()) } returns flowOf(
                progressTemporaryAttachmentUploadResult(0.5f),
            )

            val uiState = viewModel.uiState.testIn(context, name = "uiState")

            context.launch {
                eventsRepository.postGlobalEvent(GlobalEvent.AddAttachment(file))
                eventsRepository.postGlobalEvent(GlobalEvent.SaveReport)
            }
            scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { addTemporaryAttachmentUseCase(addTemporaryAttachmentData(file)) }

            expectThat(uiState.expectMostRecentItem().attachments).withFirst {
                get { isUploading }.isTrue()
                get { progress } isEqualTo 0.5f
            }

            uiState.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `GIVEN has attachments WHEN save event posted AND upload error THEH post snackbar event`() =
        runTurbineTest {
            val file = File.createTempFile("test", "jpg")
            val errorException = simpleErrorException(
                code = ErrorReasons.Report.Create.UPLOAD_FAILED,
                isVerificationError = false,
            )

            coEvery { addTemporaryAttachmentUseCase(any()) } returns exceptionFlow(errorException)

            val uiState = viewModel.uiState.testIn(context, name = "uiState")

            context.launch {
                eventsRepository.postGlobalEvent(GlobalEvent.AddAttachment(file))
                eventsRepository.postGlobalEvent(GlobalEvent.SaveReport)
            }
            scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { createReportUseCase(any()) }
            coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(errorException.toSnackBarData()) }

            uiState.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `WHEN save event posted AND create report error THEH post snackbar event AND post ChangeFabVisibility event`() =
        runTurbineTest {
            val errorException = simpleErrorException(
                code = INVALID_DATA,
                isVerificationError = false,
            )
            coEvery { createReportUseCase(any()) } throws errorException

            val uiState = viewModel.uiState.testIn(context, name = "uiState")

            context.launch {
                eventsRepository.postGlobalEvent(GlobalEvent.SaveReport)
            }
            scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(errorException.toSnackBarData()) }
            coVerify(exactly = 1) { eventsRepository.postGlobalEvent(GlobalEvent.ChangeFabVisibility(isVisible = true)) }

            uiState.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `WHEN AddAttachment global event posted THEH add attachment`() =
        runTurbineTest {
            val file = File.createTempFile("test", "jpg")

            val uiState = viewModel.uiState.testIn(context, name = "uiState")
            uiState.skipItems(1)

            context.launch {
                eventsRepository.postGlobalEvent(GlobalEvent.AddAttachment(file))
            }
            scheduler.advanceUntilIdle()

            val result = uiState.awaitItem()
            expectThat(result.attachments)
                .hasSize(1)
                .withFirst {
                    get { file } isEqualTo file
                }

            uiState.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `GIVEN has attachments WHEN DeleteAttachment event posted THEH delete attachment`() =
        runTurbineTest {
            val file = File.createTempFile("test", "jpg")

            val uiState = viewModel.uiState.testIn(context, name = "uiState")

            context.launch {
                eventsRepository.postGlobalEvent(GlobalEvent.AddAttachment(file))
            }
            scheduler.advanceUntilIdle()
            postUiEvent(UiEvent.DeleteAttachment(file))
            scheduler.advanceUntilIdle()

            val result = uiState.expectMostRecentItem()
            expectThat(result.attachments).isEmpty()

            uiState.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `WHEN list scrolled with first index 0 THEN post ChangeFabVisibility true event`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.ListScrolled(0))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(GlobalEvent.ChangeFabVisibility(true)) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN list scrolled with first index not 0 THEN post ChangeFabVisibility false event`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.ListScrolled(10))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(GlobalEvent.ChangeFabVisibility(false)) }

        uiState.cancelAndIgnoreRemainingEvents()
    }
}
